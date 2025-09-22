package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    protected var usuario: Usuario? = SessionManager.usuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ELIMINAR toda configuración de system windows
        // El sistema por defecto ya maneja correctamente la posición del contenido
    }

    protected fun setupBottomMenu() {
        // Botón Home
        findViewById<View>(R.id.menuHome).setOnClickListener {
            highlightMenuItem(R.id.menuHome)
            if (this !is WelcomeActivity) {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Botón Calculadora
        findViewById<View>(R.id.menuCalculator).setOnClickListener {
            highlightMenuItem(R.id.menuCalculator)
            if (this !is CalculatorActivity) {
                val intent = Intent(this, CalculatorActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Botón Mi Colección
        findViewById<View>(R.id.menuCollection).setOnClickListener {
            highlightMenuItem(R.id.menuCollection)
            if (this !is CollectionActivity) {
                val intent = Intent(this, CollectionActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Botón Divisas
        findViewById<View>(R.id.menuCurrency).setOnClickListener {
            highlightMenuItem(R.id.menuCurrency)
            if (this !is CurrencyActivity) {
                val intent = Intent(this, CurrencyActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Botón Marketplace
        findViewById<View>(R.id.menuMarketplace).setOnClickListener {
            highlightMenuItem(R.id.menuMarketplace)
            if (this !is MarketplaceActivity) {
                val intent = Intent(this, MarketplaceActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    protected fun highlightMenuItem(selectedMenuId: Int) {
        val menuIds = listOf(
            R.id.menuHome, R.id.menuCalculator, R.id.menuCollection,
            R.id.menuCurrency, R.id.menuMarketplace
        )

        menuIds.forEach { menuId ->
            val menuItem = findViewById<LinearLayout>(menuId)
            if (menuItem.childCount >= 2) {
                val menuText = menuItem.getChildAt(1) as TextView

                if (menuId == selectedMenuId) {
                    menuText.setTextColor(0xFF0000FF.toInt())
                } else {
                    menuText.setTextColor(0xFF888888.toInt())
                }
            }
        }
    }

    protected fun checkUserLoggedIn(): Boolean {
        return SessionManager.isLoggedIn && SessionManager.usuario != null
    }

    protected fun redirectToLoginIfNotLogged() {
        if (!checkUserLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}