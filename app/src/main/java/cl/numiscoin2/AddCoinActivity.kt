package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddCoinActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var spPais: Spinner
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
    private lateinit var ivFoto3: ImageView
    private lateinit var ivFoto4: ImageView
    private lateinit var btnGaleria1: Button
    private lateinit var btnCamara1: Button
    private lateinit var btnGaleria2: Button
    private lateinit var btnCamara2: Button
    private lateinit var btnGaleria3: Button
    private lateinit var btnCamara3: Button
    private lateinit var btnGaleria4: Button
    private lateinit var btnCamara4: Button
    private lateinit var btnGuardar: Button

    private var fotoUri: Uri? = null
    private var fotoUri2: Uri? = null
    private var fotoUri3: Uri? = null
    private var fotoUri4: Uri? = null

    private val PICK_IMAGE_REQUEST_1 = 1
    private val PICK_IMAGE_REQUEST_2 = 2
    private val PICK_IMAGE_REQUEST_3 = 3
    private val PICK_IMAGE_REQUEST_4 = 4
    private val TAKE_PHOTO_REQUEST_1 = 101
    private val TAKE_PHOTO_REQUEST_2 = 102
    private val TAKE_PHOTO_REQUEST_3 = 103
    private val TAKE_PHOTO_REQUEST_4 = 104

    private var currentPhotoPath: String? = null
    private var currentPhotoRequestCode: Int = 0

    private val TAG = "AddCoinActivity"
    private val gson = Gson()
    private var progressDialog: ProgressDialog? = null

    // Variables para almacenar los datos del usuario
    private var idUsuario: Int = 0
    private var idColeccion: Int = 0

    private var listaPaises: List<Pais> = emptyList()
    private var paisesCargados = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_coin)

        // Obtener los datos del usuario del Intent
        obtenerDatosUsuario()

        initViews()
        cargarPaises()
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
        spPais = findViewById(R.id.spPais)
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
        ivFoto3 = findViewById(R.id.ivFoto3)
        ivFoto4 = findViewById(R.id.ivFoto4)

        btnGaleria1 = findViewById(R.id.btnGaleria1)
        btnCamara1 = findViewById(R.id.btnCamara1)
        btnGaleria2 = findViewById(R.id.btnGaleria2)
        btnCamara2 = findViewById(R.id.btnCamara2)
        btnGaleria3 = findViewById(R.id.btnGaleria3)
        btnCamara3 = findViewById(R.id.btnCamara3)
        btnGaleria4 = findViewById(R.id.btnGaleria4)
        btnCamara4 = findViewById(R.id.btnCamara4)

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
            btnGaleria1.isEnabled = false
            btnCamara1.isEnabled = false
            btnGaleria2.isEnabled = false
            btnCamara2.isEnabled = false
            btnGaleria3.isEnabled = false
            btnCamara3.isEnabled = false
            btnGaleria4.isEnabled = false
            btnCamara4.isEnabled = false
        }
    }

    private fun hideLoading() {
        runOnUiThread {
            progressDialog?.dismiss()
            btnGuardar.isEnabled = true
            btnGaleria1.isEnabled = true
            btnCamara1.isEnabled = true
            btnGaleria2.isEnabled = true
            btnCamara2.isEnabled = true
            btnGaleria3.isEnabled = true
            btnCamara3.isEnabled = true
            btnGaleria4.isEnabled = true
            btnCamara4.isEnabled = true
        }
    }

    private fun setupListeners() {
        btnGaleria1.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_1) }
        btnCamara1.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_1) }

        btnGaleria2.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_2) }
        btnCamara2.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_2) }

        btnGaleria3.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_3) }
        btnCamara3.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_3) }

        btnGaleria4.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_4) }
        btnCamara4.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_4) }

        btnGuardar.setOnClickListener {
            guardarMoneda()
        }
    }

    private fun seleccionarFoto(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    /*private fun tomarFoto(requestCode: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error creando archivo", Toast.LENGTH_SHORT).show()
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    currentPhotoRequestCode = requestCode
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }*/
    private fun tomarFoto(requestCode: Int) {
        val intent = Intent(this, CameraWithOverlayActivity::class.java)
        currentPhotoRequestCode = requestCode
        startActivityForResult(intent, requestCode)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun procesarFotoCircular(imagePath: String): Uri? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            // Calcular escala para evitar OutOfMemory
            var scale = 1
            while (options.outWidth / scale / 2 >= 1024 && options.outHeight / scale / 2 >= 1024) {
                scale *= 2
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            val originalBitmap = BitmapFactory.decodeFile(imagePath, options)

            // Crear bitmap cuadrado del tamaño del círculo
            val size = minOf(originalBitmap.width, originalBitmap.height)
            val circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(circularBitmap)

            // Calcular posición para centrar el círculo
            val left = (originalBitmap.width - size) / 2
            val top = (originalBitmap.height - size) / 2
            val srcRect = Rect(left, top, left + size, top + size)
            val dstRect = Rect(0, 0, size, size)

            // Dibujar la parte circular
            canvas.drawBitmap(originalBitmap, srcRect, dstRect, null)

            // Guardar la imagen procesada
            val file = File.createTempFile(
                "circular_${System.currentTimeMillis()}",
                ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            FileOutputStream(file).use { out ->
                circularBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            return Uri.fromFile(file)

        } catch (e: Exception) {
            Log.e(TAG, "Error procesando foto circular: ${e.message}")
            return null
        }
    }

    private fun guardarMoneda() {
        // Verificar que tenemos los datos del usuario
        if (idUsuario == 0 || idColeccion == 0) {
            Toast.makeText(this, "Error: No se pudo identificar la colección del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        if (!paisesCargados) {
            Toast.makeText(this, "Espere a que se carguen los países", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = etNombre.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val idPais = obtenerIdPaisSeleccionado()
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

    // ... (código anterior sin cambios)

    private fun enviarDatosAlServidor(monedaRequest: MonedaRequest) {
        NetworkUtils.createMoneda(monedaRequest) { idObjeto, error ->
            runOnUiThread {
                if (error != null) {
                    hideLoading()
                    setResult(Activity.RESULT_CANCELED)
                    Toast.makeText(this@AddCoinActivity, error, Toast.LENGTH_SHORT).show()
                    finish()
                    return@runOnUiThread
                }

                if (idObjeto != null) {
                    Toast.makeText(this@AddCoinActivity, "Moneda creada exitosamente", Toast.LENGTH_SHORT).show()

                    // Si hay fotos seleccionadas, enviarlas al servidor
                    if (fotoUri != null || fotoUri2 != null || fotoUri3 != null || fotoUri4 != null) {
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
                    Toast.makeText(this@AddCoinActivity, "Error al crear la moneda", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }


    private fun enviarFotosAlServidor(idObjeto: Long) {
        val fotos = mutableListOf<Pair<Uri, Int>>()

        // Agregar las fotos que existen a la lista con su número correspondiente
        fotoUri?.let { fotos.add(Pair(it, 1)) }
        fotoUri2?.let { fotos.add(Pair(it, 2)) }
        fotoUri3?.let { fotos.add(Pair(it, 3)) }
        fotoUri4?.let { fotos.add(Pair(it, 4)) }

        var fotosSubidasExitosamente = 0
        val totalFotos = fotos.size

        for ((index, fotoPair) in fotos.withIndex()) {
            val fotoUri = fotoPair.first // Extraer el Uri del Pair
            val numeroFoto = fotoPair.second // Extraer el número de foto

            NetworkUtils.uploadPhoto(idObjeto, fotoUri, this, numeroFoto) { success, error ->
                if (success) {
                    fotosSubidasExitosamente++
                    Log.d(TAG, "Foto $numeroFoto subida exitosamente")
                } else {
                    Log.e(TAG, "Error al enviar foto $numeroFoto: $error")
                }

                // Verificar si todas las fotos han sido procesadas
                if (fotosSubidasExitosamente + (index + 1 - fotosSubidasExitosamente) == totalFotos) {
                    runOnUiThread {
                        hideLoading()

                        if (fotosSubidasExitosamente == totalFotos) {
                            Toast.makeText(this@AddCoinActivity, "Todas las fotos subidas exitosamente", Toast.LENGTH_SHORT).show()
                        } else if (fotosSubidasExitosamente > 0) {
                            Toast.makeText(this@AddCoinActivity, "${fotosSubidasExitosamente} de ${totalFotos} fotos subidas, pero con errores", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@AddCoinActivity, "Error al subir todas las fotos", Toast.LENGTH_SHORT).show()
                        }

                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }

        // Si no hay fotos para subir
        if (totalFotos == 0) {
            runOnUiThread {
                hideLoading()
                setResult(Activity.RESULT_OK)
                finish()
            }
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

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_1 -> {
                    fotoUri = data?.data
                    ivFoto.setImageURI(fotoUri)
                    ivFoto.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_2 -> {
                    fotoUri2 = data?.data
                    ivFoto2.setImageURI(fotoUri2)
                    ivFoto2.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_3 -> {
                    fotoUri3 = data?.data
                    ivFoto3.setImageURI(fotoUri3)
                    ivFoto3.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_4 -> {
                    fotoUri4 = data?.data
                    ivFoto4.setImageURI(fotoUri4)
                    ivFoto4.visibility = ImageView.VISIBLE
                }
                in arrayOf(TAKE_PHOTO_REQUEST_1, TAKE_PHOTO_REQUEST_2,
                    TAKE_PHOTO_REQUEST_3, TAKE_PHOTO_REQUEST_4) -> {

                    val photoPath = data?.getStringExtra(CameraWithOverlayActivity.EXTRA_OUTPUT_URI)
                    photoPath?.let { path ->
                        val processedUri = procesarFotoCircular(path)
                        processedUri?.let { uri ->
                            when (requestCode) {
                                TAKE_PHOTO_REQUEST_1 -> {
                                    fotoUri = uri
                                    ivFoto.setImageURI(uri)
                                    ivFoto.visibility = ImageView.VISIBLE
                                }
                                TAKE_PHOTO_REQUEST_2 -> {
                                    fotoUri2 = uri
                                    ivFoto2.setImageURI(uri)
                                    ivFoto2.visibility = ImageView.VISIBLE
                                }
                                TAKE_PHOTO_REQUEST_3 -> {
                                    fotoUri3 = uri
                                    ivFoto3.setImageURI(uri)
                                    ivFoto3.visibility = ImageView.VISIBLE
                                }
                                TAKE_PHOTO_REQUEST_4 -> {
                                    fotoUri4 = uri
                                    ivFoto4.setImageURI(uri)
                                    ivFoto4.visibility = ImageView.VISIBLE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_1 -> {
                    fotoUri = data?.data
                    ivFoto.setImageURI(fotoUri)
                    ivFoto.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_2 -> {
                    fotoUri2 = data?.data
                    ivFoto2.setImageURI(fotoUri2)
                    ivFoto2.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_3 -> {
                    fotoUri3 = data?.data
                    ivFoto3.setImageURI(fotoUri3)
                    ivFoto3.visibility = ImageView.VISIBLE
                }
                PICK_IMAGE_REQUEST_4 -> {
                    fotoUri4 = data?.data
                    ivFoto4.setImageURI(fotoUri4)
                    ivFoto4.visibility = ImageView.VISIBLE
                }
                in arrayOf(TAKE_PHOTO_REQUEST_1, TAKE_PHOTO_REQUEST_2,
                    TAKE_PHOTO_REQUEST_3, TAKE_PHOTO_REQUEST_4) -> {

                    currentPhotoPath?.let { path ->
                        val processedUri = procesarFotoCircular(path)
                        processedUri?.let { uri ->
                            when (requestCode) {
                                TAKE_PHOTO_REQUEST_1 -> {
                                    fotoUri = uri
                                    ivFoto.setImageURI(uri)
                                    ivFoto.visibility = ImageView.VISIBLE
                                }
                                TAKE_PHOTO_REQUEST_2 -> {
                                    fotoUri2 = uri
                                    ivFoto2.setImageURI(uri)
                                    ivFoto2.visibility = ImageView.VISIBLE
                                }
                                TAKE_PHOTO_REQUEST_3 -> {
                                    fotoUri3 = uri
                                    ivFoto3.setImageURI(uri)
                                    ivFoto3.visibility = ImageView.VISIBLE
                                }
                                TAKE_PHOTO_REQUEST_4 -> {
                                    fotoUri4 = uri
                                    ivFoto4.setImageURI(uri)
                                    ivFoto4.visibility = ImageView.VISIBLE
                                }
                            }
                        }
                    }

                    // Eliminar archivo temporal original
                    currentPhotoPath?.let { path ->
                        File(path).delete()
                    }
                }
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }

    private fun cargarPaises() {
        showLoading()

        NetworkUtils.getPaises { paises, error ->
            runOnUiThread {
                if (error != null) {
                    hideLoading()
                    Toast.makeText(this, "Error al cargar países: $error", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                if (paises != null) {
                    listaPaises = paises
                    configurarSpinnerPaises(paises)
                    paisesCargados = true
                }
                hideLoading()
            }
        }
    }

    private fun configurarSpinnerPaises(paises: List<Pais>) {
        val nombresPaises = paises.map { it.nombre }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresPaises)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spPais.adapter = adapter
    }

    private fun obtenerIdPaisSeleccionado(): Int? {
        val selectedPosition = spPais.selectedItemPosition
        return if (selectedPosition >= 0 && selectedPosition < listaPaises.size) {
            listaPaises[selectedPosition].idPais
        } else {
            null
        }
    }
}
