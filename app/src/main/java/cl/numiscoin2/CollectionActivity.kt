package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setMargins
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CollectionActivity : BaseActivity() {

    private val TAG = "CollectionActivity"
    private var isLoading = false
    private var colecciones: List<Coleccion> = emptyList()
    private val REQUEST_CREATE_COLLECTION = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        Log.d(TAG, "onCreate: CollectionActivity creada")

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)
        Log.d(TAG, "onCreate: Menú inferior configurado")

        // Configurar botón de agregar colección
        val btnAddCollection = findViewById<Button>(R.id.btnAddCollection)
        btnAddCollection.setOnClickListener {
            Log.d(TAG, "btnAddCollection: Abriendo vista para crear colección")
            val intent = Intent(this@CollectionActivity, CreateCollectionActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_COLLECTION)
        }

        // Configurar botón de agregar colección en estado vacío
        val btnAddCollectionEmpty = findViewById<Button>(R.id.btnAddCollectionEmpty)
        btnAddCollectionEmpty.setOnClickListener {
            Log.d(TAG, "btnAddCollectionEmpty: Abriendo vista para crear colección")
            val intent = Intent(this@CollectionActivity, CreateCollectionActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_COLLECTION)
        }

        // Cargar colecciones del usuario
        cargarColeccionesDesdeServidor()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CREATE_COLLECTION) {
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
    }

    private fun showLoading(show: Boolean) {
        isLoading = show
        Log.d(TAG, "showLoading: ${if (show) "Mostrando" else "Ocultando"} loading")
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val headerWithCollections = findViewById<LinearLayout>(R.id.headerWithCollections)
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
        val headerWithCollections = findViewById<LinearLayout>(R.id.headerWithCollections)
        val collectionsContainer = findViewById<LinearLayout>(R.id.collectionsContainer)
        val emptyStateContainer = findViewById<LinearLayout>(R.id.emptyStateContainer)

        collectionsContainer.removeAllViews()

        if (colecciones.isEmpty()) {
            // Mostrar estado vacío - OCULTAR header
            headerWithCollections.visibility = android.view.View.GONE
            collectionsContainer.visibility = android.view.View.GONE
            emptyStateContainer.visibility = android.view.View.VISIBLE
            return
        }

        // Mostrar listado de colecciones - MOSTRAR header
        headerWithCollections.visibility = android.view.View.VISIBLE
        collectionsContainer.visibility = android.view.View.VISIBLE
        emptyStateContainer.visibility = android.view.View.GONE

        colecciones.forEach { coleccion ->
            val collectionItem = Button(this).apply {
                text = "${coleccion.nombre}\n${coleccion.descripcion}"
                setTextColor(resources.getColor(android.R.color.white))
                setBackgroundColor(resources.getColor(R.color.menu_unselected))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16)
                }

                // Al hacer clic en una colección, abrir la actividad de objetos
                setOnClickListener {
                    val intent = Intent(this@CollectionActivity, ObjetosListActivity::class.java)
                    intent.putExtra("idColeccion", coleccion.id)
                    intent.putExtra("nombreColeccion", coleccion.nombre)
                    startActivity(intent)
                }
            }
            collectionsContainer.addView(collectionItem)
        }
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


}