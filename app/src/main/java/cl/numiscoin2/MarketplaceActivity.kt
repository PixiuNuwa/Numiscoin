package cl.numiscoin2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkEventUtils
import java.util.Calendar

class MarketplaceActivity : BaseActivity() {

    private val TAG = "MarketplaceActivity"
    private lateinit var eventosGridView: GridView
    private lateinit var calendarGridView: GridView
    private var eventosList: List<Evento> = emptyList()
    private var eventosDelMes: List<Evento> = emptyList()

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
        setContentView(R.layout.activity_marketplace)

        eventosGridView = findViewById(R.id.eventosGridView)
        calendarGridView = findViewById(R.id.calendarGridView)

        // Cargar eventos futuros para la galería
        cargarEventosFuturos()

        // Cargar eventos del mes actual para el calendario
        cargarEventosDelMesActual()

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuMarketplace) // Marcar Home como seleccionado
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

        // Calcular el número total de celdas (6 semanas * 7 días + 7 días para los encabezados)
        val totalCeldas = 49 // 7 semanas * 7 días (incluyendo la fila de encabezados)

        val diasCalendario = mutableListOf<String>()

        // AGREGAR ENCABEZADOS DE DÍAS DE LA SEMANA EN LA PRIMERA FILA
        diasCalendario.addAll(listOf("LU", "MA", "MI", "JU", "VI", "SA", "DO"))

        // Agregar días vacíos para alinear el primer día (ajustado por la fila de encabezados)
        for (i in 1 until primerDiaSemana) {
            diasCalendario.add("")
        }

        // Agregar los días del mes
        for (dia in 1..diasEnMes) {
            diasCalendario.add(dia.toString())
        }

        // Agregar días vacíos al final para completar 6 semanas (42 días) + 7 encabezados
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

}