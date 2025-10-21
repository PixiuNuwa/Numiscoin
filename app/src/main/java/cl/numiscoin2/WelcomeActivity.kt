//<<WelcomeActivity.kt
package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.numiscoin2.adapters.EventoHorizontalAdapter
import cl.numiscoin2.adapters.ObjetoRecienteHorizontalAdapter
import cl.numiscoin2.network.NetworkConfig
import cl.numiscoin2.network.NetworkEventUtils
import cl.numiscoin2.network.NetworkObjectUtils
import cl.numiscoin2.setting.ProfileActivity
import cl.numiscoin2.setting.SettingsActivity
import com.bumptech.glide.Glide
import java.util.*
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Locale

class WelcomeActivity : BaseActivity() {

    private val TAG = "WelcomeActivity"
    //private lateinit var eventosGridView: GridView

    private lateinit var objetosRecientesGridView: GridView
    private lateinit var settingsButton: ImageView
    private lateinit var totalColeccionValor: TextView
    private lateinit var totalItemsCount: TextView
    private lateinit var totalGastadoValor: TextView

    private var eventosList: List<Evento> = emptyList()
    private var objetosRecientesList: List<ObjetoColeccion> = emptyList()
    //private lateinit var objetosRecientesAdapter: ObjetoRecienteAdapter
    private lateinit var objetosRecientesRecyclerView: RecyclerView
    private lateinit var objetosRecientesAdapter: ObjetoRecienteHorizontalAdapter

    private lateinit var eventosRecyclerView: RecyclerView
    private lateinit var eventosAdapter: EventoHorizontalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        //
        setContentView(R.layout.activity_welcome)

        Log.d(TAG, "onCreate: WelcomeActivity creada")
        usuario = SessionManager.usuario

        Log.d(TAG, "onCreate: Usuario obtenido - ID: ${usuario?.idUsuario}")

        // Inicializar las vistas de totales
        inicializarVistasTotales()

        val userName = "${usuario?.nombre ?: ""} ${usuario?.apellido ?: ""}".trim()

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val welcomeTitle = findViewById<TextView>(R.id.welcomeTitle)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        try {
            settingsButton = findViewById(R.id.settingsButton)
            Log.d(TAG, "onCreate: settingsButton encontrado - $settingsButton")

            // CARGAR FOTO DEL USUARIO EN EL BOTÓN
            cargarFotoUsuario()

        } catch (e: Exception) {
            Log.e(TAG, "onCreate: ERROR - No se pudo encontrar settingsButton", e)
            Toast.makeText(this, "Error: No se encontró el botón de configuración", Toast.LENGTH_LONG).show()
            return
        }

        //eventosRecyclerView = findViewById(R.id.eventosRecyclerView)
        //objetosRecientesGridView = findViewById(R.id.objetosRecientesGridView)

        // Inicializar adapter para objetos recientes
        //objetosRecientesAdapter = ObjetoRecienteAdapter(this, emptyList())
        //objetosRecientesGridView.adapter = objetosRecientesAdapter
        // Inicializar RecyclerView horizontal
        objetosRecientesRecyclerView = findViewById(R.id.objetosRecientesRecyclerView)

        // Configurar LayoutManager horizontal
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        objetosRecientesRecyclerView.layoutManager = layoutManager
        objetosRecientesRecyclerView.setHasFixedSize(true)

        // Opcional: agregar efecto de sombra en los bordes
        objetosRecientesRecyclerView.setPadding(32, 0, 32, 0)
        objetosRecientesRecyclerView.clipToPadding = false

        objetosRecientesAdapter = ObjetoRecienteHorizontalAdapter()
        objetosRecientesRecyclerView.adapter = objetosRecientesAdapter

        welcomeTitle.text = "Hola $userName,"

        welcomeMessage.text = "bienvenido a Numiscoin, aquí están tus próximos eventos numismáticos."

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

        logoutButton.setOnClickListener {
            Log.d(TAG, "logoutButton: Clic detectado, finalizando actividad")
            finish()
        }

        eventosRecyclerView = findViewById(R.id.eventosRecyclerView)

// Configurar LayoutManager horizontal para eventos
        val eventosLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        eventosRecyclerView.layoutManager = eventosLayoutManager
        eventosRecyclerView.setHasFixedSize(true)

// Inicializar adapter para eventos
        eventosAdapter = EventoHorizontalAdapter()
        eventosRecyclerView.adapter = eventosAdapter

        // Cargar totales del usuario
        cargarTotalesUsuario()

        // Cargar eventos futuros para la galería
        cargarEventosFuturos()

        // Cargar objetos recientes
        cargarObjetosRecientes()

        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)

        Log.d(TAG, "onCreate: UI configurada correctamente")
    }

    private fun cargarFotoUsuario() {
        usuario?.let { usuario ->
            try {
                // Verificar si el usuario tiene una foto
                if (usuario.foto.isNotEmpty()) {
                    // Construir la URL completa de la foto
                    //val fotoUrl = "${NetworkConfig.BASE_URL}${usuario.foto}"
                    val fotoUrl = NetworkConfig.construirUrlCompleta(usuario.foto)
                    Log.d(TAG, "cargarFotoUsuario: Cargando foto desde: $fotoUrl")

                    // Usar Glide para cargar la imagen en el ImageButton
                    Glide.with(this)
                        .load(fotoUrl)
                        .placeholder(R.drawable.ic_settings_white) // Placeholder por si falla
                        .error(R.drawable.ic_settings_white) // Imagen de error
                        .circleCrop() // Recortar en círculo para que quede bien en el botón redondo
                        .into(settingsButton)

                    Log.d(TAG, "cargarFotoUsuario: Foto cargada exitosamente")
                } else {
                    Log.d(TAG, "cargarFotoUsuario: El usuario no tiene foto, usando ícono por defecto")
                    settingsButton.setImageResource(R.drawable.ic_settings_white)
                }
            } catch (e: Exception) {
                Log.e(TAG, "cargarFotoUsuario: Error al cargar la foto del usuario", e)
                settingsButton.setImageResource(R.drawable.ic_settings_white)
            }
        } ?: run {
            Log.e(TAG, "cargarFotoUsuario: No hay usuario disponible")
            settingsButton.setImageResource(R.drawable.ic_settings_white)
        }
    }

    private fun inicializarVistasTotales() {
        totalColeccionValor = findViewById(R.id.totalColeccionValor)
        totalItemsCount = findViewById(R.id.totalItemsCount)
        totalGastadoValor = findViewById(R.id.totalGastadoValor)
    }

    private fun cargarTotalesUsuario() {
        usuario?.idUsuario?.let { idUsuario ->
            Log.d(TAG, "cargarTotalesUsuario: Solicitando totales para usuario ID: $idUsuario")

            NetworkObjectUtils.obtenerTotalesPorUsuario(idUsuario.toLong()) { totales, error ->
                runOnUiThread {
                    if (error == null && totales != null) {
                        Log.d(TAG, "cargarTotalesUsuario: Totales recibidos - " +
                                "Colección: ${totales.totalColeccion}, " +
                                "Gastado: ${totales.totalGastado}, " +
                                "Items: ${totales.totalItems}")

                        actualizarVistasConTotales(totales)
                    } else {
                        Log.e(TAG, "cargarTotalesUsuario: Error al cargar totales - $error")
                        mostrarTotalesPorDefecto()
                        Toast.makeText(this, "Error al cargar estadísticas: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "cargarTotalesUsuario: ID de usuario no disponible")
            mostrarTotalesPorDefecto()
        }
    }

    private fun cargarObjetosRecientes() {
        usuario?.idUsuario?.let { idUsuario ->
            Log.d(TAG, "cargarObjetosRecientes: Solicitando objetos recientes para usuario ID: $idUsuario")

            NetworkObjectUtils.obtenerUltimosObjetosPorUsuario(idUsuario, 10) { objetos, error ->
                runOnUiThread {
                    if (error == null && objetos != null) {
                        Log.d(TAG, "cargarObjetosRecientes: ${objetos.size} objetos recibidos")
                        this.objetosRecientesList = objetos

                        objetosRecientesAdapter.actualizarObjetos(objetos)

                        // Configurar click listener
                        objetosRecientesAdapter.onItemClick = { objeto ->
                            Toast.makeText(this, "Objeto: ${objeto.nombre}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "cargarObjetosRecientes: Error al cargar objetos - $error")
                        Toast.makeText(this, "Error al cargar objetos recientes: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "cargarObjetosRecientes: ID de usuario no disponible")
        }
    }

    private fun actualizarVistasConTotales(totales: TotalesUsuarioResponse) {
        try {
            // Formatear valores monetarios
            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            formatoMoneda.maximumFractionDigits = 0

            // Total Colección
            totalColeccionValor.text = formatoMoneda.format(totales.totalColeccion ?: 0)

            // Total Items
            totalItemsCount.text = (totales.totalItems ?: 0).toString()

            // Total Gastado (en negativo y en rojo como en el diseño original)
            val totalGastado = totales.totalGastado ?: 0
            totalGastadoValor.text = formatoMoneda.format(totalGastado)

            Log.d(TAG, "actualizarVistasConTotales: Vistas actualizadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "actualizarVistasConTotales: Error al formatear valores", e)
            mostrarTotalesPorDefecto()
        }
    }

    private fun mostrarTotalesPorDefecto() {
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        formatoMoneda.maximumFractionDigits = 0

        totalColeccionValor.text = formatoMoneda.format(0)
        totalItemsCount.text = "0"
        //totalGastadoValor.text = "-${formatoMoneda.format(0)}"
        totalGastadoValor.text = formatoMoneda.format(0)
    }

    private fun cargarEventosFuturos() {
        NetworkEventUtils.getEventosFuturos { eventos, error ->
            runOnUiThread {
                if (error == null && eventos != null) {
                    this.eventosList = eventos
                    Log.d("WelcomeActivity","eventos: ${eventos}")
                    eventosAdapter.actualizarEventos(eventos)

                    // Configurar click listener
                    eventosAdapter.onItemClick = { evento ->
                        EventoDetailActivity.start(this, evento.idEvento)
                    }

                    Log.d(TAG, "Cargados ${eventos.size} eventos futuros")
                } else {
                    Toast.makeText(
                        this,
                        "Error al cargar eventos futuros: $error",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Error al cargar eventos futuros: $error")
                }
            }
        }
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