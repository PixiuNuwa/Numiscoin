package cl.numiscoin2.setting

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import cl.numiscoin2.BaseActivity
import cl.numiscoin2.R
import cl.numiscoin2.Usuario
import cl.numiscoin2.network.NetworkConfig
import org.json.JSONObject

class TermsActivity : BaseActivity() {

    private lateinit var termsScrollView: ScrollView
    private lateinit var acceptCheckBox: CheckBox
    private lateinit var acceptButton: Button
    private lateinit var titleTextView: TextView
    private lateinit var termsTextView: TextView
    private lateinit var thanksTextView: TextView


    //private var usuarioId: Long = 0L

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
        setContentView(R.layout.activity_terms)

        // Obtener el usuario de la intent o de donde lo tengas almacenado
        //usuario = intent.getSerializableExtra("usuario") as? Usuario
        //usuarioId = usuario?.idUsuario ?: 0L

        initViews()
        setupBottomMenu()
        highlightMenuItem(R.id.menuHome)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }


        // Verificar estado de términos al cargar
        checkTermsStatus()
    }

    private fun initViews() {
        termsScrollView = findViewById(R.id.termsScrollView)
        acceptCheckBox = findViewById(R.id.acceptCheckBox)
        acceptButton = findViewById(R.id.acceptButton)
        titleTextView = findViewById(R.id.titleTextView)
        termsTextView = findViewById(R.id.termsTextView)
        thanksTextView = findViewById(R.id.thanksTextView)

        // Configurar botón de aceptar
        acceptButton.setOnClickListener {
            if (acceptCheckBox.isChecked) {
                acceptTerms()
            } else {
                Toast.makeText(this, "Debes aceptar los términos para continuar", Toast.LENGTH_SHORT).show()
            }
        }

        // Deshabilitar botón inicialmente
        acceptButton.isEnabled = false

        // Habilitar botón solo cuando se marque el checkbox
        acceptCheckBox.setOnCheckedChangeListener { _, isChecked ->
            acceptButton.isEnabled = isChecked
        }

        // Inicialmente ocultar la vista de agradecimiento
        thanksTextView.visibility = TextView.GONE
    }

    private fun checkTermsStatus() {
        val usuarioId = usuario?.idUsuario ?: 0L


        if (usuarioId <= 0L) {
            showErrorState("No se pudo identificar al usuario")
            return
        }

        showLoadingState()

        // Llamar al endpoint para obtener el estado de términos
        Thread {
            try {
                //val url = java.net.URL("${cl.numiscoin2.network.NetworkConfig.BASE_URL}/api/jdbc/usuarios/terminos?idUsuario=$usuarioId")
                val url = java.net.URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/estado-terminos?idUsuario=$usuarioId")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                val response = if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
                }

                connection.disconnect()

                runOnUiThread {
                    handleTermsStatusResponse(responseCode, response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showErrorState("Error de conexión: ${e.message}")
                }
            }
        }.start()
    }

    private fun handleTermsStatusResponse(responseCode: Int, response: String) {
        hideLoadingState()

        try {
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")

                if (success) {
                    val data = jsonResponse.getJSONObject("data")
                    val haAceptadoTerminos = data.getBoolean("haAceptadoTerminos")

                    if (haAceptadoTerminos) {
                        showThanksState()
                    } else {
                        showTermsFormState()
                    }
                } else {
                    val message = jsonResponse.getString("message")
                    showErrorState("Error: $message")
                    // Mostrar formulario como fallback
                    showTermsFormState()
                }
            } else {
                showErrorState("Error del servidor: $responseCode")
                // Mostrar formulario como fallback
                showTermsFormState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorState("Error al procesar respuesta")
            // Mostrar formulario como fallback
            showTermsFormState()
        }
    }

    private fun acceptTerms() {
        val usuarioId = usuario?.idUsuario ?: 0L
        if (usuarioId <= 0L) {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
            return
        }

        showLoadingState()
        acceptButton.isEnabled = false

        // Llamar al endpoint para aceptar términos
        Thread {
            try {
                //val url = java.net.URL("${cl.numiscoin2.network.NetworkConfig.BASE_URL}/api/jdbc/usuarios/terminos")
                val url = java.net.URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/aceptar-terminos")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                val postData = "idUsuario=$usuarioId"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                val response = if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "{}"
                }

                connection.disconnect()

                runOnUiThread {
                    handleAcceptTermsResponse(responseCode, response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    hideLoadingState()
                    acceptButton.isEnabled = true
                    Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun handleAcceptTermsResponse(responseCode: Int, response: String) {
        hideLoadingState()

        try {
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")

                if (success) {
                    Toast.makeText(this, "Términos aceptados correctamente", Toast.LENGTH_SHORT).show()
                    showThanksState()
                } else {
                    val message = jsonResponse.getString("message")
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                    acceptButton.isEnabled = true
                }
            } else {
                Toast.makeText(this, "Error del servidor: $responseCode", Toast.LENGTH_SHORT).show()
                acceptButton.isEnabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
            acceptButton.isEnabled = true
        }
    }

    private fun showLoadingState() {
        // Puedes implementar un ProgressBar aquí si lo deseas
        acceptButton.text = "Procesando..."
        acceptButton.isEnabled = false
    }

    private fun hideLoadingState() {
        acceptButton.text = "Aceptar Términos"
    }

    private fun showTermsFormState() {
        // Mostrar formulario de términos
        titleTextView.text = "Términos del Servicio"
        termsScrollView.visibility = ScrollView.VISIBLE
        acceptCheckBox.visibility = CheckBox.VISIBLE
        acceptButton.visibility = Button.VISIBLE
        thanksTextView.visibility = TextView.GONE

        // Resetear estado del formulario
        acceptCheckBox.isChecked = false
        acceptButton.isEnabled = false
    }

    private fun showThanksState() {
        // Mostrar mensaje de agradecimiento
        titleTextView.text = "Términos del Servicio"
        termsScrollView.visibility = ScrollView.GONE
        acceptCheckBox.visibility = CheckBox.GONE
        acceptButton.visibility = Button.GONE
        thanksTextView.visibility = TextView.VISIBLE
    }

    private fun showErrorState(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Como fallback, mostrar el formulario de términos
        showTermsFormState()
    }

    companion object {
        fun start(context: Context, usuario: Usuario?) {
            val intent = Intent(context, TermsActivity::class.java)
            usuario?.let {
                intent.putExtra("usuario", it)
            }
            context.startActivity(intent)
        }
    }
}