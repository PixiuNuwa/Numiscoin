package cl.numiscoin2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cl.numiscoin2.network.NetworkCollectionUtils
import cl.numiscoin2.network.NetworkConfig
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ObjetosListActivity : BaseActivity() {

    private val TAG = "ObjetosListActivity"
    private var idColeccion: Int = 0
    private var nombreColeccion: String = ""
    private var paises: List<Pais> = emptyList()
    private var objetos: List<ObjetoColeccion> = emptyList()
    private var objetosFiltrados: List<ObjetoColeccion> = emptyList()
    private var paisSeleccionado: Pais? = null
    private var textoBusqueda: String = ""

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
        setContentView(R.layout.activity_objetos_list)

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)

        // Obtener parámetros del intent
        idColeccion = intent.getIntExtra("idColeccion", 0)
        nombreColeccion = intent.getStringExtra("nombreColeccion") ?: ""

        Log.d(TAG, "onCreate: idColeccion=$idColeccion, nombreColeccion=$nombreColeccion")

        // Configurar UI
        val title = findViewById<TextView>(R.id.title)
        title.text = nombreColeccion

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Configurar barra de búsqueda
        configurarBusqueda()

        // Configurar FAB para agregar monedas
        val fabAddCoin = findViewById<FloatingActionButton>(R.id.fabAddCoin)
        fabAddCoin.setOnClickListener {
            val usuarioActual = usuario
            if (usuarioActual == null) {
                Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this@ObjetosListActivity, AddCoinActivity::class.java)
            intent.putExtra("idColeccion", idColeccion)
            Log.d(TAG, "Enviando idColeccion: $idColeccion a AddCoinActivity")
            startActivity(intent)
        }

        // Ocultar secciones inicialmente
        ocultarTodasLasSecciones()

        // Cargar objetos de la colección para determinar si hay monedas
        cargarObjetosDeColeccion()
    }

    private fun configurarBusqueda() {
        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesario
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No necesario
            }

            override fun afterTextChanged(s: Editable?) {
                textoBusqueda = s.toString()
                aplicarFiltros()
            }
        })
    }

    private fun aplicarFiltros() {
        // Aplicar filtro de país primero
        val objetosFiltradosPorPais = if (paisSeleccionado != null) {
            objetos.filter { objeto ->
                objeto.idPais == paisSeleccionado!!.idPais
            }
        } else {
            objetos
        }

        // Luego aplicar filtro de búsqueda por nombre
        objetosFiltrados = if (textoBusqueda.isBlank()) {
            objetosFiltradosPorPais
        } else {
            objetosFiltradosPorPais.filter { objeto ->
                objeto.nombre.contains(textoBusqueda, ignoreCase = true)
            }
        }

        Log.d(TAG, "aplicarFiltros: ${objetosFiltrados.size} monedas después de aplicar filtros")
        mostrarObjetos()
    }

    private fun ocultarTodasLasSecciones() {
        findViewById<LinearLayout>(R.id.emptyStateContainer).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.headerWithObjects).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.filtrosContainer).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.objetosContainer).visibility = android.view.View.GONE
        findViewById<RecyclerView>(R.id.objetosRecyclerView).visibility = android.view.View.GONE
    }

    private fun cargarObjetosDeColeccion() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

        NetworkCollectionUtils.getCollectionObjects(idColeccion) { objetos, error ->
            runOnUiThread {
                progressBar.visibility = android.view.View.GONE

                if (error != null) {
                    Toast.makeText(this@ObjetosListActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                // Usar el operador safe call y elvis operator para manejar nulls
                val objetosList = objetos ?: emptyList()
                this.objetos = objetosList
                this.objetosFiltrados = objetosList // Inicialmente mostrar todos
                Log.d(TAG, "cargarObjetosDeColeccion: ${objetosList.size} objetos encontrados")

                if (objetosList.isEmpty()) {
                    mostrarEstadoVacio()
                } else {
                    // Hay objetos, cargar países para el filtro y mostrar la interfaz completa
                    cargarPaisesParaFiltro()
                }
            }
        }
    }

    private fun mostrarEstadoVacio() {
        val emptyStateContainer = findViewById<LinearLayout>(R.id.emptyStateContainer)
        val fabAddCoin = findViewById<FloatingActionButton>(R.id.fabAddCoin)

        emptyStateContainer.visibility = android.view.View.VISIBLE
        fabAddCoin.visibility = android.view.View.VISIBLE

        // Ocultar otras secciones
        findViewById<LinearLayout>(R.id.headerWithObjects).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.filtrosContainer).visibility = android.view.View.GONE
        findViewById<LinearLayout>(R.id.objetosContainer).visibility = android.view.View.GONE
    }

    private fun cargarPaisesParaFiltro() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

        // Usar idTipoObjeto = 1 para monedas
        NetworkCollectionUtils.getPaisesPorColeccionYTipo(idColeccion, 1) { paises, error ->
            runOnUiThread {
                progressBar.visibility = android.view.View.GONE

                if (error != null) {
                    Toast.makeText(this@ObjetosListActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                // Usar el operador safe call y elvis operator para manejar nulls
                val paisesList = paises ?: emptyList()
                this.paises = paisesList
                Log.d(TAG, "cargarPaisesParaFiltro: ${paisesList.size} países encontrados")

                mostrarInterfazCompleta()
            }
        }
    }

    private fun mostrarInterfazCompleta() {
        // Mostrar header con barra de búsqueda
        val headerWithObjects = findViewById<LinearLayout>(R.id.headerWithObjects)
        headerWithObjects.visibility = android.view.View.VISIBLE

        // Mostrar filtros de banderas si hay países
        if (paises.isNotEmpty()) {
            val filtrosContainer = findViewById<LinearLayout>(R.id.filtrosContainer)
            filtrosContainer.visibility = android.view.View.VISIBLE
            mostrarFiltrosBanderas()
        }

        // Mostrar listado de objetos
        val objetosContainer = findViewById<LinearLayout>(R.id.objetosContainer)
        objetosContainer.visibility = android.view.View.VISIBLE
        mostrarObjetos()

        // Mostrar FAB
        findViewById<FloatingActionButton>(R.id.fabAddCoin).visibility = android.view.View.VISIBLE

        // Ocultar estado vacío
        findViewById<LinearLayout>(R.id.emptyStateContainer).visibility = android.view.View.GONE
    }

    private fun mostrarFiltrosBanderas() {
        val filtrosContainer = findViewById<LinearLayout>(R.id.filtrosContainer)
        filtrosContainer.removeAllViews()

        // Verificar si la actividad está destruida
        if (isDestroyed || isFinishing) {
            Log.d(TAG, "Actividad destruida, no se pueden cargar imágenes")
            return
        }

        // Crear ScrollView horizontal para las banderas
        val horizontalScrollView = android.widget.HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val linearLayoutHorizontal = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Botón para limpiar filtro (mostrar todas las monedas)
        val clearFilterContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 0)
            }
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(this@ObjetosListActivity, R.color.menu_unselected))
        }

        val clearFilterIcon = TextView(this).apply {
            text = "❌"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 0)
            }
        }

        val clearFilterText = TextView(this).apply {
            text = "Todos"
            setTextColor(ContextCompat.getColor(this@ObjetosListActivity, android.R.color.white))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            gravity = Gravity.CENTER
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 8)
            }
        }

        clearFilterContainer.addView(clearFilterIcon)
        clearFilterContainer.addView(clearFilterText)

        // Hacer clickable para limpiar filtro
        clearFilterContainer.isClickable = true
        clearFilterContainer.setOnClickListener {
            limpiarFiltroPais()
            actualizarEstiloFiltros()
        }

        linearLayoutHorizontal.addView(clearFilterContainer)

        // Agregar banderas de países
        paises.forEach { pais ->
            val flagContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, resources.displayMetrics).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                gravity = Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(this@ObjetosListActivity, R.color.menu_unselected))
            }

            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt(),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics).toInt()
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
            }

            // Cargar bandera solo si la actividad no está destruida
            if (!isDestroyed && !isFinishing && !pais.foto.isNullOrEmpty()) {
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
            } else {
                // Cargar placeholder si no se puede cargar la imagen
                imageView.setImageResource(R.drawable.ic_placeholder)
            }

            val textView = TextView(this).apply {
                text = pais.nombre
                setTextColor(ContextCompat.getColor(this@ObjetosListActivity, android.R.color.white))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                gravity = Gravity.CENTER
                maxLines = 1
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 8)
                }
            }

            flagContainer.addView(imageView)
            flagContainer.addView(textView)

            // Hacer clickable para filtro
            flagContainer.isClickable = true
            flagContainer.setOnClickListener {
                // Verificar si la actividad sigue activa
                if (!isDestroyed && !isFinishing) {
                    filtrarPorPais(pais)
                    actualizarEstiloFiltros()
                }
            }

            linearLayoutHorizontal.addView(flagContainer)
        }

        horizontalScrollView.addView(linearLayoutHorizontal)
        filtrosContainer.addView(horizontalScrollView)
    }

    private fun filtrarPorPais(pais: Pais) {
        paisSeleccionado = pais
        aplicarFiltros()
        Log.d(TAG, "filtrarPorPais: Filtrado por país ${pais.nombre}")
    }

    private fun limpiarFiltroPais() {
        paisSeleccionado = null
        aplicarFiltros()
        Log.d(TAG, "limpiarFiltroPais: Filtro de país limpiado")
    }

    private fun actualizarEstiloFiltros() {
        val filtrosContainer = findViewById<LinearLayout>(R.id.filtrosContainer)

        // Obtener el HorizontalScrollView y luego el LinearLayout interno
        val horizontalScrollView = filtrosContainer.getChildAt(0) as? android.widget.HorizontalScrollView
        val linearLayoutHorizontal = horizontalScrollView?.getChildAt(0) as? LinearLayout

        if (linearLayoutHorizontal == null) {
            Log.e(TAG, "No se pudo encontrar el layout horizontal de filtros")
            return
        }

        // Recorrer todos los hijos del layout horizontal
        for (i in 0 until linearLayoutHorizontal.childCount) {
            val child = linearLayoutHorizontal.getChildAt(i)

            when (i) {
                0 -> {
                    // Primer hijo: botón "Limpiar filtro"
                    val clearFilterContainer = child as? LinearLayout
                    clearFilterContainer?.setBackgroundColor(
                        if (paisSeleccionado == null) {
                            ContextCompat.getColor(this, R.color.colorPrimary)
                        } else {
                            ContextCompat.getColor(this, R.color.menu_unselected)
                        }
                    )
                }
                else -> {
                    // Hijos restantes: banderas de países
                    val flagContainer = child as? LinearLayout
                    val paisIndex = i - 1 // -1 porque el primer elemento es "Limpiar filtro"

                    if (paisIndex < paises.size) {
                        val pais = paises[paisIndex]
                        flagContainer?.setBackgroundColor(
                            if (paisSeleccionado?.idPais == pais.idPais) {
                                ContextCompat.getColor(this, R.color.colorPrimary)
                            } else {
                                ContextCompat.getColor(this, R.color.menu_unselected)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun mostrarObjetos() {
        val objetosContainer = findViewById<LinearLayout>(R.id.objetosContainer)
        objetosContainer.removeAllViews()

        if (objetosFiltrados.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = when {
                    paisSeleccionado != null && textoBusqueda.isNotBlank() ->
                        "No hay monedas de ${paisSeleccionado?.nombre} que coincidan con '$textoBusqueda'"
                    paisSeleccionado != null ->
                        "No hay monedas de ${paisSeleccionado?.nombre} en esta colección"
                    textoBusqueda.isNotBlank() ->
                        "No hay monedas que coincidan con '$textoBusqueda'"
                    else ->
                        "No se encontraron objetos"
                }
                setTextColor(ContextCompat.getColor(this@ObjetosListActivity, android.R.color.white))
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setPadding(0, 32, 0, 32)
            }
            objetosContainer.addView(emptyText)
            return
        }

        objetosFiltrados.forEach { objeto ->
            val objetoItem = Button(this).apply {
                text = "${objeto.nombre}\n${objeto.descripcion ?: ""}"
                setTextColor(ContextCompat.getColor(this@ObjetosListActivity, android.R.color.white))
                setBackgroundColor(ContextCompat.getColor(this@ObjetosListActivity, R.color.menu_unselected))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }

                // Agregar OnClickListener para navegar a CoinDetailActivity
                setOnClickListener {
                    val intent = Intent(this@ObjetosListActivity, CoinDetailActivity::class.java)
                    intent.putExtra("moneda", objeto)
                    startActivity(intent)
                }
            }

            objetosContainer.addView(objetoItem)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando la actividad se reanude (por si se agregó una moneda)
        cargarObjetosDeColeccion()
    }
}