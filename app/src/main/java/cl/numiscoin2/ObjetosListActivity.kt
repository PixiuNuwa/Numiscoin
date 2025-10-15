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
import android.widget.ImageButton
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
    private var idColeccion: Long = 0L
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
        idColeccion = intent.getLongExtra("idColeccion", 0L)
        nombreColeccion = intent.getStringExtra("nombreColeccion") ?: ""

        Log.d(TAG, "onCreate: idColeccion=$idColeccion, nombreColeccion=$nombreColeccion")

        // Validar que el idColeccion sea válido
        if (idColeccion <= 0L) {
            Log.e(TAG, "onCreate: ID de colección no válido: $idColeccion")
            Toast.makeText(this, "Error: Colección no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // Configurar UI
        val title = findViewById<TextView>(R.id.title)
        title.text = nombreColeccion

        /*val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }*/
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        // Configurar barra de búsqueda
        configurarBusqueda()

        // Configurar FAB para agregar monedas
        val fabAddCoin = findViewById<TextView>(R.id.fabAddCoin)
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

        val idColeccionInt = idColeccion.toInt()

        NetworkCollectionUtils.getCollectionObjects(idColeccionInt) { objetos, error ->
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
        val fabAddCoin = findViewById<TextView>(R.id.fabAddCoin)

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

        val idColeccionInt = idColeccion.toInt()
        // Usar idTipoObjeto = 1 para monedas
        NetworkCollectionUtils.getPaisesPorColeccionYTipo(idColeccionInt, 1) { paises, error ->
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
        findViewById<TextView>(R.id.fabAddCoin).visibility = android.view.View.VISIBLE

        // Ocultar estado vacío
        findViewById<LinearLayout>(R.id.emptyStateContainer).visibility = android.view.View.GONE
    }

    private fun mostrarFiltrosBanderas() {
        val filtrosContainer = findViewById<LinearLayout>(R.id.filtrosContainer)
        filtrosContainer.removeAllViews()

        if (isDestroyed || isFinishing) {
            Log.d(TAG, "Actividad destruida, no se pueden cargar imágenes")
            return
        }

        val horizontalScrollView = android.widget.HorizontalScrollView(this)
        val linearLayoutHorizontal = LinearLayout(this)
        linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL

        // Botón para limpiar filtro
        val clearFilterView = crearCountryItemView("Todos", null, true)
        linearLayoutHorizontal.addView(clearFilterView)

        // Agregar banderas de países
        paises.forEach { pais ->
            val countryView = crearCountryItemView(pais.nombre, pais.foto, false)
            countryView.setOnClickListener {
                if (!isDestroyed && !isFinishing) {
                    filtrarPorPais(pais)
                    actualizarEstiloFiltros()
                }
            }
            linearLayoutHorizontal.addView(countryView)
        }

        horizontalScrollView.addView(linearLayoutHorizontal)
        filtrosContainer.addView(horizontalScrollView)
        actualizarEstiloFiltros()
    }

    private fun crearCountryItemView(nombre: String, fotoUrl: String?, esBotonTodos: Boolean): LinearLayout {
        val view = layoutInflater.inflate(R.layout.country_item, null) as LinearLayout
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(4, 0, 4, 0) }

        val countryFlag = view.findViewById<ImageView>(R.id.countryFlag)
        val countryName = view.findViewById<TextView>(R.id.countryName)

        countryName.text = nombre

        if (esBotonTodos) {
            countryFlag.setImageResource(android.R.drawable.ic_delete)
        } else if (!fotoUrl.isNullOrEmpty()) {
            val urlCompleta = NetworkConfig.construirUrlCompleta(fotoUrl)
            Glide.with(this)
                .load(urlCompleta)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(countryFlag)
        } else {
            countryFlag.setImageResource(R.drawable.ic_placeholder)
        }

        view.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        view.isClickable = true

        if (esBotonTodos) {
            view.setOnClickListener {
                limpiarFiltroPais()
                actualizarEstiloFiltros()
            }
        }

        return view
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
            val child = linearLayoutHorizontal.getChildAt(i) as? LinearLayout

            when (i) {
                0 -> {
                    // Primer hijo: botón "Limpiar filtro"
                    if(paisSeleccionado==null) {
                        child?.setBackgroundResource(R.drawable.btn_white_square)
                    } else {
                        child?.setBackgroundResource(R.drawable.btn_transparent_square)
                    }
                    /*child?.setBackgroundColor(
                        if (paisSeleccionado == null) {
                            ContextCompat.getColor(this, R.color.colorPrimary) // Color cuando está seleccionado
                        } else {
                            ContextCompat.getColor(this, R.color.transparent) // Color normal
                        }
                    )*/
                }
                else -> {
                    // Hijos restantes: banderas de países
                    val paisIndex = i - 1 // -1 porque el primer elemento es "Limpiar filtro"

                    if (paisIndex < paises.size) {
                        val pais = paises[paisIndex]
                        if(paisSeleccionado?.idPais == pais.idPais) {
                            child?.setBackgroundResource(R.drawable.btn_white_square)
                        } else {
                            child?.setBackgroundResource(R.drawable.btn_transparent_square)
                        }
                        /*child?.setBackgroundColor(
                            if (paisSeleccionado?.idPais == pais.idPais) {
                                ContextCompat.getColor(this, R.color.colorPrimary) // Color cuando está seleccionado
                            } else {
                                ContextCompat.getColor(this, R.color.transparent) // Color normal
                            }
                        )*/
                    }
                }
            }
        }
    }

    /*private fun mostrarObjetos() {
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
    }*/
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
            // Inflar el layout personalizado para cada moneda
            val monedaItemView = layoutInflater.inflate(R.layout.moneda_item, objetosContainer, false)

            // Configurar los textos
            val tvMonedaNombre = monedaItemView.findViewById<TextView>(R.id.tvMonedaNombre)
            val tvMonedaDescripcion = monedaItemView.findViewById<TextView>(R.id.tvMonedaDescripcion)
            val ivMoneda = monedaItemView.findViewById<ImageView>(R.id.ivMoneda)

            tvMonedaNombre.text = objeto.nombre
            tvMonedaDescripcion.text = objeto.descripcion ?: "Sin descripción"

            // Cargar la primera foto de la moneda si existe
            if (!objeto.fotos.isNullOrEmpty()) {
                val primeraFoto = objeto.fotos[0]
                val fotoUrl = NetworkConfig.construirUrlCompleta(primeraFoto.url)

                Log.d("ObjetosListActivity","esta es la url de la foto: ${fotoUrl}")
                Glide.with(this)
                    .load(fotoUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(ivMoneda)
            } else {
                // Si no hay fotos, usar placeholder
                ivMoneda.setImageResource(R.drawable.ic_placeholder)
            }

            // Configurar el clic en el item completo (para abrir los detalles de la moneda)
            monedaItemView.setOnClickListener {
                val intent = Intent(this@ObjetosListActivity, CoinDetailActivity::class.java)
                intent.putExtra("moneda", objeto)
                startActivity(intent)
            }

            objetosContainer.addView(monedaItemView)
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando la actividad se reanude (por si se agregó una moneda)
        cargarObjetosDeColeccion()
    }
}