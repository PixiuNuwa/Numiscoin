//<<BaseActivity.kt
package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    // Variable protegida para almacenar el usuario
    protected var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtener el usuario del intent si está disponible
        usuario = intent.getParcelableExtra(WelcomeActivity.EXTRA_USUARIO)
    }

    protected fun setupBottomMenu() {
        // Botón Home
        findViewById<View>(R.id.menuHome).setOnClickListener {
            highlightMenuItem(R.id.menuHome)
            if (this !is WelcomeActivity) {
                val intent = Intent(this, WelcomeActivity::class.java)
                // Pasar el usuario si está disponible
                usuario?.let { user ->
                    intent.putExtra(WelcomeActivity.EXTRA_USUARIO, user)
                }
                // Pasar el nombre de usuario si está disponible
                if (this is CurrencyActivity) {
                    val userName = intent.getStringExtra(CurrencyActivity.EXTRA_USER_NAME)
                    userName?.let {
                        intent.putExtra(WelcomeActivity.EXTRA_USER_NAME, it)
                    }
                }
                startActivity(intent)
                finish()
            }
        }

        // Botón Calculadora
        findViewById<View>(R.id.menuCalculator).setOnClickListener {
            highlightMenuItem(R.id.menuCalculator)
            if (this !is CalculatorActivity) {
                val intent = Intent(this, CalculatorActivity::class.java)
                // Pasar el usuario si está disponible
                usuario?.let { user ->
                    intent.putExtra(WelcomeActivity.EXTRA_USUARIO, user)
                }
                // Pasar el nombre de usuario si está disponible
                if (this is CurrencyActivity) {
                    val userName = intent.getStringExtra(CurrencyActivity.EXTRA_USER_NAME)
                    userName?.let {
                        intent.putExtra("user_name", it)
                    }
                }
                startActivity(intent)
                finish()
            }
        }

        // Botón Mi Colección
        findViewById<View>(R.id.menuCollection).setOnClickListener {
            highlightMenuItem(R.id.menuCollection)
            if (this !is CollectionActivity) {
                val intent = Intent(this, CollectionActivity::class.java)
                // Pasar el usuario si está disponible - ESTA ES LA PARTE CLAVE
                usuario?.let { user ->
                    intent.putExtra(WelcomeActivity.EXTRA_USUARIO, user)
                }
                // Pasar el nombre de usuario si está disponible
                if (this is CurrencyActivity) {
                    val userName = intent.getStringExtra(CurrencyActivity.EXTRA_USER_NAME)
                    userName?.let {
                        intent.putExtra("user_name", it)
                    }
                }
                startActivity(intent)
                finish()
            }
        }

        // Botón Divisas
        findViewById<View>(R.id.menuCurrency).setOnClickListener {
            highlightMenuItem(R.id.menuCurrency)
            if (this !is CurrencyActivity) {
                val intent = Intent(this, CurrencyActivity::class.java)
                // Pasar el usuario si está disponible
                usuario?.let { user ->
                    intent.putExtra(WelcomeActivity.EXTRA_USUARIO, user)
                }
                // Pasar el nombre de usuario si está disponible
                val userName = when (this) {
                    is WelcomeActivity -> intent.getStringExtra(WelcomeActivity.EXTRA_USER_NAME)
                    else -> null
                }
                userName?.let {
                    intent.putExtra(CurrencyActivity.EXTRA_USER_NAME, it)
                }
                startActivity(intent)
                finish()
            }
        }

        // Botón Marketplace
        findViewById<View>(R.id.menuMarketplace).setOnClickListener {
            highlightMenuItem(R.id.menuMarketplace)
            if (this !is MarketplaceActivity) {
                val intent = Intent(this, MarketplaceActivity::class.java)
                // Pasar el usuario si está disponible
                usuario?.let { user ->
                    intent.putExtra(WelcomeActivity.EXTRA_USUARIO, user)
                }
                // Pasar el nombre de usuario si está disponible
                if (this is CurrencyActivity) {
                    val userName = intent.getStringExtra(CurrencyActivity.EXTRA_USER_NAME)
                    userName?.let {
                        intent.putExtra("user_name", it)
                    }
                }
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
                    menuText.setTextColor(0xFF0000FF.toInt()) // Azul para seleccionado
                } else {
                    menuText.setTextColor(0xFF888888.toInt()) // Gris para no seleccionado
                }
            }
        }
    }
}
//>>BaseActivity.kt