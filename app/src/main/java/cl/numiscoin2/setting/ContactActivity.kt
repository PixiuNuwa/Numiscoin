package cl.numiscoin2.setting


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R

class ContactActivity : BaseActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var emailButton: Button
    private lateinit var callButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        initViews()
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }

    private fun initViews() {
        emailTextView = findViewById(R.id.emailTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        emailButton = findViewById(R.id.emailButton)
        callButton = findViewById(R.id.callButton)

        // Configurar información de contacto
        emailTextView.text = "soporte@numiscoin.com"
        phoneTextView.text = "+56 2 1234 5678"

        // Configurar botones de acción
        emailButton.setOnClickListener {
            sendEmail()
        }

        callButton.setOnClickListener {
            makePhoneCall()
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

    private fun makePhoneCall() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+56212345678")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo realizar la llamada", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ContactActivity::class.java)
            context.startActivity(intent)
        }
    }
}