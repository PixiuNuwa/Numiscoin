package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MarketplaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)



        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        welcomeMessage.text = "Hola."

        logoutButton.setOnClickListener {
            finish()
        }

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome) // Marcar Home como seleccionado
    }

}