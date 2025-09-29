package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ObjetosListActivity : BaseActivity() {

    private val TAG = "ObjetosListActivity"
    private var idColeccion: Int = 0
    private var idTipoObjeto: Int = 0
    private var nombreTipo: String = ""
    private var paises: List<Pais> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetos_list)

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)

        // Obtener parámetros del intent
        idColeccion = intent.getIntExtra("idColeccion", 0)
        idTipoObjeto = intent.getIntExtra("idTipoObjeto", 0)
        nombreTipo = intent.getStringExtra("nombreTipo") ?: ""

        // Configurar UI
        val title = findViewById<TextView>(R.id.title)
        title.text = "Países: $nombreTipo"

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Ocultar RecyclerView y mostrar contenedor de países
        val recyclerView = findViewById<RecyclerView>(R.id.objetosRecyclerView)
        recyclerView.visibility = android.view.View.GONE

        // Cargar países de la colección y tipo
        cargarPaisesPorColeccionYTipo()
    }

    private fun cargarPaisesPorColeccionYTipo() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

        NetworkCollectionUtils.getPaisesPorColeccionYTipo(idColeccion, idTipoObjeto) { paises, error ->
            runOnUiThread {
                progressBar.visibility = android.view.View.GONE

                if (error != null) {
                    Toast.makeText(this@ObjetosListActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                this.paises = paises ?: emptyList()

                if (paises != null && paises.isNotEmpty()) {
                    mostrarPaises(paises)
                } else {
                    Toast.makeText(this@ObjetosListActivity, "No hay países para este tipo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarPaises(paises: List<Pais>) {
        val paisesContainer = findViewById<LinearLayout>(R.id.paisesContainer)
        paisesContainer.visibility = android.view.View.VISIBLE
        paisesContainer.removeAllViews()

        paises.forEach { pais ->
            // Crear contenedor principal para cada país
            val countryContainer = LinearLayout(this)
            countryContainer.orientation = LinearLayout.VERTICAL
            countryContainer.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 12, 16, 12)
            }
            countryContainer.gravity = Gravity.CENTER
            countryContainer.setBackgroundResource(R.drawable.bg_country_item)
            countryContainer.elevation = 4f

            // ImageView para la bandera (más grande - aprox. doble tamaño)
            val imageView = ImageView(this)
            val imageSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                160f,
                resources.displayMetrics
            ).toInt()

            imageView.layoutParams = LinearLayout.LayoutParams(
                imageSize,
                (imageSize * 0.6).toInt() // Proporción 3:2
            )
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true

            // Cargar bandera con Glide
            if (!pais.foto.isNullOrEmpty()) {
                val fotoUrl = if (pais.foto.startsWith("http")) {
                    pais.foto
                } else {
                    NetworkConfig.UPLOADS_BASE_URL + pais.foto
                }

                Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(imageView)
            }

            // TextView para el nombre del país
            val textView = TextView(this)
            textView.text = pais.nombre
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            textView.gravity = Gravity.CENTER
            textView.setPadding(0, 16, 0, 16)
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            countryContainer.addView(imageView)
            countryContainer.addView(textView)

            // Hacer clickable todo el container
            countryContainer.isClickable = true
            countryContainer.setOnClickListener {
                val intent = Intent(this@ObjetosListActivity, ObjetosPaisActivity::class.java)
                intent.putExtra("idColeccion", idColeccion)
                intent.putExtra("idTipoObjeto", idTipoObjeto)
                intent.putExtra("idPais", pais.idPais)
                intent.putExtra("nombreTipo", nombreTipo)
                intent.putExtra("nombrePais", pais.nombre)
                startActivity(intent)
            }

            paisesContainer.addView(countryContainer)
        }
    }
}

/*package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ObjetosListActivity : BaseActivity() {

    private val TAG = "ObjetosListActivity"
    private var idColeccion: Int = 0
    private var idTipoObjeto: Int = 0
    private var nombreTipo: String = ""
    private var paises: List<Pais> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetos_list)

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)

        // Obtener parámetros del intent
        idColeccion = intent.getIntExtra("idColeccion", 0)
        idTipoObjeto = intent.getIntExtra("idTipoObjeto", 0)
        nombreTipo = intent.getStringExtra("nombreTipo") ?: ""

        // Configurar UI
        val title = findViewById<TextView>(R.id.title)
        title.text = "Países: $nombreTipo"

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Ocultar RecyclerView y mostrar contenedor de países
        val recyclerView = findViewById<RecyclerView>(R.id.objetosRecyclerView)
        recyclerView.visibility = android.view.View.GONE

        // Cargar países de la colección y tipo
        cargarPaisesPorColeccionYTipo()
    }

    private fun cargarPaisesPorColeccionYTipo() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

        NetworkUtils.getPaisesPorColeccionYTipo(idColeccion, idTipoObjeto) { paises, error ->
            runOnUiThread {
                progressBar.visibility = android.view.View.GONE

                if (error != null) {
                    Toast.makeText(this@ObjetosListActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                this.paises = paises ?: emptyList()

                if (paises != null && paises.isNotEmpty()) {
                    mostrarPaises(paises)
                } else {
                    Toast.makeText(this@ObjetosListActivity, "No hay países para este tipo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarPaises(paises: List<Pais>) {
        val paisesContainer = findViewById<LinearLayout>(R.id.paisesContainer)
        paisesContainer.visibility = android.view.View.VISIBLE
        paisesContainer.removeAllViews()

        // Crear layout para grid de banderas
        val gridLayout = LinearLayout(this)
        gridLayout.orientation = LinearLayout.VERTICAL
        gridLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Crear filas para el grid (3 columnas)
        var currentRow: LinearLayout? = null

        paises.forEachIndexed { index, pais ->
            if (index % 3 == 0) {
                currentRow = LinearLayout(this)
                currentRow!!.orientation = LinearLayout.HORIZONTAL
                currentRow!!.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gridLayout.addView(currentRow)
            }

            // Crear botón con bandera
            val buttonLayout = LinearLayout(this)
            buttonLayout.orientation = LinearLayout.VERTICAL
            buttonLayout.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            buttonLayout.gravity = android.view.Gravity.CENTER

            // ImageView para la bandera
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(
                80,
                60
            )
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER

            // Cargar bandera con Glide
            if (!pais.foto.isNullOrEmpty()) {
                val fotoUrl = if (pais.foto.startsWith("http")) {
                    pais.foto
                } else {
                    NetworkUtils.UPLOADS_BASE_URL + pais.foto
                }

                Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageView)
            }

            // TextView para el nombre del país
            val textView = TextView(this)
            textView.text = pais.nombre
            textView.setTextColor(resources.getColor(android.R.color.white))
            textView.textSize = 12f
            textView.gravity = android.view.Gravity.CENTER
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            buttonLayout.addView(imageView)
            buttonLayout.addView(textView)

            // Hacer clickable todo el layout
            buttonLayout.isClickable = true
            buttonLayout.setOnClickListener {
                val intent = Intent(this@ObjetosListActivity, ObjetosPaisActivity::class.java)
                intent.putExtra("idColeccion", idColeccion)
                intent.putExtra("idTipoObjeto", idTipoObjeto)
                intent.putExtra("idPais", pais.idPais)
                intent.putExtra("nombreTipo", nombreTipo)
                intent.putExtra("nombrePais", pais.nombre)
                startActivity(intent)
            }

            currentRow!!.addView(buttonLayout)
        }

        paisesContainer.addView(gridLayout)
    }
}*/