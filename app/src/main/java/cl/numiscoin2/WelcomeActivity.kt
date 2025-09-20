package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class WelcomeActivity : BaseActivity() {

    private val TAG = "WelcomeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        Log.d(TAG, "onCreate: WelcomeActivity creada")

        //Log.d(TAG, "onCreate: Usuario recibido - ID: ${usuario?.idUsuario}, Nombre: ${usuario?.nombre} ${usuario?.apellido}")
        usuario = SessionManager.usuario

        Log.d(TAG, "onCreate: Usuario obtenido - ID: ${usuario?.idUsuario}")

        val userName = "${usuario?.nombre ?: ""} ${usuario?.apellido ?: ""}".trim()

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        welcomeMessage.text = "Hola $userName, has iniciado sesión correctamente.\n" +
                "Email: ${usuario?.email ?: "N/A"}\n" +
                "ID: ${usuario?.idUsuario ?: "N/A"}"

        // Configurar botón de perfil
        profileButton.setOnClickListener {
            ProfileActivity.start(this)
        }

        // Cargar avatar si existe
        usuario?.let { user ->
            if (user.foto.isNotEmpty()) {
                val fotoUrl = NetworkUtils.construirUrlCompleta(user.foto)
                Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.circle_white_background)
                    .error(R.drawable.circle_white_background)
                    .circleCrop()
                    .into(profileButton)
            }
        }

        logoutButton.setOnClickListener {
            finish()
        }

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome) // Marcar Home como seleccionado

        Log.d(TAG, "onCreate: UI configurada correctamente")
    }

    companion object {
        fun start(context: Context) {
            Log.d("WelcomeActivity", "start: Iniciando WelcomeActivity")
            val intent = Intent(context, WelcomeActivity::class.java)
            context.startActivity(intent)
        }
    }
}
