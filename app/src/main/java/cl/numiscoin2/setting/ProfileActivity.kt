package cl.numiscoin2.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.numiscoin2.HelpActivity
import cl.numiscoin2.LoginActivity
import cl.numiscoin2.network.NetworkConfig
import cl.numiscoin2.R
import cl.numiscoin2.SessionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileAvatar: ImageView
    private lateinit var profileFullName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileName: TextView
    private lateinit var profileLastName: TextView

    //private var usuario: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        //
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<Button>(R.id.backButton)
        val helpButton = findViewById<Button>(R.id.helpButton)
        val editButton = findViewById<Button>(R.id.editButton)

        // Inicializar las vistas usando las propiedades de la clase
        profileAvatar = findViewById(R.id.profileAvatar)
        profileFullName = findViewById(R.id.profileName)
        profileEmail = findViewById(R.id.profileEmail)
        profileName = findViewById(R.id.profileName)
        profileLastName = findViewById(R.id.profileLastName)


        cargarDatosUsuario()

        backButton.setOnClickListener {
            finish()
        }

        helpButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        editButton.setOnClickListener {
            EditProfileActivity.start(this, 1)
        }


    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ProfileActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Actualizar datos desde SessionManager (que ya fue actualizado por EditProfileActivity)
            cargarDatosUsuario()
            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
        }

    }

    private fun cargarDatosUsuario() {
        // Obtener usuario de SessionManager cada vez
        val usuario = SessionManager.usuario

        if (usuario != null) {
            // Mostrar datos del usuario
            profileFullName.text = "${usuario.nombre} ${usuario.apellido}"
            profileEmail.text = "Nombre: ${usuario.email}"
            profileName.text = "Nombre: ${usuario.nombre}"
            profileLastName.text = "Apellido: ${usuario.apellido}"

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)

            // Cargar foto de perfil si existe
            if (usuario.foto.isNotEmpty() && usuario.foto != "null") {
                val fotoUrl = NetworkConfig.construirUrlCompleta(usuario.foto)
                Log.d("ProfileActivity", "URL completa de la foto: $fotoUrl")

                try {
                    Glide.with(this)
                        .load(fotoUrl)
                        .apply(requestOptions)
                        .placeholder(android.R.color.darker_gray)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(profileAvatar)
                } catch (e: Exception) {
                    Log.e("ProfileActivity", "Error cargando imagen con Glide: ${e.message}")
                    profileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                Log.d("ProfileActivity", "No hay foto de perfil o está vacía")
                profileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            Log.w("ProfileActivity", "No hay usuario en SessionManager")
            // Redirigir al login si no hay usuario
            redirigirAlLogin()
        }
    }

    private fun redirigirAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}