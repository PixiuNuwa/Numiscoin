package cl.numiscoin2

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkDataUtils
import cl.numiscoin2.network.NetworkUserUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

        setContentView(R.layout.activity_login)

        SessionManager.init(this)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        createAccountButton = findViewById(R.id.createAccountButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                showLoading(true)
                performRealLogin(email, password)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            val intent = Intent(this@LoginActivity, RecoverPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performRealLogin(email: String, password: String) {
        NetworkUserUtils.performLogin(email, password) { success, message, usuario ->
            runOnUiThread {
                showLoading(false)
                if (success && usuario != null) {
                    // VALIDACIÓN DE VERSIÓN DEL SERVIDOR
                    if (validateServerVersion(usuario.serverVersion)) {
                        // Versión compatible, proceder con el login
                        SessionManager.login(usuario)
                        loadInitialData()
                        WelcomeActivity.start(this@LoginActivity)
                        finish()
                    } else {
                        // Versión incompatible, mostrar mensaje
                        Toast.makeText(
                            this,
                            "La versión del servidor es más nueva. Por favor, actualiza la aplicación.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // Error en el login
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Obtiene la versión de la aplicación desde el AndroidManifest
     * Esta es la forma MÁS CONFiable de obtener la versionName
     */
    private fun getAppVersionName(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("LoginActivity", "No se pudo obtener la información del package: ${e.message}")
            "1.0.0" // Valor por defecto
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error obteniendo versionName: ${e.message}")
            "1.0.0" // Valor por defecto
        }
    }

    private fun validateServerVersion(serverVersion: String?): Boolean {
        // Obtener la versión de la aplicación
        val appVersion = getAppVersionName()

        // Si el servidor no envía versión, asumimos que es compatible
        if (serverVersion.isNullOrEmpty()) {
            Log.w("LoginActivity", "El servidor no envió versión, se asume compatible")
            return true
        }

        Log.d("LoginActivity", "Versión app: $appVersion, Versión servidor: $serverVersion")

        // Comparar las versiones
        return compareVersions(appVersion, serverVersion)
    }

    private fun compareVersions(appVersion: String, serverVersion: String): Boolean {
        return try {
            // Normalizar las versiones (remover cualquier caracter no numérico o punto)
            val normalizedAppVersion = appVersion.replace("[^\\d.]".toRegex(), "")
            val normalizedServerVersion = serverVersion.replace("[^\\d.]".toRegex(), "")

            // Dividir las versiones en partes numéricas
            val appParts = normalizedAppVersion.split(".").map { it.toInt() }
            val serverParts = normalizedServerVersion.split(".").map { it.toInt() }

            // Comparar cada parte de la versión (major.minor.patch)
            for (i in 0 until minOf(appParts.size, serverParts.size)) {
                when {
                    appParts[i] > serverParts[i] -> {
                        Log.d("LoginActivity", "App más nueva que servidor - Permitir")
                        return true
                    }
                    appParts[i] < serverParts[i] -> {
                        Log.d("LoginActivity", "App más antigua que servidor - Bloquear")
                        return false
                    }
                    // Si son iguales, continuar con la siguiente parte
                }
            }

            // Si llegamos aquí, las versiones son iguales en las partes comparadas
            Log.d("LoginActivity", "Versiones iguales - Permitir")
            true
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error comparando versiones: ${e.message}")
            // En caso de error, permitir el acceso por seguridad
            true
        }
    }

    private fun loadInitialData() {
        // ... (mantén tu código existente aquí)
        if (!SessionManager.isPaisesCacheValid()) {
            NetworkDataUtils.getPaises { paises, error ->
                if (paises != null) {
                    SessionManager.savePaises(paises)
                    Log.d("LoginActivity", "Países cargados y guardados en caché")
                } else {
                    Log.e("LoginActivity", "Error cargando países: $error")
                }
            }
        }

        if (!SessionManager.isDivisasCacheValid()) {
            NetworkDataUtils.getDivisas { divisas, error ->
                if (divisas != null) {
                    SessionManager.saveDivisas(divisas)
                    Log.d("LoginActivity", "Divisas cargadas y guardadas en caché")
                } else {
                    Log.e("LoginActivity", "Error cargando divisas: $error")
                }
            }
        }

        if (!SessionManager.isMetalesCacheValid()) {
            NetworkDataUtils.getMetales { metales, error ->
                if (metales != null) {
                    SessionManager.saveMetales(metales)
                    Log.d("LoginActivity", "Metales cargados y guardados en caché")
                } else {
                    Log.e("LoginActivity", "Error cargando metales: $error")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        loginButton.isEnabled = !show
        createAccountButton.isEnabled = !show
    }
}