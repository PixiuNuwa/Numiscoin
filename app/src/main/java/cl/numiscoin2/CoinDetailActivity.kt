package cl.numiscoin2

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CoinDetailActivity : AppCompatActivity() {

    private lateinit var objeto: ObjetoColeccion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_detail)

        // Obtener el objeto de la moneda
        objeto = intent.getParcelableExtra("moneda") ?: return

        // Configurar la UI
        setupUI()
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