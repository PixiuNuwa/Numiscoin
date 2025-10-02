package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import cl.numiscoin2.network.NetworkConfig
import cl.numiscoin2.network.NetworkEventUtils
import cl.numiscoin2.setting.ProfileActivity
import cl.numiscoin2.setting.SettingsActivity
import com.bumptech.glide.Glide
import java.util.*
import java.text.SimpleDateFormat

class WelcomeActivity : BaseActivity() {

    private val TAG = "WelcomeActivity"
    private lateinit var eventosGridView: GridView
    private lateinit var calendarGridView: GridView
    private lateinit var settingsButton: ImageButton
    private var eventosList: List<Evento> = emptyList()
    private var eventosDelMes: List<Evento> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        Log.d(TAG, "onCreate: WelcomeActivity creada")
        usuario = SessionManager.usuario

        Log.d(TAG, "onCreate: Usuario obtenido - ID: ${usuario?.idUsuario}")

        val userName = "${usuario?.nombre ?: ""} ${usuario?.apellido ?: ""}".trim()

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        // AGREGAR LOGS PARA DIAGNOSTICAR EL BOTÓN DE CONFIGURACIÓN
        try {
            settingsButton = findViewById(R.id.settingsButton)
            Log.d(TAG, "onCreate: settingsButton encontrado - $settingsButton")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: ERROR - No se pudo encontrar settingsButton", e)
            Toast.makeText(this, "Error: No se encontró el botón de configuración", Toast.LENGTH_LONG).show()
            return
        }

        eventosGridView = findViewById(R.id.eventosGridView)
        calendarGridView = findViewById(R.id.calendarGridView)

        welcomeMessage.text = "Hola $userName, bienvenido a NumisCoin.\n" +
                "Aquí podrás ver los próximos eventos numismáticos."

        // AGREGAR LOGS PARA PROFILE BUTTON
        profileButton.setOnClickListener {
            Log.d(TAG, "profileButton: Clic detectado, iniciando ProfileActivity")
            try {
                ProfileActivity.start(this)
                Log.d(TAG, "profileButton: ProfileActivity iniciada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "profileButton: ERROR al iniciar ProfileActivity", e)
                Toast.makeText(this, "Error al abrir perfil", Toast.LENGTH_SHORT).show()
            }
        }

        // AGREGAR LOGS DETALLADOS PARA SETTINGS BUTTON
        settingsButton.setOnClickListener {
            Log.d(TAG, "settingsButton: Clic detectado en botón configuración")
            Log.d(TAG, "settingsButton: Context actual - $this")

            try {
                Log.d(TAG, "settingsButton: Intent creado, iniciando SettingsActivity...")
                SettingsActivity.start(this)
                Log.d(TAG, "settingsButton: SettingsActivity iniciada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "settingsButton: ERROR crítico al iniciar SettingsActivity", e)
                Toast.makeText(this, "Error al abrir configuración: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        usuario?.let { user ->
            if (user.foto.isNotEmpty()) {
                val fotoUrl = NetworkConfig.construirUrlCompleta(user.foto)
                Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.circle_white_background)
                    .error(R.drawable.circle_white_background)
                    .circleCrop()
                    .into(profileButton)
            }
        }

        logoutButton.setOnClickListener {
            Log.d(TAG, "logoutButton: Clic detectado, finalizando actividad")
            finish()
        }

        // Cargar eventos futuros para la galería
        cargarEventosFuturos()

        // Cargar eventos del mes actual para el calendario
        cargarEventosDelMesActual()

        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)

        Log.d(TAG, "onCreate: UI configurada correctamente")
    }

    private fun cargarEventosFuturos() {
        NetworkEventUtils.getEventosFuturos { eventos, error ->
            runOnUiThread {
                if (error == null && eventos != null) {
                    this.eventosList = eventos
                    val adapter = EventoAdapter(this, eventos)
                    eventosGridView.adapter = adapter

                    eventosGridView.setOnItemClickListener { _, _, position, _ ->
                        val evento = eventos[position]
                        //mostrarDetalleEvento(evento)
                        EventoDetailActivity.start(this, evento.idEvento)
                    }

                    Log.d(TAG, "Cargados ${eventos.size} eventos futuros")
                } else {
                    Toast.makeText(this, "Error al cargar eventos futuros: $error", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error al cargar eventos futuros: $error")
                }
            }
        }
    }

    private fun cargarEventosDelMesActual() {
        val calendar = Calendar.getInstance()
        val anio = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-based

        NetworkEventUtils.getEventosPorMes(anio, mes) { eventos, error ->
            runOnUiThread {
                if (error == null && eventos != null) {
                    this.eventosDelMes = eventos
                    configurarCalendario(anio, mes, eventos)
                    Log.d(TAG, "Cargados ${eventos.size} eventos para el mes $mes/$anio")
                } else {
                    // Si hay error, mostrar calendario vacío
                    configurarCalendario(anio, mes, emptyList())
                    Log.e(TAG, "Error al cargar eventos del mes: $error")
                }
            }
        }
    }

    private fun configurarCalendario(año: Int, mes: Int, eventos: List<Evento>) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, año)
            set(Calendar.MONTH, mes - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val diasEnMes = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val primerDiaSemana = calendar.get(Calendar.DAY_OF_WEEK)

        // Obtener el último día del mes para saber cuántas semanas necesitamos
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val ultimoDiaMes = calendar.get(Calendar.DAY_OF_MONTH)
        val ultimoDiaSemana = calendar.get(Calendar.DAY_OF_WEEK)

        // Calcular el número total de celdas (siempre 6 semanas para consistencia)
        val totalCeldas = 42 // 6 semanas * 7 días

        val diasCalendario = mutableListOf<String>()

        // Agregar días vacíos para alinear el primer día
        for (i in 1 until primerDiaSemana) {
            diasCalendario.add("")
        }

        // Agregar los días del mes
        for (dia in 1..diasEnMes) {
            diasCalendario.add(dia.toString())
        }

        // Agregar días vacíos al final para completar 6 semanas
        val celdasRestantes = totalCeldas - diasCalendario.size
        for (i in 1..celdasRestantes) {
            diasCalendario.add("")
        }

        // Configurar el GridView para que tenga 7 columnas (días de la semana)
        calendarGridView.numColumns = 7

        val adapter = CalendarioAdapter(this, diasCalendario, eventos, año, mes)
        calendarGridView.adapter = adapter

        Log.d(TAG, "Calendario configurado: $diasEnMes días, $totalCeldas celdas, ${diasCalendario.size} items")
    }

    private fun mostrarDetalleEvento(evento: Evento) {
        Toast.makeText(this, "Evento: ${evento.nombreEvento}\nFecha: ${formatearFecha(evento.fechaInicio)}",
            Toast.LENGTH_LONG).show()
    }

    private fun formatearFecha(fecha: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(fecha)
    }

    companion object {
        fun start(context: Context) {
            Log.d("WelcomeActivity", "start: Iniciando WelcomeActivity")
            val intent = Intent(context, WelcomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}