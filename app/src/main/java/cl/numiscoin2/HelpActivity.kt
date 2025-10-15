package cl.numiscoin2

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HelpActivity : BaseActivity() {
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
        setContentView(R.layout.activity_help)


        val description = findViewById<TextView>(R.id.helpDescription)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }


        description.text = "Aquí van a ir todos los manuales de monedas"

        // Lista de manuales
        val manualsList = listOf(
            "1. Guía_Básica_de_Identificación_de_Monedas.pdf",
            "2. Manual_de_Conservación_y_Almacenamiento_de_Monedas.pdf",
            "3. Catálogo de Monedas Históricas de América Latina.pdf",
            "4. Introducción a la Numismática: Términos y Conceptos.pdf",
            "5. Guía de Inversión en Monedas de Colección.pdf"
        )

        val manualsTextView = findViewById<TextView>(R.id.manualsList)
        manualsTextView.text = manualsList.joinToString("\n\n")

        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }
}