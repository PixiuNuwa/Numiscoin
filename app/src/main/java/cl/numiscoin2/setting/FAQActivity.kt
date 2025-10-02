package cl.numiscoin2.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ExpandableListView
import android.widget.Toast
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R

class FAQActivity : BaseActivity() {

    private lateinit var expandableListView: ExpandableListView
    private var faqList: MutableList<FAQ> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        initData()
        setupExpandableListView()
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }

    private fun initData() {
        faqList.add(FAQ(
            "¿Qué es NumisCoin?",
            "NumisCoin es una aplicación diseñada para coleccionistas de monedas que permite gestionar colecciones, calcular valores y conectarse con otros numismáticos."
        ))
        faqList.add(FAQ(
            "¿Cómo agrego monedas a mi colección?",
            "Puedes agregar monedas desde la sección 'Mi Colección' usando el botón '+' y completando la información requerida de cada moneda."
        ))
        faqList.add(FAQ(
            "¿La aplicación requiere conexión a internet?",
            "Algunas funciones como la sincronización de datos y el marketplace requieren conexión, pero puedes ver tu colección offline."
        ))
        faqList.add(FAQ(
            "¿Cómo cambio mi información de perfil?",
            "Ve a Ajustes → Perfil para modificar tu información personal, foto y preferencias."
        ))
        faqList.add(FAQ(
            "¿Puedo exportar mi colección?",
            "Sí, en Ajustes → Descargar copia de seguridad puedes exportar tu colección en formato XML."
        ))
        faqList.add(FAQ(
            "¿Cómo contacto con soporte?",
            "Puedes usar la sección 'Contacto' en Ajustes para encontrar nuestros canales de comunicación."
        ))
    }

    private fun setupExpandableListView() {
        expandableListView = findViewById(R.id.expandableListView)

        val adapter = FAQExpandableListAdapter(this, faqList)
        expandableListView.setAdapter(adapter)

        // Expandir el primer grupo por defecto
        expandableListView.expandGroup(0)
    }

    data class FAQ(
        val question: String,
        val answer: String
    )

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FAQActivity::class.java)
            context.startActivity(intent)
        }
    }
}