package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ObjetosPaisActivity : BaseActivity(), CoinAdapter.OnItemClickListener {

    private val TAG = "ObjetosPaisActivity"
    private var idColeccion: Int = 0
    private var idTipoObjeto: Int = 0
    private var idPais: Int = 0
    private var nombreTipo: String = ""
    private var nombrePais: String = ""
    private var objetosCompletos: List<ObjetoColeccion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_objetos_list)

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)

        // Obtener parámetros del intent
        idColeccion = intent.getIntExtra("idColeccion", 0)
        idTipoObjeto = intent.getIntExtra("idTipoObjeto", 0)
        idPais = intent.getIntExtra("idPais", 0)
        nombreTipo = intent.getStringExtra("nombreTipo") ?: ""
        nombrePais = intent.getStringExtra("nombrePais") ?: ""

        // Configurar UI
        val title = findViewById<TextView>(R.id.title)
        title.text = "$nombreTipo - $nombrePais"

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Cargar objetos del tipo y país seleccionados
        cargarObjetosPorTipoYPais()
    }

    private fun cargarObjetosPorTipoYPais() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

        NetworkCollectionUtils.getCollectionObjects(idColeccion) { objetos, error ->
            runOnUiThread {
                progressBar.visibility = android.view.View.GONE

                if (error != null) {
                    Toast.makeText(this@ObjetosPaisActivity, error, Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                // Filtrar objetos por tipo y país
                val objetosFiltrados = objetos?.filter {
                    it.idTipoObjeto == idTipoObjeto && it.idPais == idPais
                } ?: emptyList()

                objetosCompletos = objetosFiltrados

                if (objetosFiltrados.isNotEmpty()) {
                    mostrarObjetos(objetosFiltrados)
                } else {
                    Toast.makeText(this@ObjetosPaisActivity, "No hay objetos de este tipo y país", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarObjetos(objetos: List<ObjetoColeccion>) {
        val recyclerView = findViewById<RecyclerView>(R.id.objetosRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.visibility = android.view.View.VISIBLE

        // Convertir a Moneda para el adaptador existente
        val monedas = objetos.map { objeto ->
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
        }

        val adapter = CoinAdapter(monedas, objetos)
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(objeto: ObjetoColeccion) {
        Log.d(TAG, "onItemClick: Objeto seleccionado: ${objeto.nombre}")
        val intent = Intent(this, CoinDetailActivity::class.java)
        intent.putExtra("moneda", objeto)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data?.getBooleanExtra("deleted", false) == true) {
            Log.d(TAG, "Objeto eliminado, recargando lista...")
            cargarObjetosPorTipoYPais()
        }
    }
}