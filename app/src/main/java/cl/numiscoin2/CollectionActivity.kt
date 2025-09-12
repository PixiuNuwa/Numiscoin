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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
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
            intent.putExtra(WelcomeActivity.EXTRA_USUARIO, usuarioActual)
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

        if (usuario == null || usuario?.idUsuario == 0L) {
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

/*package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

class CollectionActivity : BaseActivity(), CoinAdapter.OnItemClickListener {

    private val TAG = "CollectionActivity"
    private var primeraColeccion: Coleccion? = null
    private val REQUEST_ADD_COIN = 1
    private var isLoading = false
    private var objetosCompletos: List<ObjetoColeccion> = emptyList() // ← AGREGAR esta variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        Log.d(TAG, "onCreate: CollectionActivity creada")

        // Ahora el usuario viene de la variable heredada de BaseActivity
        Log.d(TAG, "onCreate: Usuario ID ${usuario?.idUsuario ?: "N/A"}, Nombre: ${usuario?.nombre ?: "N/A"}")

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection) // Marcar Colección como seleccionado
        Log.d(TAG, "onCreate: Menú inferior configurado")

        // Resto del código existente...
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        val coinsRecyclerView = findViewById<RecyclerView>(R.id.coinsRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)

        val fabAddCoin = findViewById<FloatingActionButton>(R.id.fabAddCoin)
        fabAddCoin.setOnClickListener {
            // SOLUCIÓN: Crear copias locales para evitar problemas de concurrencia
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
            intent.putExtra(WelcomeActivity.EXTRA_USUARIO, usuarioActual)
            intent.putExtra("idColeccion", coleccionActual.id.toInt())
            Log.d(TAG, "Enviando idColeccion: ${coleccionActual.id} a AddCoinActivity")

            startActivityForResult(intent, REQUEST_ADD_COIN)
        }

        coinsRecyclerView.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "onCreate: RecyclerView configurado")

        backButton.setOnClickListener {
            Log.d(TAG, "backButton: Click en botón volver")
            finish()
        }

        // Cargar colección
        cargarColeccionDesdeServidor()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ADD_COIN && resultCode == RESULT_OK) {
            Log.d(TAG, "Moneda agregada exitosamente, recargando colección...")
            cargarColeccionDesdeServidor()
        }
        else if (resultCode == RESULT_OK && data?.getBooleanExtra("deleted", false) == true) {
            Log.d(TAG, "Moneda eliminada, recargando colección...")
            cargarColeccionDesdeServidor()
        }
    }

    // Función para mostrar/ocultar loading
    private fun showLoading(show: Boolean) {
        isLoading = show
        Log.d(TAG, "showLoading: ${if (show) "Mostrando" else "Ocultando"} loading")
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val coinsRecyclerView = findViewById<RecyclerView>(R.id.coinsRecyclerView)

        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        if (show) {
            coinsRecyclerView.visibility = android.view.View.GONE
        }
    }

    // Función para mostrar monedas - MODIFICADA
    private fun displayCoins(coins: List<Moneda>) {
        Log.d(TAG, "displayCoins: Mostrando ${coins.size} monedas en la UI")
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        val coinsRecyclerView = findViewById<RecyclerView>(R.id.coinsRecyclerView)

        collectionInfo.text = "Tu colección (${coins.size} objetos)"
        coinsRecyclerView.visibility = android.view.View.VISIBLE

        // Crear adapter con ambos parámetros y establecer listener
        val adapter = CoinAdapter(coins, objetosCompletos)
        adapter.setOnItemClickListener(this)
        coinsRecyclerView.adapter = adapter
    }

    // Implementación del listener para clicks - AGREGAR este método
    override fun onItemClick(objeto: ObjetoColeccion) {
        Log.d(TAG, "onItemClick: Objeto seleccionado: ${objeto.nombre}")
        val intent = Intent(this, CoinDetailActivity::class.java)
        intent.putExtra("moneda", objeto)
        //startActivity(intent)
        startActivityForResult(intent, 2)
    }

    private fun cargarColeccionDesdeServidor() {
        showLoading(true)
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        collectionInfo.text = "Cargando tu colección..."
        Log.d(TAG, "cargarColeccionDesdeServidor: Iniciando carga de colección")

        if (usuario == null || usuario?.idUsuario == 0L) {
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

                    // Obtener objetos de la colección
                    NetworkUtils.getCollectionObjects(primeraColeccion!!.id) { objetos, errorObjetos ->
                        runOnUiThread {
                            showLoading(false)

                            if (errorObjetos != null) {
                                collectionInfo.text = "Error al cargar objetos"
                                Toast.makeText(this@CollectionActivity, errorObjetos, Toast.LENGTH_SHORT).show()
                                return@runOnUiThread
                            }

                            objetosCompletos = objetos ?: emptyList()

                            // Convertir ObjetoColeccion a Moneda para el adaptador existente
                            val monedas = objetos?.map { objeto ->
                                Moneda(
                                    id = objeto.id,
                                    nombre = objeto.nombre,
                                    descripcion = objeto.descripcion,
                                    pais = "ID País: ${objeto.idPais}",
                                    anio = objeto.anio.toString(),
                                    estado = objeto.monedaInfo?.estado ?: "Sin información",
                                    valor = objeto.monedaInfo?.valorAdquirido ?: "Sin valor",
                                    fotos = objeto.fotos
                                )
                            } ?: emptyList()

                            if (monedas.isNotEmpty()) {
                                displayCoins(monedas)
                            } else {
                                collectionInfo.text = "No tienes objetos en tu colección '${primeraColeccion?.nombre}'"
                                Toast.makeText(this@CollectionActivity, "Colección vacía", Toast.LENGTH_SHORT).show()
                            }
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
*/