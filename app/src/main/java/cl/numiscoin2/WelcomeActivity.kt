//<<WelcomeActivity.kt
package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : BaseActivity() {

    private val TAG = "WelcomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        Log.d(TAG, "onCreate: WelcomeActivity creada")

        // Ahora el usuario viene de la variable heredada de BaseActivity
        Log.d(TAG, "onCreate: Usuario recibido - ID: ${usuario?.idUsuario}, Nombre: ${usuario?.nombre} ${usuario?.apellido}")

        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "${usuario?.nombre ?: ""} ${usuario?.apellido ?: ""}"

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        welcomeMessage.text = "Hola $userName, has iniciado sesión correctamente.\n" +
                "Email: ${usuario?.email ?: "N/A"}\n" +
                "ID: ${usuario?.idUsuario ?: "N/A"}"

        logoutButton.setOnClickListener {
            finish()
        }

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome) // Marcar Home como seleccionado

        Log.d(TAG, "onCreate: UI configurada correctamente")
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_USUARIO = "extra_usuario"

        fun start(context: Context, nombreCompleto: String, usuario: Usuario) {
            Log.d("WelcomeActivity", "start: Iniciando WelcomeActivity con usuario ID: ${usuario.idUsuario}")
            val intent = Intent(context, WelcomeActivity::class.java).apply {
                putExtra(EXTRA_USER_NAME, nombreCompleto)
                putExtra(EXTRA_USUARIO, usuario)
            }
            context.startActivity(intent)
        }
    }
}
//>>WelcomeActivity.kt