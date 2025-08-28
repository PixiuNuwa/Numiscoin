package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MarketplaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)



        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)


        welcomeMessage.text = "Hola."


        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuMarketplace) // Marcar Home como seleccionado
    }

}