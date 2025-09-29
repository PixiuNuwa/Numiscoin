package cl.numiscoin2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var createAccountButton: Button
    private lateinit var selectPhotoButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        nameEditText = findViewById(R.id.nameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        createAccountButton = findViewById(R.id.createAccountButton)
        selectPhotoButton = findViewById(R.id.selectPhotoButton)
        profileImageView = findViewById(R.id.profileImageView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        selectPhotoButton.setOnClickListener {
            openImagePicker()
        }

        createAccountButton.setOnClickListener {
            val nombre = nameEditText.text.toString()
            val apellido = lastNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateInputs(nombre, apellido, email, password)) {
                showLoading(true)
                registerUser(nombre, apellido, email, password)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun validateInputs(nombre: String, apellido: String, email: String, password: String): Boolean {
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser(nombre: String, apellido: String, email: String, password: String) {
        NetworkUserUtils.performRegister(nombre, apellido, email, password) { success, message, usuario ->
            runOnUiThread {
                if (success && usuario != null) {
                    goToMembershipActivity(usuario.idUsuario)
                    // Si se creó el usuario exitosamente y hay foto seleccionada, subir la foto
                    /*if (selectedImageUri != null && usuario.idUsuario > 0) {
                        uploadProfilePhoto(usuario.idUsuario)
                    } else {
                        showLoading(false)
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                        finish() // Regresar a la pantalla de login
                    }*/
                } else {
                    showLoading(false)
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToMembershipActivity(userId: Long) {
        showLoading(false)
        Toast.makeText(this@RegisterActivity, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MembershipActivity::class.java).apply {
            putExtra("USER_ID", userId)
            // Puedes agregar más datos del usuario si es necesario
            putExtra("USER_EMAIL", emailEditText.text.toString())
        }

        // Limpiar la pila de actividades y empezar nueva sesión
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun uploadProfilePhoto(idUsuario: Long) {
        selectedImageUri?.let { uri ->
            NetworkUserUtils.uploadProfilePhoto(idUsuario, uri, this) { success, message ->
                runOnUiThread {
                    showLoading(false)
                    if (success) {
                        Toast.makeText(this@RegisterActivity, "Cuenta creada y foto de perfil actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Cuenta creada pero error al subir foto: $message", Toast.LENGTH_LONG).show()
                    }
                    finish() // Regresar a la pantalla de login
                }
            }
        } ?: run {
            showLoading(false)
            Toast.makeText(this@RegisterActivity, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        createAccountButton.isEnabled = !show
        selectPhotoButton.isEnabled = !show
    }
}