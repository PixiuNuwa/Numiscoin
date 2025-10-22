package cl.numiscoin2.setting

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.MembershipActivity
import cl.numiscoin2.network.NetworkConfig
import cl.numiscoin2.R
import cl.numiscoin2.WelcomeActivity
import com.bumptech.glide.Glide

class SettingsActivity : BaseActivity() {

    private lateinit var userPhoto: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView

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
        setContentView(R.layout.activity_settings)

        setupUI()

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        setupBottomMenu()
        highlightMenuItem(R.id.menuHome) // O el menú que corresponda
    }

    private fun setupUI() {
        userPhoto = findViewById(R.id.userPhoto)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)

        // Configurar datos del usuario
        usuario?.let { user ->
            // Foto de perfil
            if (user.foto.isNotEmpty()) {
                val fotoUrl = NetworkConfig.construirUrlCompleta(user.foto)
                Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.circle_white_background)
                    .circleCrop()
                    .into(userPhoto)
            }

            // Nombre y email
            userName.text = "${user.nombre} ${user.apellido}"
            userEmail.text = user.email
        }

        // Configurar botones
        setupButton(R.id.profileButton, ProfileActivity::class.java)
        setupButton(R.id.homeButton, WelcomeActivity::class.java)
        //setupButton(R.id.languageButton, LanguageActivity::class.java)
        setupButton(R.id.faqButton, FAQActivity::class.java)
        setupButton(R.id.contactButton, ContactActivity::class.java)
        setupButton(R.id.termsButton, TermsActivity::class.java)
        setupButton(R.id.membershipButton, MembershipActivity::class.java)
        //setupButton(R.id.backupButton, BackupActivity::class.java)
    }

    /*private fun setupButton(buttonId: Int, activityClass: Class<*>) {
        findViewById<TextView>(buttonId).setOnClickListener {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        }
    }*/
    private fun setupButton(buttonId: Int, activityClass: Class<*>) {
        findViewById<View>(buttonId).setOnClickListener {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}