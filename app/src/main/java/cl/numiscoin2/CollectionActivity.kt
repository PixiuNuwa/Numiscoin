package cl.numiscoin2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import cl.numiscoin2.network.NetworkCollectionUtils
import cl.numiscoin2.network.NetworkObjectUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale

class CollectionActivity : BaseActivity() {

    private val TAG = "CollectionActivity"
    private var isLoading = false
    private var colecciones: List<Coleccion> = emptyList()
    private var coleccionesFiltradas: List<Coleccion> = emptyList()
    private val REQUEST_CREATE_COLLECTION = 100
    private val REQUEST_EDIT_COLLECTION = 101 // Nuevo código para edición
    private lateinit var totalColeccionValor: TextView
    private lateinit var totalItemsCount: TextView
    private lateinit var totalGastadoValor: TextView

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
        setContentView(R.layout.activity_collection)
        Log.d(TAG, "onCreate: CollectionActivity creada")

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)
        Log.d(TAG, "onCreate: Menú inferior configurado")

        // Configurar barra de búsqueda
        configurarBusqueda()

        inicializarVistasTotales()

        cargarTotalesUsuario()

        // Cargar colecciones del usuario
        cargarColeccionesDesdeServidor()

        val fabAddCollection = findViewById<TextView>(R.id.fabAddCollection)
        fabAddCollection.setOnClickListener {
            Log.d(TAG, "fabAddCollection: Abriendo vista para crear colección")
            val intent = Intent(this@CollectionActivity, CreateCollectionActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_COLLECTION)
        }
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
                filtrarColecciones(s.toString())
            }
        })
    }

    private fun filtrarColecciones(textoBusqueda: String) {
        if (textoBusqueda.isBlank()) {
            // Si no hay texto de búsqueda, mostrar todas las colecciones
            coleccionesFiltradas = colecciones
        } else {
            // Filtrar colecciones por nombre (ignorando mayúsculas/minúsculas)
            coleccionesFiltradas = colecciones.filter { coleccion ->
                coleccion.nombre.contains(textoBusqueda, ignoreCase = true)
            }
        }
        mostrarColeccionesFiltradas()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CREATE_COLLECTION -> {
                when (resultCode) {
                    RESULT_OK -> {
                        val idColeccion = data?.getLongExtra("id_coleccion", -1L) ?: -1L
                        val nombreColeccion = data?.getStringExtra("nombre_coleccion") ?: ""

                        Log.d(TAG, "Colección creada exitosamente - ID: $idColeccion, Nombre: $nombreColeccion")
                        Toast.makeText(this, "Colección '$nombreColeccion' creada exitosamente", Toast.LENGTH_SHORT).show()
                        cargarColeccionesDesdeServidor()
                    }
                    RESULT_CANCELED -> {
                        // El usuario canceló la creación
                        Log.d(TAG, "Creación de colección cancelada por el usuario")
                    }
                }
            }
            REQUEST_EDIT_COLLECTION -> {
                when (resultCode) {
                    RESULT_OK -> {
                        val coleccionEditada = data?.getBooleanExtra("coleccion_editada", false) ?: false
                        if (coleccionEditada) {
                            Log.d(TAG, "Colección editada exitosamente")
                            Toast.makeText(this, "Colección actualizada exitosamente", Toast.LENGTH_SHORT).show()
                            cargarColeccionesDesdeServidor()
                        }
                    }
                    RESULT_CANCELED -> {
                        // El usuario canceló la edición
                        Log.d(TAG, "Edición de colección cancelada por el usuario")
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        isLoading = show
        Log.d(TAG, "showLoading: ${if (show) "Mostrando" else "Ocultando"} loading")
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val headerWithCollections = findViewById<androidx.cardview.widget.CardView>(R.id.headerWithCollections)
        val collectionsContainer = findViewById<LinearLayout>(R.id.collectionsContainer)
        val emptyStateContainer = findViewById<LinearLayout>(R.id.emptyStateContainer)

        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        if (show) {
            headerWithCollections.visibility = android.view.View.GONE
            collectionsContainer.visibility = android.view.View.GONE
            emptyStateContainer.visibility = android.view.View.GONE
        }
    }

    private fun mostrarColecciones() {
        Log.d(TAG, "mostrarColecciones: Mostrando ${colecciones.size} colecciones")

        // Inicializar la lista filtrada con todas las colecciones
        coleccionesFiltradas = colecciones
        mostrarColeccionesFiltradas()
    }

    private fun mostrarColeccionesFiltradas() {
        Log.d(TAG, "mostrarColeccionesFiltradas: Mostrando ${coleccionesFiltradas.size} colecciones filtradas")

        val headerWithCollections = findViewById<androidx.cardview.widget.CardView>(R.id.headerWithCollections)
        val collectionsContainer = findViewById<LinearLayout>(R.id.collectionsContainer)
        val emptyStateContainer = findViewById<LinearLayout>(R.id.emptyStateContainer)
        val searchEmptyStateContainer = findViewById<LinearLayout>(R.id.searchEmptyStateContainer)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        collectionsContainer.removeAllViews()

        if (coleccionesFiltradas.isEmpty()) {
            val isSearching = etSearch.text.toString().isNotBlank()

            if (isSearching) {
                // Búsqueda sin resultados - mostrar solo mensaje de búsqueda vacía
                headerWithCollections.visibility = android.view.View.VISIBLE
                collectionsContainer.visibility = android.view.View.GONE
                emptyStateContainer.visibility = android.view.View.GONE
                searchEmptyStateContainer.visibility = android.view.View.VISIBLE
            } else {
                // Estado inicial sin colecciones - mostrar estado vacío completo
                headerWithCollections.visibility = android.view.View.GONE
                collectionsContainer.visibility = android.view.View.GONE
                emptyStateContainer.visibility = android.view.View.VISIBLE
                searchEmptyStateContainer.visibility = android.view.View.GONE
            }
        } else {
            // Hay colecciones para mostrar
            headerWithCollections.visibility = android.view.View.VISIBLE
            collectionsContainer.visibility = android.view.View.VISIBLE
            emptyStateContainer.visibility = android.view.View.GONE
            searchEmptyStateContainer.visibility = android.view.View.GONE

            coleccionesFiltradas.forEach { coleccion ->
                // Inflar el layout personalizado para cada colección
                val collectionItemView = layoutInflater.inflate(R.layout.collection_item, collectionsContainer, false)

                // Configurar los textos
                val tvCollectionName = collectionItemView.findViewById<TextView>(R.id.tvCollectionName)
                val tvCollectionDescription = collectionItemView.findViewById<TextView>(R.id.tvCollectionDescription)
                val btnEdit = collectionItemView.findViewById<ImageButton>(R.id.btnEdit)
                val btnDelete = collectionItemView.findViewById<ImageButton>(R.id.btnDelete)

                tvCollectionName.text = coleccion.nombre
                tvCollectionDescription.text = coleccion.descripcion

                // Configurar el clic en el item completo (para abrir la colección)
                collectionItemView.setOnClickListener {
                    val intent = Intent(this@CollectionActivity, ObjetosListActivity::class.java)
                    intent.putExtra("idColeccion", coleccion.id)
                    intent.putExtra("nombreColeccion", coleccion.nombre)
                    startActivity(intent)
                }

                // Configurar botón Editar
                btnEdit.setOnClickListener {
                    Log.d(TAG, "Botón editar clickeado para colección ID: ${coleccion.id}")
                    // Abrir actividad de edición
                    val intent = Intent(this@CollectionActivity, EditCollectionActivity::class.java)
                    intent.putExtra("idColeccion", coleccion.id)
                    intent.putExtra("nombreColeccion", coleccion.nombre)
                    intent.putExtra("descripcionColeccion", coleccion.descripcion)
                    startActivityForResult(intent, REQUEST_EDIT_COLLECTION)
                }

                // Configurar botón Eliminar
                btnDelete.setOnClickListener {
                    Log.d(TAG, "Botón eliminar clickeado para colección ID: ${coleccion.id}")
                    // Llamar al método para eliminar la colección
                    eliminarColeccion(coleccion)
                }

                collectionsContainer.addView(collectionItemView)
            }
        }
    }

    private fun eliminarColeccion(coleccion: Coleccion) {
        Log.d(TAG, "eliminarColeccion: Iniciando eliminación de colección ID: ${coleccion.id}")

        // Mostrar diálogo de confirmación
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Colección")
            .setMessage("¿Estás seguro de que quieres eliminar la colección '${coleccion.nombre}'?")
            .setPositiveButton("Eliminar") { dialog, which ->
                // Llamar al servicio para eliminar la colección
                NetworkCollectionUtils.deleteCollection(coleccion.id) { success, error, cantidadObjetos ->
                    runOnUiThread {
                        if (success) {
                            Log.d(TAG, "eliminarColeccion: Colección eliminada exitosamente")
                            Toast.makeText(
                                this@CollectionActivity,
                                "Colección '${coleccion.nombre}' eliminada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Recargar la lista de colecciones
                            cargarColeccionesDesdeServidor()
                        } else {
                            if (cantidadObjetos != null && cantidadObjetos > 0) {
                                // Error porque la colección tiene objetos
                                Log.w(TAG, "eliminarColeccion: La colección tiene $cantidadObjetos objetos, no se puede eliminar")
                                Toast.makeText(
                                    this@CollectionActivity,
                                    "No se puede eliminar la colección porque contiene $cantidadObjetos objeto(s). Debe eliminar o mover todos los objetos primero.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Otro tipo de error
                                Log.e(TAG, "eliminarColeccion: Error al eliminar colección - $error")
                                Toast.makeText(
                                    this@CollectionActivity,
                                    "Error al eliminar la colección: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarColeccionesDesdeServidor() {
        showLoading(true)
        Log.d(TAG, "cargarColeccionesDesdeServidor: Iniciando carga de colecciones")

        val usuarioId = usuario?.idUsuario ?: 0L

        if (usuarioId == 0L) {
            Log.e(TAG, "cargarColeccionesDesdeServidor: Usuario no válido")
            showLoading(false)
            Toast.makeText(this, "Error al identificar el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener colecciones del usuario
        NetworkCollectionUtils.getUserCollections(usuario!!.idUsuario) { colecciones, error ->
            runOnUiThread {
                showLoading(false)

                if (error != null) {
                    Toast.makeText(this@CollectionActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                this.colecciones = colecciones ?: emptyList()
                mostrarColecciones()
            }
        }
    }

    private fun cargarTotalesUsuario() {
        usuario?.idUsuario?.let { idUsuario ->
            Log.d(TAG, "cargarTotalesUsuario: Solicitando totales para usuario ID: $idUsuario")

            NetworkObjectUtils.obtenerTotalesPorUsuario(idUsuario.toLong()) { totales, error ->
                runOnUiThread {
                    if (error == null && totales != null) {
                        Log.d(TAG, "cargarTotalesUsuario: Totales recibidos - " +
                                "Colección: ${totales.totalColeccion}, " +
                                "Gastado: ${totales.totalGastado}, " +
                                "Items: ${totales.totalItems}")

                        actualizarVistasConTotales(totales)
                    } else {
                        Log.e(TAG, "cargarTotalesUsuario: Error al cargar totales - $error")
                        mostrarTotalesPorDefecto()
                        Toast.makeText(this, "Error al cargar estadísticas: $error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "cargarTotalesUsuario: ID de usuario no disponible")
            mostrarTotalesPorDefecto()
        }
    }

    private fun inicializarVistasTotales() {
        totalColeccionValor = findViewById(R.id.totalColeccionValor)
        totalItemsCount = findViewById(R.id.totalItemsCount)
        totalGastadoValor = findViewById(R.id.totalGastadoValor)
    }

    private fun actualizarVistasConTotales(totales: TotalesUsuarioResponse) {
        try {
            // Formatear valores monetarios
            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            formatoMoneda.maximumFractionDigits = 0

            // Total Colección
            totalColeccionValor.text = formatoMoneda.format(totales.totalColeccion ?: 0)

            // Total Items
            totalItemsCount.text = (totales.totalItems ?: 0).toString()

            // Total Gastado (en negativo y en rojo como en el diseño original)
            val totalGastado = totales.totalGastado ?: 0
            totalGastadoValor.text = formatoMoneda.format(totalGastado)

            Log.d(TAG, "actualizarVistasConTotales: Vistas actualizadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "actualizarVistasConTotales: Error al formatear valores", e)
            mostrarTotalesPorDefecto()
        }
    }

    private fun mostrarTotalesPorDefecto() {
        val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        formatoMoneda.maximumFractionDigits = 0

        totalColeccionValor.text = formatoMoneda.format(0)
        totalItemsCount.text = "0"
        totalGastadoValor.text = formatoMoneda.format(0)
    }
}