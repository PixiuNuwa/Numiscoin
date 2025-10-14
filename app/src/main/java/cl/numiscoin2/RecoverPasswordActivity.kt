package cl.numiscoin2

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkUserUtils

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var sendButton: Button
    //private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }

        setContentView(R.layout.activity_recover_password)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        sendButton = findViewById(R.id.sendButton)
        //progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (validateInputs(email)) {
                showLoading(true)
                // Aquí llamarías a la función para enviar la solicitud al backend
                performPasswordRecovery(email)
            }
        }
    }

    private fun validateInputs(email: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresa tu email", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performPasswordRecovery(email: String) {
        NetworkUserUtils.recoverPassword(email) { success, message ->
            runOnUiThread {
                showLoading(false)
                if (success) {
                    Toast.makeText(this, "Solicitud de recuperación enviada. Revisa tu email.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        //progressBar.visibility = if (show) View.VISIBLE else View.GONE
        sendButton.isEnabled = !show
        emailEditText.isEnabled = !show
    }
}