package cl.numiscoin2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

class CollectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        // Inicializar vistas
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val collectionInfo = findViewById<TextView>(R.id.collectionInfo)
        val coinsRecyclerView = findViewById<RecyclerView>(R.id.coinsRecyclerView)
        val backButton = findViewById<Button>(R.id.backButton)

        coinsRecyclerView.layoutManager = LinearLayoutManager(this)

        backButton.setOnClickListener {
            finish()
        }

        // Función para mostrar/ocultar loading
        fun showLoading(show: Boolean) {
            progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
            if (show) {
                coinsRecyclerView.visibility = android.view.View.GONE
            }
        }

        // Función para mostrar monedas
        fun displayCoins(coins: List<Moneda>) {
            collectionInfo.text = "Tu colección (${coins.size} monedas)"
            coinsRecyclerView.visibility = android.view.View.VISIBLE
            coinsRecyclerView.adapter = CoinAdapter(coins)
        }

        // Cargar colección
        showLoading(true)
        collectionInfo.text = "Cargando tu colección..."

        Thread {
            try {
                val url = URL("https://2f832ec5162a.ngrok-free.app/api/jdbc/monedas")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                Log.d("CollectionDebug", "Solicitando monedas...")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("CollectionDebug", "Respuesta recibida: $response")

                    val gson = Gson()
                    val coinListType = object : TypeToken<List<Moneda>>() {}.type
                    val coins = gson.fromJson<List<Moneda>>(response, coinListType)

                    runOnUiThread {
                        showLoading(false)
                        if (coins != null && coins.isNotEmpty()) {
                            displayCoins(coins)
                        } else {
                            collectionInfo.text = "No tienes monedas en tu colección"
                            Toast.makeText(this@CollectionActivity, "Colección vacía", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        showLoading(false)
                        collectionInfo.text = "Error al cargar la colección"
                        Toast.makeText(this@CollectionActivity, "Error del servidor: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()

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