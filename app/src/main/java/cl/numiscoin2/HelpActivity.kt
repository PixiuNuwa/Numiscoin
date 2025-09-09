package cl.numiscoin2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val title = findViewById<TextView>(R.id.helpTitle)
        val description = findViewById<TextView>(R.id.helpDescription)
        val backButton = findViewById<Button>(R.id.backButtonHelp)

        title.text = "Ayuda"
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

        backButton.setOnClickListener {
            finish()
        }
    }
}