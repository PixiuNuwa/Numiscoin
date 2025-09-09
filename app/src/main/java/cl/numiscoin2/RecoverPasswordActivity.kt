package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        sendButton = findViewById(R.id.sendButton)
        progressBar = findViewById(R.id.progressBar)
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
        NetworkUtils.recoverPassword(email) { success, message ->
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
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        sendButton.isEnabled = !show
        emailEditText.isEnabled = !show
    }
}