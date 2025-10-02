package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CoinDetailActivity : AppCompatActivity() {

    private lateinit var objeto: ObjetoColeccion
    private lateinit var deleteButton: Button
    private lateinit var editButton: Button
    private lateinit var ivImagenPrincipal: ImageView
    private lateinit var llMiniaturas: LinearLayout
    private var fotos: List<FotoObjeto> = emptyList()
    private var fotoSeleccionadaIndex: Int = 0

    companion object {
        private const val EDIT_COIN_REQUEST = 1001
        const val EXTRA_MONEDA_ACTUALIZADA = "moneda_actualizada"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_detail)

        // Obtener el objeto de la moneda
        objeto = intent.getParcelableExtra("moneda") ?: return

        // Inicializar vistas
        initViews()

        // Configurar la UI
        setupUI()

        // Configurar botones
        setupDeleteButton()
        setupEditButton()
    }

    private fun initViews() {
        ivImagenPrincipal = findViewById(R.id.ivImagenPrincipal)
        llMiniaturas = findViewById(R.id.llMiniaturas)
        deleteButton = findViewById(R.id.btnDelete)
        editButton = findViewById(R.id.btnEdit)
    }

    private fun setupUI() {
        // Configurar nombre
        findViewById<TextView>(R.id.tvNombre).text = objeto.nombre

        // Configurar imágenes
        setupImagenes()

        // Configurar tabs de información
        setupTabsInfo()
    }

    private fun setupImagenes() {
        fotos = objeto.fotos ?: emptyList()

        if (fotos.isEmpty()) {
            // Si no hay fotos, ocultar las miniaturas
            llMiniaturas.visibility = android.view.View.GONE
            // Mostrar imagen placeholder en la principal
            ivImagenPrincipal.setImageResource(R.drawable.ic_placeholder)
            return
        }

        // Mostrar primera imagen como principal
        mostrarImagenPrincipal(0)

        // Configurar miniaturas
        setupMiniaturas()
    }

    private fun mostrarImagenPrincipal(index: Int) {
        if (fotos.isNotEmpty() && index < fotos.size) {
            val foto = fotos[index]
            val fotoUrl = if (foto.url.startsWith("http")) {
                foto.url
            } else {
                NetworkConfig.UPLOADS_BASE_URL + foto.url
            }

            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(ivImagenPrincipal)

            fotoSeleccionadaIndex = index

            // Configurar click para zoom (puedes implementar un dialog con zoom)
            ivImagenPrincipal.setOnClickListener {
                mostrarImagenZoom(fotoUrl)
            }
        }
    }

    private fun mostrarImagenZoom(imageUrl: String) {
        // Crear un diálogo personalizado
        val dialog = android.app.AlertDialog.Builder(this).create()

        // Crear un ImageView configurado correctamente
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true

            // Hacer la imagen clickeable para cerrar el diálogo
            setOnClickListener {
                dialog.dismiss()
            }
        }

        // Cargar la imagen con Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .into(imageView)

        // Configurar el diálogo
        dialog.setView(imageView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Mostrar el diálogo
        dialog.show()

        // Configurar el tamaño del diálogo para que ocupe casi toda la pantalla
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun setupMiniaturas() {
        llMiniaturas.removeAllViews()

        val screenWidth = resources.displayMetrics.widthPixels
        val imageSize = (screenWidth * 0.24).toInt() // 24% del ancho
        val margin = (screenWidth * 0.01).toInt() // 1% de separación

        fotos.forEachIndexed { index, foto ->
            val miniaturaContainer = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    imageSize,
                    imageSize
                ).apply {
                    setMargins(margin, 0, margin, 0)
                }
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(
                    if (index == fotoSeleccionadaIndex) {
                        ContextCompat.getColor(this@CoinDetailActivity, R.color.colorPrimary)
                    } else {
                        ContextCompat.getColor(this@CoinDetailActivity, R.color.menu_unselected)
                    }
                )
            }

            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (imageSize * 0.8).toInt(),
                    (imageSize * 0.8).toInt()
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            val fotoUrl = if (foto.url.startsWith("http")) {
                foto.url
            } else {
                NetworkConfig.UPLOADS_BASE_URL + foto.url
            }

            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(imageView)

            // Configurar click para cambiar imagen principal
            miniaturaContainer.setOnClickListener {
                mostrarImagenPrincipal(index)
                actualizarMiniaturasSeleccionadas(index)
            }

            miniaturaContainer.addView(imageView)
            llMiniaturas.addView(miniaturaContainer)
        }
    }

    private fun actualizarMiniaturasSeleccionadas(selectedIndex: Int) {
        for (i in 0 until llMiniaturas.childCount) {
            val miniatura = llMiniaturas.getChildAt(i) as LinearLayout
            miniatura.setBackgroundColor(
                if (i == selectedIndex) {
                    ContextCompat.getColor(this, R.color.colorPrimary)
                } else {
                    ContextCompat.getColor(this, R.color.menu_unselected)
                }
            )
        }
    }

    private fun setupTabsInfo() {
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerInfo)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val adapter = InfoPagerAdapter(this) // Pasar 'this' como FragmentActivity
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Información"
                1 -> "Características"
                2 -> "Valores"
                else -> "Tab"
            }
        }.attach()
    }

    private inner class InfoPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> InfoTabFragment.newInstance(objeto)
                1 -> CaracteristicasTabFragment.newInstance(objeto)
                2 -> ValoresTabFragment.newInstance(objeto)
                else -> InfoTabFragment.newInstance(objeto)
            }
        }
    }

    // Configuración de botones (sin cambios)
    private fun setupDeleteButton() {
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
        NetworkObjectUtils.deleteMoneda(objeto.id) { success, error ->
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

    private fun setupEditButton() {
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
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Cargando datos actualizados...")
            setCancelable(false)
            show()
        }

        NetworkObjectUtils.obtenerMonedaPorId(objeto.id) { monedaActualizada, error ->
            runOnUiThread {
                progressDialog.dismiss()

                if (monedaActualizada != null) {
                    objeto = monedaActualizada
                    setupUI()
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al cargar datos actualizados: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}