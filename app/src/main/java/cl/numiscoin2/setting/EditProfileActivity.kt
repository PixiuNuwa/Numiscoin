package cl.numiscoin2.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cl.numiscoin2.NetworkConfig
import cl.numiscoin2.NetworkUserUtils
import cl.numiscoin2.R
import cl.numiscoin2.SessionManager
import cl.numiscoin2.Usuario
import cl.numiscoin2.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class EditProfileActivity : AppCompatActivity() {

    private lateinit var usuario: Usuario
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        usuario = SessionManager.usuario ?: run {
            Toast.makeText(this, "Error: No se pudo cargar la información del usuario", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar vistas
        val profileAvatar = findViewById<ImageView>(R.id.editProfileAvatar)
        val editName = findViewById<EditText>(R.id.editProfileName)
        val editLastName = findViewById<EditText>(R.id.editProfileLastName)
        val editEmail = findViewById<EditText>(R.id.editProfileEmail)
        val changePhotoButton = findViewById<Button>(R.id.changePhotoButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val editPassword = findViewById<EditText>(R.id.editProfilePassword)
        val editConfirmPassword = findViewById<EditText>(R.id.editProfileConfirmPassword)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        // Cargar datos actuales
        editName.setText(usuario.nombre)
        editLastName.setText(usuario.apellido)
        editEmail.setText(usuario.email)

        // Cargar foto de perfil si existe
        cargarFotoPerfil(profileAvatar)

        // Botón para cambiar foto - Abrir galería
        changePhotoButton.setOnClickListener {
            abrirGaleria()
        }

        // Botón para cambiar contraseña (INDEPENDIENTE)
        changePasswordButton.setOnClickListener {
            val nuevaPassword = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (nuevaPassword.isEmpty()) {
                Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPassword != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevaPassword.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Actualizando contraseña...", Toast.LENGTH_SHORT).show()
            cambiarPasswordEnServidor(nuevaPassword)
        }

        // Botón para guardar cambios de perfil (SOLO nombre, apellido, email)
        saveButton.setOnClickListener {
            val nuevoNombre = editName.text.toString().trim()
            val nuevoApellido = editLastName.text.toString().trim()
            val nuevoEmail = editEmail.text.toString().trim()

            // Validaciones básicas
            if (nuevoNombre.isEmpty() || nuevoApellido.isEmpty() || nuevoEmail.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Actualizando perfil...", Toast.LENGTH_SHORT).show()
            actualizarUsuarioEnServidor(nuevoNombre, nuevoApellido, nuevoEmail)
        }

        // Botón para cancelar
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun cargarFotoPerfil(profileAvatar: ImageView) {
        if (usuario.foto.isNotEmpty() && usuario.foto != "null") {
            val fotoUrl = NetworkConfig.construirUrlCompleta(usuario.foto)
            Glide.with(this)
                .load(fotoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.drawable.ic_menu_gallery)
                .into(profileAvatar)
        } else {
            profileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                // Procesar la imagen (comprimir si es necesario)
                val processedUri = Utils.procesarImagen(this, uri)

                // Mostrar la imagen procesada
                val profileAvatar = findViewById<ImageView>(R.id.editProfileAvatar)
                Glide.with(this)
                    .load(processedUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(profileAvatar)

                // Subir la foto al servidor
                subirFotoAlServidor(processedUri)
            }
        }
    }

    private fun subirFotoAlServidor(fotoUri: Uri) {
        Toast.makeText(this, "Subiendo foto...", Toast.LENGTH_SHORT).show()

        NetworkUserUtils.uploadProfilePhoto(
            usuario.idUsuario,
            fotoUri,
            this
        ) { success, message ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(
                        this,
                        "Foto de perfil actualizada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Actualizar la foto en el objeto usuario localmente
                    usuario = usuario.copy(foto = "profile_${usuario.idUsuario}.jpg")
                } else {
                    Toast.makeText(this, "Error al subir foto: $message", Toast.LENGTH_SHORT).show()
                    // Revertir a la foto anterior si hay error
                    cargarFotoPerfil(findViewById(R.id.editProfileAvatar))
                }
            }
        }
    }

    private fun actualizarUsuarioEnServidor(nombre: String, apellido: String, email: String) {
        NetworkUserUtils.actualizarUsuario(
            usuario.idUsuario,
            nombre,
            apellido,
            email
        ) { success, message, usuarioActualizado ->
            runOnUiThread {
                if (success && usuarioActualizado != null) {
                    // Mantener la foto actualizada si se cambió
                    val usuarioFinal = if (usuarioActualizado.password == null) {
                        // Si el servidor no devuelve password, mantener el actual
                        usuarioActualizado.copy(
                            foto = usuario.foto,
                            password = usuario.password // ← Mantener el password actual
                        )
                    } else {
                        // Si el servidor devuelve password, usarlo
                        usuarioActualizado.copy(foto = usuario.foto)
                    }
                    SessionManager.usuario = usuarioFinal
                    finalizarActualizacion(usuarioFinal, "Perfil actualizado correctamente")
                } else {
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cambiarPasswordEnServidor(password: String) {
        NetworkUserUtils.cambiarPassword(
            usuario.idUsuario,
            password
        ) { success, message ->
            runOnUiThread {
                if (success) {
                    // Limpiar campos de contraseña
                    findViewById<EditText>(R.id.editProfilePassword).text.clear()
                    findViewById<EditText>(R.id.editProfileConfirmPassword).text.clear()
                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Error al cambiar contraseña: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun finalizarActualizacion(usuarioActualizado: Usuario, mensaje: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("usuarioActualizado", usuarioActualizado)
        setResult(Activity.RESULT_OK, resultIntent)
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        fun start(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, EditProfileActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
    }
}