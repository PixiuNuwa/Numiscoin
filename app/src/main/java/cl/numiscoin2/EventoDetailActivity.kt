package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkConfig
import cl.numiscoin2.network.NetworkEventUtils
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class EventoDetailActivity : BaseActivity() {

    private val TAG = "EventoDetailActivity"
    private lateinit var backButton: ImageButton
    private lateinit var eventoPoster: ImageView
    private lateinit var eventoTitulo: TextView
    private lateinit var eventoFecha: TextView
    private lateinit var eventoDescripcion: TextView
    private lateinit var eventoBreveDescripcion: TextView
    //private lateinit var closeButton: Button

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
        setContentView(R.layout.activity_evento_detail)

        Log.d(TAG, "onCreate: EventoDetailActivity creada")

        // Obtener el ID del evento del intent
        val eventoId = intent.getIntExtra(EVENTO_ID_EXTRA, -1)

        if (eventoId == -1) {
            Toast.makeText(this, "Error: No se pudo cargar el evento", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initViews()
        setupClickListeners()
        cargarDetalleEvento(eventoId)

        setupBottomMenu()
        highlightMenuItem(R.id.menuMarketplace)
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        eventoPoster = findViewById(R.id.eventoPoster)
        eventoTitulo = findViewById(R.id.eventoTitulo)
        eventoFecha = findViewById(R.id.eventoFecha)
        eventoDescripcion = findViewById(R.id.eventoDescripcion)
        eventoBreveDescripcion = findViewById(R.id.eventoBreveDescripcion)
        //closeButton = findViewById(R.id.closeButton)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            Log.d(TAG, "backButton: Clic detectado, volviendo atrás")
            onBackPressed()
        }

        /*closeButton.setOnClickListener {
            Log.d(TAG, "closeButton: Clic detectado, finalizando actividad")
            finish()
        }*/
    }

    private fun cargarDetalleEvento(eventoId: Int) {
        Log.d(TAG, "cargarDetalleEvento: Cargando detalles del evento ID: $eventoId")

        // Mostrar loading
        findViewById<TextView>(R.id.eventoTitulo).text = "Cargando..."
        findViewById<TextView>(R.id.eventoDescripcion).text = ""

        NetworkEventUtils.getEventoPorId(eventoId) { evento, error ->
            runOnUiThread {
                if (error == null && evento != null) {
                    mostrarDetallesEvento(evento)
                    Log.d(TAG, "cargarDetalleEvento: Evento cargado exitosamente - ${evento.nombreEvento}")
                } else {
                    Toast.makeText(this, "Error al cargar detalles del evento: $error", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error al cargar detalles del evento: $error")
                    // Mostrar mensaje de error en la UI
                    findViewById<TextView>(R.id.eventoTitulo).text = "Error al cargar evento"
                    findViewById<TextView>(R.id.eventoDescripcion).text = "Por favor, intente más tarde"
                }
            }
        }
    }

    private fun mostrarDetallesEvento(evento: Evento) {
        // Cargar imagen del poster
        if (evento.fotoPoster.isNotEmpty()) {
            val posterUrl = NetworkConfig.construirUrlCompleta(evento.fotoPoster)
            Glide.with(this)
                .load(posterUrl)
                .placeholder(R.drawable.circle_white_background)
                .error(R.drawable.circle_white_background)
                .into(eventoPoster)
        }

        // Configurar textos
        eventoTitulo.text = evento.nombreEvento
        eventoBreveDescripcion.text = evento.breveDescripcion
        eventoDescripcion.text = evento.descripcion

        Log.d("EventoDetailActivity", "Fecha Ini: ${formatearFecha(evento.fechaInicio)}")
        // Formatear fecha
        val fechaFormateada = if (evento.fechaInicio == evento.fechaFin) {
            "Fecha: ${formatearFecha(evento.fechaInicio)}"
        } else {
            "Del ${formatearFecha(evento.fechaInicio)} al ${formatearFecha(evento.fechaFin)}"
        }
        eventoFecha.text = fechaFormateada
    }

    private fun formatearFecha(fecha: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(fecha)
    }

    companion object {
        private const val EVENTO_ID_EXTRA = "evento_id"

        fun start(context: Context, eventoId: Int) {
            Log.d("EventoDetailActivity", "start: Iniciando EventoDetailActivity para evento ID: $eventoId")
            val intent = Intent(context, EventoDetailActivity::class.java)
            intent.putExtra(EVENTO_ID_EXTRA, eventoId)
            context.startActivity(intent)
        }
    }
}