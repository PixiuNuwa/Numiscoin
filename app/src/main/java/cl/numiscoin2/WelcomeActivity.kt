package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class WelcomeActivity : ComponentActivity() {

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

        // Configurar listeners del menú inferior
        setupBottomMenu()
    }

    private fun setupBottomMenu() {
        // Botón Home
        findViewById<View>(R.id.menuHome).setOnClickListener {
            highlightMenuItem(R.id.menuHome)
            Toast.makeText(this, "Home seleccionado", Toast.LENGTH_SHORT).show()
        }

        // Botón Calculadora
        findViewById<View>(R.id.menuCalculator).setOnClickListener {
            highlightMenuItem(R.id.menuCalculator)
            Toast.makeText(this, "Calculadora seleccionada", Toast.LENGTH_SHORT).show()
        }

        // Botón Mi Colección
        findViewById<View>(R.id.menuCollection).setOnClickListener {
            highlightMenuItem(R.id.menuCollection)
            //Toast.makeText(this, "Mi Colección seleccionada", Toast.LENGTH_SHORT).show()
            // Navegar a la actividad de colección
            val intent = Intent(this, CollectionActivity::class.java)
            startActivity(intent)
        }

        // Botón Divisas
        findViewById<View>(R.id.menuCurrency).setOnClickListener {
            highlightMenuItem(R.id.menuCurrency)
            Toast.makeText(this, "Divisas seleccionadas", Toast.LENGTH_SHORT).show()
        }

        // Botón Marketplace
        findViewById<View>(R.id.menuMarketplace).setOnClickListener {
            highlightMenuItem(R.id.menuMarketplace)
            Toast.makeText(this, "Marketplace seleccionado", Toast.LENGTH_SHORT).show()
        }

        // Marcar el primer botón como seleccionado por defecto
        highlightMenuItem(R.id.menuHome)
    }

    private fun highlightMenuItem(selectedMenuId: Int) {
        val menuIds = listOf(
            R.id.menuHome, R.id.menuCalculator, R.id.menuCollection,
            R.id.menuCurrency, R.id.menuMarketplace
        )

        menuIds.forEach { menuId ->
            val menuItem = findViewById<LinearLayout>(menuId)
            // Acceder a los hijos del LinearLayout
            if (menuItem.childCount >= 2) {
                val menuText = menuItem.getChildAt(1) as TextView

                if (menuId == selectedMenuId) {
                    // Botón seleccionado - Azul
                    menuText.setTextColor(0xFF0000FF.toInt())
                } else {
                    // Botón no seleccionado - Gris
                    menuText.setTextColor(0xFF888888.toInt())
                }
            }
        }
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"

        fun start(activity: ComponentActivity, userName: String) {
            val intent = Intent(activity, WelcomeActivity::class.java)
            intent.putExtra(EXTRA_USER_NAME, userName)
            activity.startActivity(intent)
        }
    }
}