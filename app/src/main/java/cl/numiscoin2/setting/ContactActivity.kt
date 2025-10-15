package cl.numiscoin2.setting


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R

class ContactActivity : BaseActivity() {

    private lateinit var emailTextView: EditText
    private lateinit var tituloTextView: EditText
    private lateinit var mensajeTextView: TextView
    private lateinit var sendButton: Button
    //private lateinit var callButton: Button

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
        setContentView(R.layout.activity_contact)

        initViews()

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }

    private fun initViews() {
        emailTextView = findViewById(R.id.emailEditText)
        tituloTextView = findViewById(R.id.tituloEditText)
        mensajeTextView = findViewById(R.id.etMensaje)
        sendButton = findViewById(R.id.sendButton)
        //callButton = findViewById(R.id.callButton)
        // Configurar botones de acción
        sendButton.setOnClickListener {
            sendEmail()
        }

    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:soporte@numiscoin.com")
                putExtra(Intent.EXTRA_SUBJECT, "Consulta NumisCoin")
                putExtra(Intent.EXTRA_TEXT, "Hola equipo NumisCoin,\n\n")
            }
            startActivity(Intent.createChooser(intent, "Enviar email"))
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir la aplicación de email", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ContactActivity::class.java)
            context.startActivity(intent)
        }
    }
}