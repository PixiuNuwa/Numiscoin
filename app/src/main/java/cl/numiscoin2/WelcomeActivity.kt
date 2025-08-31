package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : BaseActivity() {

    private lateinit var usuario: Usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Obtener el objeto Usuario del intent
        usuario = intent.getParcelableExtra(EXTRA_USUARIO) ?: run {
            // Si no viene el usuario, crear uno vacío (solo para evitar crash)
            Usuario(0, "Usuario", "", "", "", "")
        }

        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "${usuario.nombre} ${usuario.apellido}"

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        welcomeMessage.text = "Hola $userName, has iniciado sesión correctamente.\n" +
                "Email: ${usuario.email}\n" +
                "ID: ${usuario.idUsuario}"

        logoutButton.setOnClickListener {
            finish()
        }

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome) // Marcar Home como seleccionado
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_USUARIO = "extra_usuario"

        fun start(context: Context, nombreCompleto: String, usuario: Usuario) {
            val intent = Intent(context, WelcomeActivity::class.java).apply {
                putExtra(EXTRA_USER_NAME, nombreCompleto)
                putExtra(EXTRA_USUARIO, usuario) //
            }
            context.startActivity(intent)
        }
    }
}
/*package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import cl.numiscoin2.CurrencyActivity.Companion.EXTRA_USER_NAME

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "Usuario"

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        welcomeMessage.text = "Hola $userName, has iniciado sesión correctamente."

        logoutButton.setOnClickListener {
            finish()
        }

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome) // Marcar Home como seleccionado
    }

    companion object {
        fun start(context: Context, nombreCompleto: String, usuario: Usuario) {
            val intent = Intent(context, WelcomeActivity::class.java).apply {
                putExtra("NOMBRE_COMPLETO", nombreCompleto)
                putExtra("USUARIO", usuario)
            }
            context.startActivity(intent)
        }
    }
}*/
