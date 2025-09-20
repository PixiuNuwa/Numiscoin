package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setMargins
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CollectionActivity : BaseActivity() {

    private val TAG = "CollectionActivity"
    private var primeraColeccion: Coleccion? = null
    private val REQUEST_ADD_COIN = 1
    private var isLoading = false
    private var tiposObjetos: List<TipoObjeto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        Log.d(TAG, "onCreate: CollectionActivity creada")

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)
        Log.d(TAG, "onCreate: Menú inferior configurado")

        // Configurar FAB para agregar objetos
        val fabAddCoin = findViewById<FloatingActionButton>(R.id.fabAddCoin)
        fabAddCoin.setOnClickListener {
            val usuarioActual = usuario
            val coleccionActual = primeraColeccion

            if (usuarioActual == null) {
                Log.w(TAG, "Usuario nulo, no se puede abrir AddCoinActivity")
                Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (coleccionActual == null) {
                Log.w(TAG, "No hay colección disponible")
                Toast.makeText(this, "Error: No hay colección seleccionada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this@CollectionActivity, AddCoinActivity::class.java)
            intent.putExtra("idColeccion", coleccionActual.id.toInt())
            Log.d(TAG, "Enviando idColeccion: ${coleccionActual.id} a AddCoinActivity")

            startActivityForResult(intent, REQUEST_ADD_COIN)
        }

        // Configurar botón de retroceso
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            Log.d(TAG, "backButton: Click en botón volver")
            finish()
        }

        // Cargar colección y tipos de objetos
        cargarColeccionDesdeServidor()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_COIN && resultCode == RESULT_OK) {
            Log.d(TAG, "Objeto agregado exitosamente, recargando colección...")
            cargarColeccionDesdeServidor()
        } else if (resultCode == RESULT_OK && data?.getBooleanExtra("deleted", false) == true) {
            Log.d(TAG, "Objeto eliminado, recargando colección...")
            cargarColeccionDesdeServidor()
        }
    }

    private fun showLoading(show: Boolean) {
        isLoading = show
        Log.d(TAG, "showLoading: ${if (show) "Mostrando" else "Ocultando"} loading")
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tiposContainer = findViewById<LinearLayout>(R.id.tiposContainer)

        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        if (show) {
            tiposContainer.visibility = android.view.View.GONE
        }
    }

    private fun mostrarTiposObjetos() {
        Log.d(TAG, "mostrarTiposObjetos: Mostrando ${tiposObjetos.size} tipos de objetos")
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        val tiposContainer = findViewById<LinearLayout>(R.id.tiposContainer)

        collectionInfo.text = "'${primeraColeccion?.nombre}'"
        tiposContainer.visibility = android.view.View.VISIBLE
        tiposContainer.removeAllViews()

        if (tiposObjetos.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "No hay tipos de objetos disponibles"
            emptyText.setTextColor(resources.getColor(android.R.color.white))
            emptyText.textSize = 16f
            emptyText.gravity = android.view.Gravity.CENTER
            tiposContainer.addView(emptyText)
            return
        }

        // Cambiar MaterialButton por Button normal
        tiposObjetos.forEach { tipo ->
            val button = Button(this).apply {
                text = tipo.nombre
                setTextColor(resources.getColor(android.R.color.white))
                setBackgroundColor(resources.getColor(R.color.menu_unselected))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16)
                }

                // Al hacer clic en un botón, abrir la actividad que muestra los objetos de ese tipo
                setOnClickListener {
                    val intent = Intent(this@CollectionActivity, ObjetosListActivity::class.java)
                    intent.putExtra("idColeccion", primeraColeccion?.id)
                    intent.putExtra("idTipoObjeto", tipo.id)
                    intent.putExtra("nombreTipo", tipo.nombre)
                    startActivity(intent)
                }
            }
            tiposContainer.addView(button)
        }
    }

    private fun cargarColeccionDesdeServidor() {
        showLoading(true)
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        collectionInfo.text = "Cargando tu colección..."
        Log.d(TAG, "cargarColeccionDesdeServidor: Iniciando carga de colección")

        val usuarioId = usuario?.idUsuario ?: 0L

        if (usuarioId == 0L) {
            Log.e(TAG, "cargarColeccionDesdeServidor: Usuario no válido")
            showLoading(false)
            collectionInfo.text = "Error: Usuario no identificado"
            Toast.makeText(this, "Error al identificar el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener colecciones del usuario
        NetworkUtils.getUserCollections(usuario!!.idUsuario) { colecciones, error ->
            runOnUiThread {
                if (error != null) {
                    showLoading(false)
                    collectionInfo.text = "Error al cargar colecciones"
                    Toast.makeText(this@CollectionActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                if (colecciones != null && colecciones.isNotEmpty()) {
                    primeraColeccion = colecciones[0]
                    Log.d("CollectionDebug", "Colección encontrada: ID ${primeraColeccion?.id}, Nombre: ${primeraColeccion?.nombre}")

                    // Obtener tipos de objetos de la colección
                    NetworkUtils.getCollectionObjectTypes(primeraColeccion!!.id) { tipos, errorTipos ->
                        runOnUiThread {
                            showLoading(false)

                            if (errorTipos != null) {
                                collectionInfo.text = "Error al cargar tipos de objetos"
                                Toast.makeText(this@CollectionActivity, errorTipos, Toast.LENGTH_SHORT).show()
                                return@runOnUiThread
                            }

                            tiposObjetos = tipos ?: emptyList()
                            mostrarTiposObjetos()
                        }
                    }
                } else {
                    showLoading(false)
                    collectionInfo.text = "No tienes colecciones creadas"
                    Toast.makeText(this@CollectionActivity, "No se encontraron colecciones", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
