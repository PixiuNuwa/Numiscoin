package cl.numiscoin2.setting

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R

class FAQActivity : BaseActivity() {

    private lateinit var expandableListView: ExpandableListView
    private var faqList: MutableList<FAQ> = mutableListOf()

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
        setContentView(R.layout.activity_faq)

        initData()
        setupExpandableListView()

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)
    }

    private fun initData() {
        faqList.add(FAQ(
            "¿Quienes pueden ver mis colecciones?",
            "Por defecto, solo tú puedes ver tus colecciones. Tu privacidad es nuestra prioridad. Si en algún momento deseas compartir los items de tu colección, puedes hacerlo fácilmente utilizando la función de compartir dentro de la App."
        ))
        faqList.add(FAQ(
            "¿Cuantas fotos puedo subir?",
            "Tu aplicación tiene 3 estatus silver, gold y platinoum; y según ese estatus es la cantidad de fotos que puedes subir."
        ))
        faqList.add(FAQ(
            "¿Puedo sólo incorporar monedas?",
            "No, tu aplicación pronto te permitirá coleccionar medallas, billetes y otras antiguedades, te estaremos avisando."
        ))
        faqList.add(FAQ(
            "¿Si tengo dudas luego de haber adquirido la aplicación,  como obtendré ayuda?",
            "No te preocupes en contacto siempre tendrás alguien que te ayude con el uso de la misma, pero además contarás con nuestro tutorial."
        ))
        faqList.add(FAQ(
            "¿Pregunta 5?",
            "bla bla bla yadda yadda."
        ))
        faqList.add(FAQ(
            "¿Pregunta 6?",
            "bla bla bla yadda yadda."
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