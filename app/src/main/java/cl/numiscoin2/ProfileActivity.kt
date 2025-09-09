package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val helpButton = findViewById<Button>(R.id.helpButton)
        val profileAvatar = findViewById<ImageView>(R.id.profileAvatar)
        val profileName = findViewById<TextView>(R.id.profileName)
        val profileEmail = findViewById<TextView>(R.id.profileEmail)
        val profileId = findViewById<TextView>(R.id.profileId)
        val profileDate = findViewById<TextView>(R.id.profileDate)

        // Obtener usuario de la intent
        val usuario = intent.getParcelableExtra<Usuario>("usuario")

        if (usuario != null) {
            // Mostrar datos del usuario
            profileName.text = "${usuario.nombre} ${usuario.apellido}"
            profileEmail.text = usuario.email
            profileId.text = "ID: ${usuario.idUsuario}"
            profileDate.text = "Miembro desde: ${usuario.fechaCreacion}"

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)

            // Cargar foto de perfil si existe
            if (usuario.foto.isNotEmpty() && usuario.foto != "null") {
                val fotoUrl = NetworkUtils.construirUrlCompleta(usuario.foto)
                Log.d("OnCreate", "URL completa de la foto: $fotoUrl")

                try {
                    Glide.with(this)
                        .load(fotoUrl)
                        .apply(requestOptions)
                        .placeholder(android.R.color.darker_gray)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(profileAvatar)
                } catch (e: Exception) {
                    Log.e("OnCreate", "Error cargando imagen con Glide: ${e.message}")
                    // Fallback a imagen por defecto
                    profileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                Log.d("OnCreate", "No hay foto de perfil o está vacía")
                // Usar un placeholder más visible
                profileAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        helpButton.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        fun start(context: Context, usuario: Usuario) {
            val intent = Intent(context, ProfileActivity::class.java).apply {
                putExtra("usuario", usuario)
            }
            context.startActivity(intent)
        }
    }
}