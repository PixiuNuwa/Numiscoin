package cl.numiscoin2

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkCollectionUtils

class EditCollectionActivity : BaseActivity() {

    private val TAG = "EditCollectionActivity"
    private var idColeccion: Long = -1
    private var nombreOriginal: String = ""
    private var descripcionOriginal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        //
        setContentView(R.layout.activity_edit_collection)
        Log.d(TAG, "onCreate: EditCollectionActivity creada")

        // Obtener datos de la colección desde el intent
        idColeccion = intent.getLongExtra("idColeccion", -1L)
        nombreOriginal = intent.getStringExtra("nombreColeccion") ?: ""
        descripcionOriginal = intent.getStringExtra("descripcionColeccion") ?: ""

        if (idColeccion == -1L) {
            Log.e(TAG, "onCreate: ID de colección no válido")
            Toast.makeText(this, "Error: Colección no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "onCreate: Editando colección ID: $idColeccion, Nombre: $nombreOriginal")
        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)
        // Configurar interfaz
        configurarInterfaz()

    }

    private fun configurarInterfaz() {
        // Configurar título
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Editar Colección"

        // Configurar campos de texto con los valores actuales
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)

        etNombre.setText(nombreOriginal)
        etDescripcion.setText(descripcionOriginal)

        // Configurar botón Grabar
        val btnGrabar = findViewById<Button>(R.id.btnGrabar)
        btnGrabar.setOnClickListener {
            grabarCambios()
        }

        // Configurar botón Eliminar
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)
        btnEliminar.setOnClickListener {
            eliminarColeccion()
        }

        // Configurar botón de volver (si existe)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun grabarCambios() {
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)

        val nuevoNombre = etNombre.text.toString().trim()
        val nuevaDescripcion = etDescripcion.text.toString().trim()

        // Validaciones
        if (nuevoNombre.isEmpty()) {
            Toast.makeText(this, "El nombre de la colección es obligatorio", Toast.LENGTH_SHORT).show()
            etNombre.requestFocus()
            return
        }

        // Verificar si hubo cambios
        if (nuevoNombre == nombreOriginal && nuevaDescripcion == descripcionOriginal) {
            Toast.makeText(this, "No se detectaron cambios", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "grabarCambios: Actualizando colección ID: $idColeccion")
        Log.d(TAG, "grabarCambios: Nuevo nombre: $nuevoNombre")
        Log.d(TAG, "grabarCambios: Nueva descripción: $nuevaDescripcion")

        // Mostrar progreso
        mostrarProgreso(true)

        // Llamar al servicio para actualizar la colección
        NetworkCollectionUtils.updateCollection(idColeccion, nuevoNombre, nuevaDescripcion) { success, error ->
            runOnUiThread {
                mostrarProgreso(false)

                if (success) {
                    Log.d(TAG, "grabarCambios: Colección actualizada exitosamente")
                    Toast.makeText(this@EditCollectionActivity, "Colección actualizada exitosamente", Toast.LENGTH_SHORT).show()

                    // Retornar resultado exitoso
                    val resultIntent = Intent().apply {
                        putExtra("coleccion_editada", true)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Log.e(TAG, "grabarCambios: Error al actualizar colección - $error")
                    Toast.makeText(this@EditCollectionActivity, "Error al actualizar la colección: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun eliminarColeccion() {
        Log.d(TAG, "eliminarColeccion: Solicitando eliminación de colección ID: $idColeccion")

        // Mostrar diálogo de confirmación
        android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Colección")
            .setMessage("¿Estás seguro de que quieres eliminar la colección '$nombreOriginal'?")
            .setPositiveButton("Eliminar") { dialog, which ->
                // Llamar al servicio para eliminar la colección
                NetworkCollectionUtils.deleteCollection(idColeccion) { success, error, cantidadObjetos ->
                    runOnUiThread {
                        if (success) {
                            Log.d(TAG, "eliminarColeccion: Colección eliminada exitosamente")
                            Toast.makeText(
                                this@EditCollectionActivity,
                                "Colección '$nombreOriginal' eliminada exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Retornar resultado exitoso (la colección fue eliminada)
                            val resultIntent = Intent().apply {
                                putExtra("coleccion_eliminada", true)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            if (cantidadObjetos != null && cantidadObjetos > 0) {
                                // Error porque la colección tiene objetos
                                Log.w(TAG, "eliminarColeccion: La colección tiene $cantidadObjetos objetos, no se puede eliminar")
                                Toast.makeText(
                                    this@EditCollectionActivity,
                                    "No se puede eliminar la colección porque contiene $cantidadObjetos objeto(s). Debe eliminar o mover todos los objetos primero.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Otro tipo de error
                                Log.e(TAG, "eliminarColeccion: Error al eliminar colección - $error")
                                Toast.makeText(
                                    this@EditCollectionActivity,
                                    "Error al eliminar la colección: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarProgreso(mostrar: Boolean) {
        val btnGrabar = findViewById<Button>(R.id.btnGrabar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        if (mostrar) {
            btnGrabar.isEnabled = false
            btnEliminar.isEnabled = false
            btnGrabar.text = "Guardando..."
        } else {
            btnGrabar.isEnabled = true
            btnEliminar.isEnabled = true
            btnGrabar.text = "Grabar"
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
}