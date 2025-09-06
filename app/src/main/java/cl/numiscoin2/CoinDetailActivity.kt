package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.net.ssl.HttpsURLConnection
import java.net.URL

class CoinDetailActivity : AppCompatActivity() {

    private lateinit var objeto: ObjetoColeccion
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_detail)

        // Obtener el objeto de la moneda
        objeto = intent.getParcelableExtra("moneda") ?: return

        // Configurar la UI
        setupUI()
        // Configurar botón de eliminar
        setupDeleteButton()
    }

    private fun setupUI() {
        // Información básica
        findViewById<TextView>(R.id.tvNombre).text = objeto.nombre
        findViewById<TextView>(R.id.tvDescripcion).text = objeto.descripcion
        findViewById<TextView>(R.id.tvAnio).text = objeto.anio.toString()
        findViewById<TextView>(R.id.tvPais).text = "ID País: ${objeto.idPais}"

        // Información de moneda
        objeto.monedaInfo?.let { info ->
            findViewById<TextView>(R.id.tvFamilia).text = info.familia ?: "No especificado"
            findViewById<TextView>(R.id.tvVariante).text = info.variante ?: "No especificado"
            findViewById<TextView>(R.id.tvCeca).text = info.ceca ?: "No especificado"
            findViewById<TextView>(R.id.tvTipo).text = info.tipo ?: "No especificado"
            findViewById<TextView>(R.id.tvDisenador).text = info.disenador ?: "No especificado"
            findViewById<TextView>(R.id.tvTotalProducido).text = info.totalProducido ?: "No especificado"
            findViewById<TextView>(R.id.tvValorSinCircular).text = info.valorSinCircular ?: "No especificado"
            findViewById<TextView>(R.id.tvValorComercial).text = info.valorComercial ?: "No especificado"
            findViewById<TextView>(R.id.tvValorAdquirido).text = info.valorAdquirido ?: "No especificado"
            findViewById<TextView>(R.id.tvEstado).text = info.estado ?: "No especificado"
            findViewById<TextView>(R.id.tvObservaciones).text = info.observaciones ?: "Sin observaciones"
            findViewById<TextView>(R.id.tvAcunada).text = info.acunada ?: "No especificado"
        }

        // Configurar carrusel de imágenes si hay fotos
        setupImageCarousel()
    }

    private fun setupDeleteButton() {
        deleteButton = Button(this).apply {
            text = "Eliminar Moneda"
            setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            setTextColor(resources.getColor(android.R.color.white))
        }

        // Agregar el botón al layout principal
        val layout = findViewById<LinearLayout>(R.id.mainLayout)
        layout.addView(deleteButton)

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar esta moneda? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { dialog, which ->
                deleteMoneda()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ... (código anterior sin cambios)

    private fun deleteMoneda() {
        NetworkUtils.deleteMoneda(objeto.id) { success, error ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this@CoinDetailActivity, "Moneda eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("deleted", true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@CoinDetailActivity, error ?: "Error al eliminar la moneda", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

// ... (código posterior sin cambios)

    /*private fun deleteMoneda() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://5147bbbf57c8.ngrok-free.app/api/jdbc/monedas/${objeto.id}")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                val response = if (responseCode == HttpsURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream.bufferedReader().use { it.readText() }
                }

                withContext(Dispatchers.Main) {
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        Toast.makeText(this@CoinDetailActivity, "Moneda eliminada exitosamente", Toast.LENGTH_SHORT).show()

                        // Crear intent de resultado para actualizar la CollectionActivity
                        val resultIntent = Intent()
                        resultIntent.putExtra("deleted", true)
                        setResult(RESULT_OK, resultIntent)

                        finish() // Cerrar esta actividad
                    } else {
                        Toast.makeText(this@CoinDetailActivity, "Error al eliminar la moneda", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CoinDetailActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }*/

    private fun setupImageCarousel() {
        val fotos = objeto.fotos
        if (fotos.isNullOrEmpty()) {
            findViewById<ViewPager2>(R.id.viewPager).visibility = android.view.View.GONE
            findViewById<TabLayout>(R.id.tabLayout).visibility = android.view.View.GONE
            return
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val adapter = ImagePagerAdapter(fotos)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Opcional: puedes agregar indicadores o números
        }.attach()
    }
}