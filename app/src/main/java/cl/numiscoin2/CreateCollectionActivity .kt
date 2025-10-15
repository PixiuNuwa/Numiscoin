package cl.numiscoin2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkCollectionUtils

class CreateCollectionActivity : BaseActivity() {

    private val TAG = "CreateCollectionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        setContentView(R.layout.activity_create_collection)

        Log.d(TAG, "onCreate: CreateCollectionActivity creada")

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val btnCrear = findViewById<Button>(R.id.btnCrear)


        btnCrear.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            crearColeccion(nombre, descripcion)
        }
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)
    }

    private fun crearColeccion(nombre: String, descripcion: String) {
        // Mostrar loading
        val btnCrear = findViewById<Button>(R.id.btnCrear)
        btnCrear.isEnabled = false
        btnCrear.text = "Creando..."

        Log.d(TAG, "crearColeccion: Creando colección: $nombre")

        // Obtener el ID del usuario actual
        val usuarioId = usuario?.idUsuario ?: 0L

        if (usuarioId == 0L) {
            Log.e(TAG, "crearColeccion: Usuario no válido")
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            btnCrear.isEnabled = true
            btnCrear.text = "Crear"
            return
        }

        // Llamar al método real de NetworkCollectionUtils
        NetworkCollectionUtils.createCollection(usuarioId, nombre, descripcion) { coleccion, error ->
            runOnUiThread {
                if (error != null) {
                    // Manejar error
                    Log.e(TAG, "crearColeccion: Error al crear colección: $error")
                    Toast.makeText(this@CreateCollectionActivity, "Error: $error", Toast.LENGTH_SHORT).show()

                    // Restaurar botón
                    btnCrear.isEnabled = true
                    btnCrear.text = "Crear"
                } else {
                    // Éxito
                    if (coleccion != null) {
                        Log.d(TAG, "crearColeccion: Colección creada exitosamente - ID: ${coleccion.id}, Nombre: ${coleccion.nombre}")

                        // Puedes pasar datos de vuelta si es necesario
                        val resultIntent = Intent().apply {
                            putExtra("coleccion_creada", true)
                            putExtra("id_coleccion", coleccion.id)
                            putExtra("nombre_coleccion", coleccion.nombre)
                        }

                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.e(TAG, "crearColeccion: Colección nula recibida")
                        Toast.makeText(this@CreateCollectionActivity, "Error: No se recibió respuesta del servidor", Toast.LENGTH_SHORT).show()
                        btnCrear.isEnabled = true
                        btnCrear.text = "Crear"
                    }
                }
            }
        }
    }
}