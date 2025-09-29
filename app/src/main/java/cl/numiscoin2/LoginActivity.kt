package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                // Llamada real al servidor
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
            //Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@LoginActivity, RecoverPasswordActivity::class.java)
            startActivity(intent)
        }



        findViewById<Button>(R.id.googleButton).setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRealLogin(email: String, password: String) {
        NetworkUserUtils.performLogin(email, password) { success, message, usuario ->
            runOnUiThread {
                showLoading(false)
                if (success && usuario != null) {
                    SessionManager.login(usuario)
                    loadInitialData()
                    WelcomeActivity.start(this@LoginActivity)
                    finish()
                } else {
                    // Error en el login
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadInitialData() {
        // Cargar paises si no están en caché o son inválidos
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

        // Cargar divisas si no están en caché o son inválidas
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

        // Cargar metales si no están en caché o son inválidos
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
