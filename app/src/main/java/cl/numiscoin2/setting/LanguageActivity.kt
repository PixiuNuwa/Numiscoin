package cl.numiscoin2.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R

class LanguageActivity : BaseActivity() {

    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        initViews()
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }

    private fun initViews() {
        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        saveButton = findViewById(R.id.saveButton)

        // Configurar listener para guardar
        saveButton.setOnClickListener {
            saveLanguageSelection()
        }
    }

    private fun saveLanguageSelection() {
        val selectedId = languageRadioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona un idioma", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadioButton = findViewById<RadioButton>(selectedId)
        val selectedLanguage = selectedRadioButton.text.toString()

        // Aquí guardarías la preferencia de idioma
        Toast.makeText(this, "Idioma seleccionado: $selectedLanguage", Toast.LENGTH_SHORT).show()

        // Simular guardado de preferencias
        // SharedPreferencesManager.saveLanguage(selectedLanguage)

        finish()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LanguageActivity::class.java)
            context.startActivity(intent)
        }
    }
}