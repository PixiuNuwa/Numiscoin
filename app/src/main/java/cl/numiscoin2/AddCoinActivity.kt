package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class AddCoinActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPais: EditText
    private lateinit var etAnio: EditText
    private lateinit var etValorAdquirido: EditText
    private lateinit var etEstado: EditText
    private lateinit var etFamilia: EditText
    private lateinit var etIdFamilia: EditText
    private lateinit var etVariante: EditText
    private lateinit var etCeca: EditText
    private lateinit var etTipo: EditText
    private lateinit var etDisenador: EditText
    private lateinit var etTotalProducido: EditText
    private lateinit var etValorSinCircular: EditText
    private lateinit var etValorComercial: EditText
    private lateinit var etObservaciones: EditText
    private lateinit var etOrden: EditText
    private lateinit var etAcunada: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var ivFoto2: ImageView
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnSeleccionarFoto2: Button
    private lateinit var btnGuardar: Button

    private var fotoUri: Uri? = null
    private var fotoUri2: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val PICK_IMAGE_REQUEST_2 = 2
    private val TAG = "AddCoinActivity"
    private val gson = Gson()
    private var progressDialog: ProgressDialog? = null

    // Variables para almacenar los datos del usuario
    private var idUsuario: Int = 0
    private var idColeccion: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_coin)

        // Obtener los datos del usuario del Intent
        obtenerDatosUsuario()

        initViews()
        setupListeners()
        setupProgressDialog()
    }

    private fun obtenerDatosUsuario() {
        // Obtener el objeto Usuario del Intent
        val usuario = intent.getParcelableExtra<Usuario>(WelcomeActivity.EXTRA_USUARIO)

        if (usuario != null) {
            idUsuario = usuario.idUsuario.toInt() // Obtener idUsuario del objeto Usuario
            idColeccion = intent.getIntExtra("idColeccion", 0) // Obtener idColeccion del extra individual

            Log.d(TAG, "Datos usuario recibidos - idUsuario: $idUsuario, idColeccion: $idColeccion")

            if (idUsuario == 0 || idColeccion == 0) {
                Log.w(TAG, "Advertencia: Datos incompletos - idUsuario: $idUsuario, idColeccion: $idColeccion")
                Toast.makeText(this, "Error: No se pudo identificar la colección", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Advertencia: No se recibió objeto usuario")
            Toast.makeText(this, "Error: No se pudo identificar el usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPais = findViewById(R.id.etPais)
        etAnio = findViewById(R.id.etAnio)
        etValorAdquirido = findViewById(R.id.etValorAdquirido)
        etEstado = findViewById(R.id.etEstado)
        etFamilia = findViewById(R.id.etFamilia)
        etIdFamilia = findViewById(R.id.etIdFamilia)
        etVariante = findViewById(R.id.etVariante)
        etCeca = findViewById(R.id.etCeca)
        etTipo = findViewById(R.id.etTipo)
        etDisenador = findViewById(R.id.etDisenador)
        etTotalProducido = findViewById(R.id.etTotalProducido)
        etValorSinCircular = findViewById(R.id.etValorSinCircular)
        etValorComercial = findViewById(R.id.etValorComercial)
        etObservaciones = findViewById(R.id.etObservaciones)
        etOrden = findViewById(R.id.etOrden)
        etAcunada = findViewById(R.id.etAcunada)
        ivFoto = findViewById(R.id.ivFoto)
        ivFoto2 = findViewById(R.id.ivFoto2)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        btnSeleccionarFoto2 = findViewById(R.id.btnSeleccionarFoto2)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Subiendo información al servidor...")
        progressDialog?.setCancelable(false)
    }

    private fun showLoading() {
        runOnUiThread {
            progressDialog?.show()
            btnGuardar.isEnabled = false
            btnSeleccionarFoto.isEnabled = false
            btnSeleccionarFoto2.isEnabled = false
        }
    }

    private fun hideLoading() {
        runOnUiThread {
            progressDialog?.dismiss()
            btnGuardar.isEnabled = true
            btnSeleccionarFoto.isEnabled = true
            btnSeleccionarFoto2.isEnabled = true
        }
    }

    private fun setupListeners() {
        btnSeleccionarFoto.setOnClickListener {
            seleccionarFoto(PICK_IMAGE_REQUEST)
        }

        btnSeleccionarFoto2.setOnClickListener {
            seleccionarFoto(PICK_IMAGE_REQUEST_2)
        }

        btnGuardar.setOnClickListener {
            guardarMoneda()
        }
    }

    private fun seleccionarFoto(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    private fun guardarMoneda() {
        // Verificar que tenemos los datos del usuario
        if (idUsuario == 0 || idColeccion == 0) {
            Toast.makeText(this, "Error: No se pudo identificar la colección del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val idPais = etPais.text.toString().trim().toIntOrNull()
        val anio = etAnio.text.toString().trim().toIntOrNull()
        val familia = etFamilia.text.toString().trim()
        val idFamilia = etIdFamilia.text.toString().trim().toIntOrNull()
        val variante = etVariante.text.toString().trim()
        val ceca = etCeca.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val disenador = etDisenador.text.toString().trim()
        val totalProducido = etTotalProducido.text.toString().trim()
        val valorSinCircular = etValorSinCircular.text.toString().trim()
        val valorComercial = etValorComercial.text.toString().trim()
        val valorAdquirido = etValorAdquirido.text.toString().trim()
        val estado = etEstado.text.toString().trim()
        val observaciones = etObservaciones.text.toString().trim()
        val orden = etOrden.text.toString().trim().toIntOrNull()
        val acunada = etAcunada.text.toString().trim()

        if (nombre.isEmpty() || idPais == null || anio == null) {
            Toast.makeText(this, "Por favor complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar loading
        showLoading()

        // Crear objeto MonedaRequest usando el idColeccion del usuario
        val monedaRequest = MonedaRequest(
            nombre = nombre,
            descripcion = if (descripcion.isEmpty()) null else descripcion,
            idPais = idPais,
            anio = anio,
            idTipoObjeto = 1, // Siempre 1 para monedas
            idUsuario = idUsuario,
            idColeccion = idColeccion,
            familia = if (familia.isEmpty()) null else familia,
            idFamilia = idFamilia,
            variante = if (variante.isEmpty()) null else variante,
            ceca = if (ceca.isEmpty()) null else ceca,
            tipo = if (tipo.isEmpty()) null else tipo,
            disenador = if (disenador.isEmpty()) null else disenador,
            totalProducido = if (totalProducido.isEmpty()) null else totalProducido,
            valorSinCircular = if (valorSinCircular.isEmpty()) null else valorSinCircular,
            valorComercial = if (valorComercial.isEmpty()) null else valorComercial,
            valorAdquirido = if (valorAdquirido.isEmpty()) null else valorAdquirido,
            estado = if (estado.isEmpty()) null else estado,
            observaciones = if (observaciones.isEmpty()) null else observaciones,
            orden = orden,
            acunada = if (acunada.isEmpty()) null else acunada
        )

        // Enviar datos al servidor en un hilo separado
        Thread {
            try {
                enviarDatosAlServidor(monedaRequest)
            } catch (e: Exception) {
                runOnUiThread {
                    hideLoading()
                    setResult(Activity.RESULT_CANCELED)
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }

    private fun enviarDatosAlServidor(monedaRequest: MonedaRequest) {
        val url = URL("https://5147bbbf57c8.ngrok-free.app/api/jdbc/monedas")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            // Convertir objeto a JSON
            val json = gson.toJson(monedaRequest)
            Log.d(TAG, "Enviando JSON: $json")

            // Escribir el JSON en el cuerpo de la solicitud
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(json)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage

            Log.d(TAG, "Código de respuesta Creacion objeto moneda: $responseCode")
            Log.d(TAG, "Mensaje de respuesta: $responseMessage")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Respuesta del servidor: $response")

                // Parsear la respuesta JSON
                val responseObj = gson.fromJson(response, Map::class.java)
                val success = responseObj["success"] as? Boolean
                val message = responseObj["message"] as? String
                val idObjeto = (responseObj["idObjeto"] as? Number)?.toLong()

                runOnUiThread {
                    if (success == true && idObjeto != null) {
                        Toast.makeText(this, message ?: "Moneda creada exitosamente", Toast.LENGTH_SHORT).show()

                        // Si hay fotos seleccionadas, enviarlas al servidor
                        if (fotoUri != null || fotoUri2 != null) {
                            Log.d(TAG, "Hay fotos, enviando al servidor con objeto:${idObjeto}")
                            Thread {
                                try {
                                    Log.d(TAG, "Llamando a subir fotos")
                                    enviarFotosAlServidor(idObjeto)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error al enviar fotos: ${e.message}")
                                    hideLoading()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }.start()
                        } else {
                            Log.d(TAG, "No hay fotos para subir")
                            hideLoading()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    } else {
                        hideLoading()
                        setResult(Activity.RESULT_CANCELED)
                        Toast.makeText(this, message ?: "Error al crear la moneda", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalles"

                Log.e(TAG, "Error del servidor: $errorResponse")
                runOnUiThread {
                    hideLoading()
                    setResult(Activity.RESULT_CANCELED)
                    Toast.makeText(this, "Error del servidor: $responseCode", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error en la conexión: ${e.message}")
            runOnUiThread {
                hideLoading()
                setResult(Activity.RESULT_CANCELED)
                Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        } finally {
            connection.disconnect()
        }
    }

    // NUEVO MÉTODO: Enviar ambas fotos al servidor
    private fun enviarFotosAlServidor(idObjeto: Long) {
        val fotos = mutableListOf<Uri>()

        // Agregar las fotos que existen a la lista
        fotoUri?.let { fotos.add(it) }
        fotoUri2?.let { fotos.add(it) }

        var fotosSubidasExitosamente = 0
        var errores = mutableListOf<String>()

        for ((index, fotoUri) in fotos.withIndex()) {
            try {
                enviarFotoIndividualAlServidor(idObjeto, fotoUri, index + 1)
                fotosSubidasExitosamente++
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar foto ${index + 1}: ${e.message}")
                errores.add("Foto ${index + 1}: ${e.message}")
            }
        }

        runOnUiThread {
            hideLoading()

            if (fotosSubidasExitosamente == fotos.size) {
                Toast.makeText(this, "Todas las fotos subidas exitosamente", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
            } else if (fotosSubidasExitosamente > 0) {
                Toast.makeText(this, "${fotosSubidasExitosamente} de ${fotos.size} fotos subidas, pero con errores", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_OK)
            } else {
                Toast.makeText(this, "Error al subir todas las fotos", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK) // Aún así consideramos éxito porque la moneda se creó
            }

            finish()
        }
    }

    // MÉTODO MODIFICADO: Para enviar una foto individual
    private fun enviarFotoIndividualAlServidor(idObjeto: Long, fotoUri: Uri, numeroFoto: Int) {
        Log.d(TAG, "Subiendo foto $numeroFoto al servidor con id: $idObjeto")
        val url = URL("https://5147bbbf57c8.ngrok-free.app/api/jdbc/upload/images")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.useCaches = false

            val boundary = "---------------------------${System.currentTimeMillis()}"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val outputStream = DataOutputStream(connection.outputStream)

            // Agregar parámetro idObjeto
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"idObjeto\"\r\n\r\n")
            outputStream.writeBytes("$idObjeto\r\n")
            outputStream.flush()

            // Agregar archivo de imagen con nombre único para cada foto
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"images\"; filename=\"foto_${idObjeto}_$numeroFoto.jpg\"\r\n")
            outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            outputStream.flush()

            // Escribir los bytes de la imagen
            val inputStream = contentResolver.openInputStream(fotoUri)
            inputStream?.use { input ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }

            outputStream.writeBytes("\r\n")
            outputStream.writeBytes("--$boundary--\r\n")
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Foto $numeroFoto subida exitosamente: $response")
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalles"
                Log.e(TAG, "Error al subir foto $numeroFoto: $responseCode - $errorResponse")
                throw IOException("Error $responseCode: $errorResponse")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar foto $numeroFoto: ${e.message}")
            throw e
        } finally {
            connection.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    fotoUri = data.data
                    ivFoto.setImageURI(fotoUri)
                    ivFoto.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_2 -> {
                    fotoUri2 = data.data
                    ivFoto2.setImageURI(fotoUri2)
                    ivFoto2.visibility = ImageView.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }
}
/*package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class AddCoinActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etPais: EditText
    private lateinit var etAnio: EditText
    private lateinit var etValorAdquirido: EditText
    private lateinit var etEstado: EditText
    private lateinit var etFamilia: EditText
    private lateinit var etIdFamilia: EditText
    private lateinit var etVariante: EditText
    private lateinit var etCeca: EditText
    private lateinit var etTipo: EditText
    private lateinit var etDisenador: EditText
    private lateinit var etTotalProducido: EditText
    private lateinit var etValorSinCircular: EditText
    private lateinit var etValorComercial: EditText
    private lateinit var etObservaciones: EditText
    private lateinit var etOrden: EditText
    private lateinit var etAcunada: EditText
    private lateinit var ivFoto: ImageView
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnGuardar: Button

    private var fotoUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val TAG = "AddCoinActivity"
    private val gson = Gson()
    private var progressDialog: ProgressDialog? = null

    // Variables para almacenar los datos del usuario
    private var idUsuario: Int = 0
    private var idColeccion: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_coin)

        // Obtener los datos del usuario del Intent
        obtenerDatosUsuario()

        initViews()
        setupListeners()
        setupProgressDialog()
    }

    private fun obtenerDatosUsuario() {
        // Obtener el objeto Usuario del Intent
        val usuario = intent.getParcelableExtra<Usuario>(WelcomeActivity.EXTRA_USUARIO)

        if (usuario != null) {
            idUsuario = usuario.idUsuario.toInt() // Obtener idUsuario del objeto Usuario
            idColeccion = intent.getIntExtra("idColeccion", 0) // Obtener idColeccion del extra individual

            Log.d(TAG, "Datos usuario recibidos - idUsuario: $idUsuario, idColeccion: $idColeccion")

            if (idUsuario == 0 || idColeccion == 0) {
                Log.w(TAG, "Advertencia: Datos incompletos - idUsuario: $idUsuario, idColeccion: $idColeccion")
                Toast.makeText(this, "Error: No se pudo identificar la colección", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Advertencia: No se recibió objeto usuario")
            Toast.makeText(this, "Error: No se pudo identificar el usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etPais = findViewById(R.id.etPais)
        etAnio = findViewById(R.id.etAnio)
        etValorAdquirido = findViewById(R.id.etValorAdquirido)
        etEstado = findViewById(R.id.etEstado)
        etFamilia = findViewById(R.id.etFamilia)
        etIdFamilia = findViewById(R.id.etIdFamilia)
        etVariante = findViewById(R.id.etVariante)
        etCeca = findViewById(R.id.etCeca)
        etTipo = findViewById(R.id.etTipo)
        etDisenador = findViewById(R.id.etDisenador)
        etTotalProducido = findViewById(R.id.etTotalProducido)
        etValorSinCircular = findViewById(R.id.etValorSinCircular)
        etValorComercial = findViewById(R.id.etValorComercial)
        etObservaciones = findViewById(R.id.etObservaciones)
        etOrden = findViewById(R.id.etOrden)
        etAcunada = findViewById(R.id.etAcunada)
        ivFoto = findViewById(R.id.ivFoto)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun setupProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Subiendo información al servidor...")
        progressDialog?.setCancelable(false)
    }

    private fun showLoading() {
        runOnUiThread {
            progressDialog?.show()
            btnGuardar.isEnabled = false
            btnSeleccionarFoto.isEnabled = false
        }
    }

    private fun hideLoading() {
        runOnUiThread {
            progressDialog?.dismiss()
            btnGuardar.isEnabled = true
            btnSeleccionarFoto.isEnabled = true
        }
    }

    private fun setupListeners() {
        btnSeleccionarFoto.setOnClickListener {
            seleccionarFoto()
        }

        btnGuardar.setOnClickListener {
            guardarMoneda()
        }
    }

    private fun seleccionarFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun guardarMoneda() {
        // Verificar que tenemos los datos del usuario
        if (idUsuario == 0 || idColeccion == 0) {
            Toast.makeText(this, "Error: No se pudo identificar la colección del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val idPais = etPais.text.toString().trim().toIntOrNull()
        val anio = etAnio.text.toString().trim().toIntOrNull()
        val familia = etFamilia.text.toString().trim()
        val idFamilia = etIdFamilia.text.toString().trim().toIntOrNull()
        val variante = etVariante.text.toString().trim()
        val ceca = etCeca.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val disenador = etDisenador.text.toString().trim()
        val totalProducido = etTotalProducido.text.toString().trim()
        val valorSinCircular = etValorSinCircular.text.toString().trim()
        val valorComercial = etValorComercial.text.toString().trim()
        val valorAdquirido = etValorAdquirido.text.toString().trim()
        val estado = etEstado.text.toString().trim()
        val observaciones = etObservaciones.text.toString().trim()
        val orden = etOrden.text.toString().trim().toIntOrNull()
        val acunada = etAcunada.text.toString().trim()

        if (nombre.isEmpty() || idPais == null || anio == null) {
            Toast.makeText(this, "Por favor complete los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar loading
        showLoading()

        // Crear objeto MonedaRequest usando el idColeccion del usuario
        val monedaRequest = MonedaRequest(
            nombre = nombre,
            descripcion = if (descripcion.isEmpty()) null else descripcion,
            idPais = idPais,
            anio = anio,
            idTipoObjeto = 1, // Siempre 1 para monedas
            idUsuario = idUsuario,
            idColeccion = idColeccion, // ← Aquí usamos el idColeccion del usuario
            familia = if (familia.isEmpty()) null else familia,
            idFamilia = idFamilia,
            variante = if (variante.isEmpty()) null else variante,
            ceca = if (ceca.isEmpty()) null else ceca,
            tipo = if (tipo.isEmpty()) null else tipo,
            disenador = if (disenador.isEmpty()) null else disenador,
            totalProducido = if (totalProducido.isEmpty()) null else totalProducido,
            valorSinCircular = if (valorSinCircular.isEmpty()) null else valorSinCircular,
            valorComercial = if (valorComercial.isEmpty()) null else valorComercial,
            valorAdquirido = if (valorAdquirido.isEmpty()) null else valorAdquirido,
            estado = if (estado.isEmpty()) null else estado,
            observaciones = if (observaciones.isEmpty()) null else observaciones,
            orden = orden,
            acunada = if (acunada.isEmpty()) null else acunada
        )

        // Enviar datos al servidor en un hilo separado
        Thread {
            try {
                enviarDatosAlServidor(monedaRequest)
            } catch (e: Exception) {
                runOnUiThread {
                    hideLoading()
                    Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun enviarDatosAlServidor(monedaRequest: MonedaRequest) {
        val url = URL("https://5147bbbf57c8.ngrok-free.app/api/jdbc/monedas")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            // Convertir objeto a JSON
            val json = gson.toJson(monedaRequest)
            Log.d(TAG, "Enviando JSON: $json")

            // Escribir el JSON en el cuerpo de la solicitud
            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(json)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage

            Log.d(TAG, "Código de respuesta Creacion objeto moneda: $responseCode")
            Log.d(TAG, "Mensaje de respuesta: $responseMessage")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Respuesta del servidor: $response")

                // Parsear la respuesta JSON
                val responseObj = gson.fromJson(response, Map::class.java)
                val success = responseObj["success"] as? Boolean
                val message = responseObj["message"] as? String
                val idObjeto = (responseObj["idObjeto"] as? Number)?.toLong()

                runOnUiThread {
                    if (success == true && idObjeto != null) {
                        Toast.makeText(this, message ?: "Moneda creada exitosamente", Toast.LENGTH_SHORT).show()

                        // Si hay una foto seleccionada, enviarla al servidor
                        if (fotoUri != null) {
                            Log.d(TAG, "Hay foto, enviando al servidor con objeto:${idObjeto}")
                            Thread {
                                try {
                                    Log.d(TAG, "Llamando a subir fotos: ${fotoUri} / ${idObjeto}")
                                    enviarFotoAlServidor(idObjeto, fotoUri!!)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error al enviar foto: ${e.message}")
                                    hideLoading()
                                }
                            }.start()
                        } else {
                            Log.d(TAG, "No hay foto para subir")
                            hideLoading()
                            finish()
                        }
                    } else {
                        hideLoading()
                        Toast.makeText(this, message ?: "Error al crear la moneda", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalles"

                Log.e(TAG, "Error del servidor: $errorResponse")
                runOnUiThread {
                    hideLoading()
                    Toast.makeText(this, "Error del servidor: $responseCode", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error en la conexión: ${e.message}")
            runOnUiThread {
                hideLoading()
                Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun enviarFotoAlServidor(idObjeto: Long, fotoUri: Uri) {
        Log.d(TAG, "Solicitando al servidor por cada foto con id: $idObjeto")
        val url = URL("https://5147bbbf57c8.ngrok-free.app/api/jdbc/upload/images")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.useCaches = false

            val boundary = "---------------------------${System.currentTimeMillis()}"
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val outputStream = DataOutputStream(connection.outputStream)

            // Agregar parámetro idObjeto
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"idObjeto\"\r\n\r\n")
            outputStream.writeBytes("$idObjeto\r\n")
            outputStream.flush()

            // Agregar archivo de imagen
            outputStream.writeBytes("--$boundary\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"images\"; filename=\"a_foto_$idObjeto.jpg\"\r\n")
            outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            outputStream.flush()

            // Escribir los bytes de la imagen
            val inputStream = contentResolver.openInputStream(fotoUri)
            inputStream?.use { input ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }

            outputStream.writeBytes("\r\n")
            outputStream.writeBytes("--$boundary--\r\n")
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Foto subida exitosamente: $response")
                runOnUiThread {
                    hideLoading()
                    Toast.makeText(this, "Foto subida exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Sin detalles"
                Log.e(TAG, "Error al subir foto: $responseCode - $errorResponse")
                runOnUiThread {
                    hideLoading()
                    Toast.makeText(this, "Error al subir foto: $responseCode", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar foto: ${e.message}")
            runOnUiThread {
                hideLoading()
                Toast.makeText(this, "Error al subir foto: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        } finally {
            connection.disconnect()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            fotoUri = data.data
            ivFoto.setImageURI(fotoUri)
            ivFoto.visibility = ImageView.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurarse de que el diálogo se cierre al destruir la actividad
        progressDialog?.dismiss()
    }
}
*/