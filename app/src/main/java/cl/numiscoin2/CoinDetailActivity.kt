package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
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
import android.util.Log

class CoinDetailActivity : AppCompatActivity() {

    private lateinit var objeto: ObjetoColeccion
    private lateinit var deleteButton: Button
    private lateinit var editButton: Button

    companion object {
        private const val EDIT_COIN_REQUEST = 1001
        const val EXTRA_MONEDA_ACTUALIZADA = "moneda_actualizada"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_detail)

        // Obtener el objeto de la moneda
        objeto = intent.getParcelableExtra("moneda") ?: return

        // Configurar la UI
        setupUI()
        // Configurar botón de eliminar
        setupDeleteButton()

        setupEditButton()
    }

    private fun setupUI() {
        // Información básica
        findViewById<TextView>(R.id.tvNombre).text = objeto.nombre
        findViewById<TextView>(R.id.tvDescripcion).text = objeto.descripcion
        findViewById<TextView>(R.id.tvAnio).text = objeto.anio.toString()
        findViewById<TextView>(R.id.tvPais).text = objeto.nombrePais

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
        deleteButton = findViewById(R.id.btnDelete)

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

    private fun setupEditButton() {
        editButton = findViewById<Button>(R.id.btnEdit)
        editButton.setOnClickListener {
            val intent = Intent(this, EditCoinActivity::class.java)
            intent.putExtra("moneda", objeto)
            startActivityForResult(intent, EDIT_COIN_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_COIN_REQUEST && resultCode == Activity.RESULT_OK) {

            Toast.makeText(this, "Moneda actualizada exitosamente", Toast.LENGTH_SHORT).show()
            recargarMoneda()
        }
    }

    private fun recargarMoneda() {
        Log.i("MONEDA","IN")
        // Mostrar progress bar o indicador de carga
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Cargando datos actualizados...")
            Log.i("MONEDA","Cargando datos actualizados...")
            setCancelable(false)
            show()
        }

        // Llamar al servidor para obtener los datos actualizados
        NetworkUtils.obtenerMonedaPorId(objeto.id) { monedaActualizada, error ->
            runOnUiThread {
                Log.i("MONEDA","obteniendo moneda por ID")
                progressDialog.dismiss()

                if (monedaActualizada != null) {
                    // Actualizar el objeto y la UI
                    Log.i("MONEDA","actualizar objeto y UI")
                    objeto = monedaActualizada
                    setupUI() // Volver a configurar la UI con los nuevos datos
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al cargar datos actualizados: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}