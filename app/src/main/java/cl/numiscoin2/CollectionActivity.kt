//<<CollectionActivity.kt
package cl.numiscoin2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

class CollectionActivity : BaseActivity() {

    private val TAG = "CollectionActivity"

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

        coinsRecyclerView.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "onCreate: RecyclerView configurado")

        backButton.setOnClickListener {
            Log.d(TAG, "backButton: Click en botón volver")
            finish()
        }

        // Función para mostrar/ocultar loading
        fun showLoading(show: Boolean) {
            Log.d(TAG, "showLoading: ${if (show) "Mostrando" else "Ocultando"} loading")
            progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
            if (show) {
                coinsRecyclerView.visibility = android.view.View.GONE
            }
        }

        // Función para mostrar monedas
        fun displayCoins(coins: List<Moneda>) {
            Log.d(TAG, "displayCoins: Mostrando ${coins.size} monedas en la UI")
            collectionInfo.text = "Tu colección (${coins.size} objetos)"
            coinsRecyclerView.visibility = android.view.View.VISIBLE
            coinsRecyclerView.adapter = CoinAdapter(coins)
        }

        // Cargar colección
        showLoading(true)
        collectionInfo.text = "Cargando tu colección..."
        Log.d(TAG, "onCreate: Iniciando carga de colección en background thread")

        // Verificar que tenemos un usuario válido
        if (usuario == null || usuario?.idUsuario == 0L) {
            Log.e(TAG, "onCreate: Usuario no válido para cargar colección")
            showLoading(false)
            collectionInfo.text = "Error: Usuario no identificado"
            Toast.makeText(this, "Error al identificar el usuario", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            Log.d(TAG, "BackgroundThread: Iniciando proceso de carga de datos para usuario ID: ${usuario?.idUsuario}")
            try {
                // Primero: Obtener las colecciones del usuario
                val coleccionesUrl = URL("https://a05d441d8a25.ngrok-free.app/api/jdbc/colecciones/usuario/${usuario?.idUsuario}")
                val coleccionesConnection = coleccionesUrl.openConnection() as HttpURLConnection
                coleccionesConnection.requestMethod = "GET"
                coleccionesConnection.setRequestProperty("Accept", "application/json")

                Log.d("CollectionDebug", "Solicitando colecciones para usuario ID: ${usuario?.idUsuario}")

                val coleccionesResponseCode = coleccionesConnection.responseCode
                if (coleccionesResponseCode == HttpURLConnection.HTTP_OK) {
                    val coleccionesResponse = coleccionesConnection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("CollectionDebug", "Respuesta colecciones recibida: $coleccionesResponse")

                    val gson = Gson()
                    val coleccionListType = object : TypeToken<List<Coleccion>>() {}.type
                    val colecciones = gson.fromJson<List<Coleccion>>(coleccionesResponse, coleccionListType)

                    if (colecciones != null && colecciones.isNotEmpty()) {
                        // Tomar la primera colección (podrías modificar esto para elegir una específica)
                        val primeraColeccion = colecciones[0]
                        Log.d("CollectionDebug", "Colección encontrada: ID ${primeraColeccion.id}, Nombre: ${primeraColeccion.nombre}")

                        // Segundo: Obtener los objetos de la colección
                        val objetosUrl = URL("https://a05d441d8a25.ngrok-free.app/api/jdbc/colecciones/${primeraColeccion.id}/objetos")
                        val objetosConnection = objetosUrl.openConnection() as HttpURLConnection
                        objetosConnection.requestMethod = "GET"
                        objetosConnection.setRequestProperty("Accept", "application/json")

                        Log.d("CollectionDebug", "Solicitando objetos para colección ID: ${primeraColeccion.id}")

                        val objetosResponseCode = objetosConnection.responseCode
                        if (objetosResponseCode == HttpURLConnection.HTTP_OK) {
                            val objetosResponse = objetosConnection.inputStream.bufferedReader().use { it.readText() }
                            Log.d("CollectionDebug", "Respuesta objetos recibida: $objetosResponse")

                            val objetoListType = object : TypeToken<List<ObjetoColeccion>>() {}.type
                            val objetos = gson.fromJson<List<ObjetoColeccion>>(objetosResponse, objetoListType)

                            // Convertir ObjetoColeccion a Moneda para el adaptador existente
                            val monedas = objetos?.map { objeto ->
                                Moneda(
                                    id = objeto.id,
                                    nombre = objeto.nombre,
                                    descripcion = objeto.descripcion,
                                    pais = "ID País: ${objeto.idPais}", // Puedes mejorar esto mapeando ID a nombre de país
                                    anio = objeto.anio.toString(),
                                    estado = objeto.monedaInfo?.estado ?: "Sin información",
                                    valor = objeto.monedaInfo?.valorAdquirido ?: "Sin valor"
                                )
                            } ?: emptyList()

                            runOnUiThread {
                                showLoading(false)
                                if (monedas.isNotEmpty()) {
                                    displayCoins(monedas)
                                } else {
                                    collectionInfo.text = "No tienes objetos en tu colección '${primeraColeccion.nombre}'"
                                    Toast.makeText(this@CollectionActivity, "Colección vacía", Toast.LENGTH_SHORT).show()
                                }
                            }

                            objetosConnection.disconnect()
                        } else {
                            runOnUiThread {
                                showLoading(false)
                                collectionInfo.text = "Error al cargar objetos de la colección"
                                Toast.makeText(this@CollectionActivity, "Error del servidor: $objetosResponseCode", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            showLoading(false)
                            collectionInfo.text = "No tienes colecciones creadas"
                            Toast.makeText(this@CollectionActivity, "No se encontraron colecciones", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        showLoading(false)
                        collectionInfo.text = "Error al cargar colecciones"
                        Toast.makeText(this@CollectionActivity, "Error del servidor: $coleccionesResponseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                coleccionesConnection.disconnect()

            } catch (e: Exception) {
                Log.e("CollectionError", "Error al cargar colección", e)
                runOnUiThread {
                    showLoading(false)
                    collectionInfo.text = "Error de conexión"
                    Toast.makeText(this@CollectionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
//>>CollectionActivity.kt
/*package cl.numiscoin2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

class CollectionActivity : BaseActivity() {

    private lateinit var usuario: Usuario
    private val TAG = "CollectionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        // Obtener el objeto Usuario del intent
        usuario = intent.getParcelableExtra(WelcomeActivity.EXTRA_USUARIO) ?: run {
            Log.w(TAG, "onCreate: No se encontró usuario en el intent, creando usuario vacío")
            // Si no viene el usuario, crear uno vacío (solo para evitar crash)
            Usuario(0, "Usuario", "", "", "", "")
        }
        Log.d(TAG, "onCreate: Usuario ID ${usuario.idUsuario}, Nombre: ${usuario.nombre}")

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection) // Marcar Colección como seleccionado
        Log.d(TAG, "onCreate: Menú inferior configurado")

        // Resto del código existente...
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        val coinsRecyclerView = findViewById<RecyclerView>(R.id.coinsRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)

        coinsRecyclerView.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "onCreate: RecyclerView configurado")

        backButton.setOnClickListener {
            Log.d(TAG, "backButton: Click en botón volver")
            finish()
        }

        // Función para mostrar/ocultar loading
        fun showLoading(show: Boolean) {
            Log.d(TAG, "showLoading: ${if (show) "Mostrando" else "Ocultando"} loading")
            progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
            if (show) {
                coinsRecyclerView.visibility = android.view.View.GONE
            }
        }

        // Función para mostrar monedas
        fun displayCoins(coins: List<Moneda>) {
            Log.d(TAG, "displayCoins: Mostrando ${coins.size} monedas en la UI")
            collectionInfo.text = "Tu colección (${coins.size} objetos)"
            coinsRecyclerView.visibility = android.view.View.VISIBLE
            coinsRecyclerView.adapter = CoinAdapter(coins)
        }

        // Cargar colección
        showLoading(true)
        collectionInfo.text = "Cargando tu colección..."
        Log.d(TAG, "onCreate: Iniciando carga de colección en background thread")

        Thread {
            Log.d(TAG, "BackgroundThread: Iniciando proceso de carga de datos")
            try {
                // Primero: Obtener las colecciones del usuario
                val coleccionesUrl = URL("https://a05d441d8a25.ngrok-free.app/api/jdbc/colecciones/usuario/${usuario.idUsuario}")
                val coleccionesConnection = coleccionesUrl.openConnection() as HttpURLConnection
                coleccionesConnection.requestMethod = "GET"
                coleccionesConnection.setRequestProperty("Accept", "application/json")

                Log.d("CollectionDebug", "Solicitando colecciones para usuario ID: ${usuario.idUsuario}")

                val coleccionesResponseCode = coleccionesConnection.responseCode
                if (coleccionesResponseCode == HttpURLConnection.HTTP_OK) {
                    val coleccionesResponse = coleccionesConnection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("CollectionDebug", "Respuesta colecciones recibida: $coleccionesResponse")

                    val gson = Gson()
                    val coleccionListType = object : TypeToken<List<Coleccion>>() {}.type
                    val colecciones = gson.fromJson<List<Coleccion>>(coleccionesResponse, coleccionListType)

                    if (colecciones != null && colecciones.isNotEmpty()) {
                        // Tomar la primera colección (podrías modificar esto para elegir una específica)
                        val primeraColeccion = colecciones[0]
                        Log.d("CollectionDebug", "Colección encontrada: ID ${primeraColeccion.id}, Nombre: ${primeraColeccion.nombre}")

                        // Segundo: Obtener los objetos de la colección
                        val objetosUrl = URL("https://a05d441d8a25.ngrok-free.app/api/jdbc/colecciones/${primeraColeccion.id}/objetos")
                        val objetosConnection = objetosUrl.openConnection() as HttpURLConnection
                        objetosConnection.requestMethod = "GET"
                        objetosConnection.setRequestProperty("Accept", "application/json")

                        Log.d("CollectionDebug", "Solicitando objetos para colección ID: ${primeraColeccion.id}")

                        val objetosResponseCode = objetosConnection.responseCode
                        if (objetosResponseCode == HttpURLConnection.HTTP_OK) {
                            val objetosResponse = objetosConnection.inputStream.bufferedReader().use { it.readText() }
                            Log.d("CollectionDebug", "Respuesta objetos recibida: $objetosResponse")

                            val objetoListType = object : TypeToken<List<ObjetoColeccion>>() {}.type
                            val objetos = gson.fromJson<List<ObjetoColeccion>>(objetosResponse, objetoListType)

                            // Convertir ObjetoColeccion a Moneda para el adaptador existente
                            val monedas = objetos?.map { objeto ->
                                Moneda(
                                    id = objeto.id,
                                    nombre = objeto.nombre,
                                    descripcion = objeto.descripcion,
                                    pais = "ID País: ${objeto.idPais}", // Puedes mejorar esto mapeando ID a nombre de país
                                    anio = objeto.anio.toString(),
                                    estado = objeto.monedaInfo?.estado ?: "Sin información",
                                    valor = objeto.monedaInfo?.valorAdquirido ?: "Sin valor"
                                )
                            } ?: emptyList()

                            runOnUiThread {
                                showLoading(false)
                                if (monedas.isNotEmpty()) {
                                    displayCoins(monedas)
                                } else {
                                    collectionInfo.text = "No tienes objetos en tu colección '${primeraColeccion.nombre}'"
                                    Toast.makeText(this@CollectionActivity, "Colección vacía", Toast.LENGTH_SHORT).show()
                                }
                            }

                            objetosConnection.disconnect()
                        } else {
                            runOnUiThread {
                                showLoading(false)
                                collectionInfo.text = "Error al cargar objetos de la colección"
                                Toast.makeText(this@CollectionActivity, "Error del servidor: $objetosResponseCode", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            showLoading(false)
                            collectionInfo.text = "No tienes colecciones creadas"
                            Toast.makeText(this@CollectionActivity, "No se encontraron colecciones", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        showLoading(false)
                        collectionInfo.text = "Error al cargar colecciones"
                        Toast.makeText(this@CollectionActivity, "Error del servidor: $coleccionesResponseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                coleccionesConnection.disconnect()

            } catch (e: Exception) {
                Log.e("CollectionError", "Error al cargar colección", e)
                runOnUiThread {
                    showLoading(false)
                    collectionInfo.text = "Error de conexión"
                    Toast.makeText(this@CollectionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}*/