package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cl.numiscoin2.network.NetworkDataUtils
import cl.numiscoin2.network.NetworkObjectUtils
import com.google.gson.Gson

class AddCoinActivity : BaseActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var spPais: Spinner
    private lateinit var etAnio: EditText
    private lateinit var etValorAdquirido: EditText
    private lateinit var etEstado: EditText
    //private lateinit var etFamilia: EditText
    //private lateinit var etIdFamilia: EditText
    private lateinit var etVariante: EditText
    private lateinit var etCeca: EditText
    private lateinit var etTipo: EditText
    private lateinit var etDisenador: EditText
    private lateinit var etTotalProducido: EditText
    //private lateinit var etValorSinCircular: EditText
    private lateinit var etValorComercial: EditText
    private lateinit var etObservaciones: EditText
    //private lateinit var etOrden: EditText
    //private lateinit var etAcunada: EditText
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
    private var idUsuario: Long = 0L
    private var idColeccion: Int = 0

    private var listaPaises: List<Pais> = emptyList()
    private var paisesCargados = false

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
        setContentView(R.layout.activity_add_coin)

        setupBottomMenu()
        highlightMenuItem(R.id.menuCollection)

        // Obtener los datos del usuario del Intent
        obtenerDatosUsuario()

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }
        initViews()
        cargarPaises()
        setupListeners()
        setupProgressDialog()


    }

    private fun obtenerDatosUsuario() {
        // Obtener el objeto Usuario del Intent
        val usuario = SessionManager.usuario

        if (usuario != null) {
            idUsuario = usuario.idUsuario // Obtener idUsuario del objeto Usuario

            // CAMBIO AQUÍ: Obtener como Long y convertir a Int
            idColeccion = intent.getLongExtra("idColeccion", 0L).toInt()
            // O también puedes usar: idColeccion = intent.getIntExtra("idColeccion", 0)

            Log.d(TAG, "Datos usuario recibidos - idUsuario: $idUsuario, idColeccion: $idColeccion")

            if (idUsuario == 0L || idColeccion == 0) {
                Log.w(TAG, "Advertencia: Datos incompletos - idUsuario: $idUsuario, idColeccion: $idColeccion")
                Toast.makeText(this, "Error: No se pudo identificar la colección", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Advertencia: No se recibió objeto usuario")
            Toast.makeText(this, "Error: No se pudo identificar el usuario", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        spPais = findViewById(R.id.spPais)
        etAnio = findViewById(R.id.etAnio)
        etValorAdquirido = findViewById(R.id.etValorAdquirido)
        etEstado = findViewById(R.id.etEstado)
        //etFamilia = findViewById(R.id.etFamilia)
        //etIdFamilia = findViewById(R.id.etIdFamilia)
        etVariante = findViewById(R.id.etVariante)
        etCeca = findViewById(R.id.etCeca)
        etTipo = findViewById(R.id.etTipo)
        etDisenador = findViewById(R.id.etDisenador)
        etTotalProducido = findViewById(R.id.etTotalProducido)
        //etValorSinCircular = findViewById(R.id.etValorSinCircular)
        etValorComercial = findViewById(R.id.etValorComercial)
        etObservaciones = findViewById(R.id.etObservaciones)
        //etOrden = findViewById(R.id.etOrden)
        //etAcunada = findViewById(R.id.etAcunada)

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
        startActivityForResult(ImageUtils.crearIntentGaleria(), requestCode)
    }

    private fun tomarFoto(requestCode: Int) {
        startActivityForResult(ImageUtils.crearIntentCamara(this), requestCode)
    }

    /*@Throws(IOException::class)
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
    }*/



    private fun guardarMoneda() {
        // Verificar que tenemos los datos del usuario
        if (idUsuario == 0L || idColeccion == 0) {
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
        val anio = etAnio.text.toString().trim()
        //val familia = etFamilia.text.toString().trim()
        //val idFamilia = etIdFamilia.text.toString().trim().toIntOrNull()
        val variante = etVariante.text.toString().trim()
        val ceca = etCeca.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val disenador = etDisenador.text.toString().trim()
        val totalProducido = etTotalProducido.text.toString().trim()
        //val valorSinCircular = etValorSinCircular.text.toString().trim()
        val valorComercial = etValorComercial.text.toString().trim()
        val valorAdquirido = etValorAdquirido.text.toString().trim()
        val estado = etEstado.text.toString().trim()
        val observaciones = etObservaciones.text.toString().trim()
        //val orden = etOrden.text.toString().trim().toIntOrNull()
        //val acunada = etAcunada.text.toString().trim()

        // Validar campos obligatorios
        val errores = mutableListOf<String>()

        if (nombre.isEmpty()) errores.add("El nombre es obligatorio")
        if (descripcion.isEmpty()) errores.add("La descripción es obligatoria")
        if (idPais == null) errores.add("Debe seleccionar un país")
        if (anio.isEmpty()) errores.add("El año es obligatorio")
        if (variante.isEmpty()) errores.add("La variante es obligatoria")
        if (ceca.isEmpty()) errores.add("La ceca es obligatoria")
        if (tipo.isEmpty()) errores.add("El tipo es obligatorio")
        if (disenador.isEmpty()) errores.add("El grabador es obligatorio")
        if (totalProducido.isEmpty()) errores.add("El total producido es obligatorio")
        if (valorComercial.isEmpty()) errores.add("El valor comercial es obligatorio")
        if (valorAdquirido.isEmpty()) errores.add("El valor adquirido es obligatorio")
        if (estado.isEmpty()) errores.add("El estado es obligatorio")
        if (observaciones.isEmpty()) errores.add("Las observaciones son obligatorias")

        // Validar formatos numéricos
        val anioInt = anio.toIntOrNull()
        if (anioInt == null) errores.add("El año debe ser un número válido")

        val totalProducidoInt = totalProducido.toIntOrNull()
        if (totalProducidoInt == null) errores.add("El total producido debe ser un número válido")

        val valorComercialInt = valorComercial.toIntOrNull()
        if (valorComercialInt == null) errores.add("El valor comercial debe ser un número válido")

        val valorAdquiridoInt = valorAdquirido.toIntOrNull()
        if (valorAdquiridoInt == null) errores.add("El valor adquirido debe ser un número válido")

        // Mostrar errores si existen
        if (errores.isNotEmpty()) {
            val mensajeError = buildString {
                append("Por favor corrija los siguientes errores:\n")
                errores.forEachIndexed { index, error ->
                    append("${index + 1}. $error\n")
                }
            }
            Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show()
            return
        }

        // Mostrar loading
        showLoading()

        // Crear objeto MonedaRequest usando el idColeccion del usuario
        val monedaRequest = MonedaRequest(
            nombre = nombre,
            descripcion = if (descripcion.isEmpty()) null else descripcion,
            idPais = idPais!!,
            anio = anioInt!!,
            idTipoObjeto = 1, // Siempre 1 para monedas
            idUsuario = idUsuario.toInt(),
            idColeccion = idColeccion,
            familia = "NA",
            idFamilia = 0,
            variante = if (variante.isEmpty()) null else variante,
            ceca = if (ceca.isEmpty()) null else ceca,
            tipo = if (tipo.isEmpty()) null else tipo,
            disenador = if (disenador.isEmpty()) null else disenador,
            totalProducido = totalProducidoInt!!,
            valorSinCircular = 0,
            valorComercial = valorComercialInt!!,
            valorAdquirido = valorAdquiridoInt!!,
            estado = if (estado.isEmpty()) null else estado,
            observaciones = if (observaciones.isEmpty()) null else observaciones,
            orden = 0,
            acunada = "NA"
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
        NetworkObjectUtils.createMoneda(monedaRequest) { idObjeto, error ->
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

            NetworkObjectUtils.uploadPhoto(idObjeto, fotoUri, this, numeroFoto) { success, error ->
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        ImageUtils.manejarResultadoFoto(this, requestCode, resultCode, data) { uri, fotoIndex ->
            if (uri != null) {
                when (fotoIndex) {
                    1 -> {
                        fotoUri = uri
                        ivFoto.setImageURI(uri)
                        ivFoto.visibility = ImageView.VISIBLE
                    }
                    2 -> {
                        fotoUri2 = uri
                        ivFoto2.setImageURI(uri)
                        ivFoto2.visibility = ImageView.VISIBLE
                    }
                    3 -> {
                        fotoUri3 = uri
                        ivFoto3.setImageURI(uri)
                        ivFoto3.visibility = ImageView.VISIBLE
                    }
                    4 -> {
                        fotoUri4 = uri
                        ivFoto4.setImageURI(uri)
                        ivFoto4.visibility = ImageView.VISIBLE
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        progressDialog?.dismiss()
    }

    private fun cargarPaises() {
        showLoading()

        NetworkDataUtils.getPaises { paises, error ->
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

    fun String.toIntOrNull(): Int? {
        return try {
            this.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }
}
