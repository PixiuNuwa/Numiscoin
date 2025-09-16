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
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditCoinActivity : AppCompatActivity() {

    private lateinit var objeto: ObjetoColeccion
    private val fotosSeleccionadas = mutableListOf<Uri?>()
    private val gson = Gson()

    // Views
    private lateinit var etNombre: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etAnio: EditText
    private lateinit var etFamilia: EditText
    private lateinit var etVariante: EditText
    private lateinit var etCeca: EditText
    private lateinit var etTipo: EditText
    private lateinit var etDisenador: EditText
    private lateinit var etTotalProducido: EditText
    private lateinit var etValorSinCircular: EditText
    private lateinit var etValorComercial: EditText
    private lateinit var etValorAdquirido: EditText
    private lateinit var etEstado: EditText
    private lateinit var etObservaciones: EditText
    private lateinit var etAcunada: EditText

    private lateinit var ivFoto1: ImageView
    private lateinit var ivFoto2: ImageView
    private lateinit var ivFoto3: ImageView
    private lateinit var ivFoto4: ImageView

    private lateinit var btnGaleria1: Button
    private lateinit var btnCamara1: Button
    private lateinit var btnEliminarFoto1: Button

    private lateinit var btnGaleria2: Button
    private lateinit var btnCamara2: Button
    private lateinit var btnEliminarFoto2: Button

    private lateinit var btnGaleria3: Button
    private lateinit var btnCamara3: Button
    private lateinit var btnEliminarFoto3: Button

    private lateinit var btnGaleria4: Button
    private lateinit var btnCamara4: Button
    private lateinit var btnEliminarFoto4: Button

    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    // Variables para fotos
    private var fotoUri1: Uri? = null
    private var fotoUri2: Uri? = null
    private var fotoUri3: Uri? = null
    private var fotoUri4: Uri? = null

    // Request codes
    private val PICK_IMAGE_REQUEST_1 = 1
    private val PICK_IMAGE_REQUEST_2 = 2
    private val PICK_IMAGE_REQUEST_3 = 3
    private val PICK_IMAGE_REQUEST_4 = 4
    private val TAKE_PHOTO_REQUEST_1 = 101
    private val TAKE_PHOTO_REQUEST_2 = 102
    private val TAKE_PHOTO_REQUEST_3 = 103
    private val TAKE_PHOTO_REQUEST_4 = 104

    private var currentPhotoRequestCode: Int = 0

    companion object {
        const val EXTRA_MONEDA_ACTUALIZADA = "moneda_actualizada"
        const val EDIT_COIN_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_coin)

        // Obtener el objeto de la moneda
        objeto = intent.getParcelableExtra("moneda") ?: run {
            Toast.makeText(this, "Error: No se recibió la moneda", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        initFotosSeleccionadas()
        setupUI()
        setupButtons()
    }

    private fun initViews() {
        // Campos de texto
        etNombre = findViewById(R.id.etNombre)
        etDescripcion = findViewById(R.id.etDescripcion)
        etAnio = findViewById(R.id.etAnio)
        etFamilia = findViewById(R.id.etFamilia)
        etVariante = findViewById(R.id.etVariante)
        etCeca = findViewById(R.id.etCeca)
        etTipo = findViewById(R.id.etTipo)
        etDisenador = findViewById(R.id.etDisenador)
        etTotalProducido = findViewById(R.id.etTotalProducido)
        etValorSinCircular = findViewById(R.id.etValorSinCircular)
        etValorComercial = findViewById(R.id.etValorComercial)
        etValorAdquirido = findViewById(R.id.etValorAdquirido)
        etEstado = findViewById(R.id.etEstado)
        etObservaciones = findViewById(R.id.etObservaciones)
        etAcunada = findViewById(R.id.etAcunada)

        // ImageViews
        ivFoto1 = findViewById(R.id.ivFoto1)
        ivFoto2 = findViewById(R.id.ivFoto2)
        ivFoto3 = findViewById(R.id.ivFoto3)
        ivFoto4 = findViewById(R.id.ivFoto4)

        // Botones de fotos
        btnGaleria1 = findViewById(R.id.btnGaleria1)
        btnCamara1 = findViewById(R.id.btnCamara1)
        btnEliminarFoto1 = findViewById(R.id.btnEliminarFoto1)

        btnGaleria2 = findViewById(R.id.btnGaleria2)
        btnCamara2 = findViewById(R.id.btnCamara2)
        btnEliminarFoto2 = findViewById(R.id.btnEliminarFoto2)

        btnGaleria3 = findViewById(R.id.btnGaleria3)
        btnCamara3 = findViewById(R.id.btnCamara3)
        btnEliminarFoto3 = findViewById(R.id.btnEliminarFoto3)

        btnGaleria4 = findViewById(R.id.btnGaleria4)
        btnCamara4 = findViewById(R.id.btnCamara4)
        btnEliminarFoto4 = findViewById(R.id.btnEliminarFoto4)

        // Botones de acción
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun initFotosSeleccionadas() {
        fotosSeleccionadas.clear()
        // Inicializar con las fotos existentes
        for (i in 0 until 4) {
            if (i < (objeto.fotos?.size ?: 0)) {
                val fotoUrl = objeto.fotos?.get(i)?.url
                if (fotoUrl != null) {
                    val uri = Uri.parse(fotoUrl)
                    fotosSeleccionadas.add(uri)
                    when (i) {
                        0 -> fotoUri1 = uri
                        1 -> fotoUri2 = uri
                        2 -> fotoUri3 = uri
                        3 -> fotoUri4 = uri
                    }
                } else {
                    fotosSeleccionadas.add(null)
                }
            } else {
                fotosSeleccionadas.add(null)
            }
        }
    }

    private fun setupUI() {
        // Información básica
        etNombre.setText(objeto.nombre)
        etDescripcion.setText(objeto.descripcion)
        etAnio.setText(objeto.anio.toString())

        // Información de moneda
        objeto.monedaInfo?.let { info ->
            etFamilia.setText(info.familia ?: "")
            etVariante.setText(info.variante ?: "")
            etCeca.setText(info.ceca ?: "")
            etTipo.setText(info.tipo ?: "")
            etDisenador.setText(info.disenador ?: "")
            etTotalProducido.setText(info.totalProducido ?: "")
            etValorSinCircular.setText(info.valorSinCircular ?: "")
            etValorComercial.setText(info.valorComercial ?: "")
            etValorAdquirido.setText(info.valorAdquirido ?: "")
            etEstado.setText(info.estado ?: "")
            etObservaciones.setText(info.observaciones ?: "")
            etAcunada.setText(info.acunada ?: "")
        }

        // Configurar vistas de imágenes
        setupImageViews()
    }

    private fun setupImageViews() {
        val imageViews = listOf(ivFoto1, ivFoto2, ivFoto3, ivFoto4)
        val eliminarButtons = listOf(btnEliminarFoto1, btnEliminarFoto2, btnEliminarFoto3, btnEliminarFoto4)

        for (i in 0 until 4) {
            val uri = fotosSeleccionadas[i]
            if (uri != null) {
                // Cargar imagen existente
                imageViews[i].setImageURI(uri)
                eliminarButtons[i].isVisible = true
            } else {
                imageViews[i].setImageResource(android.R.drawable.ic_menu_add)
                eliminarButtons[i].isVisible = false
            }
        }
    }

    private fun setupButtons() {
        // Botones de galería
        btnGaleria1.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_1) }
        btnGaleria2.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_2) }
        btnGaleria3.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_3) }
        btnGaleria4.setOnClickListener { seleccionarFoto(PICK_IMAGE_REQUEST_4) }

        // Botones de cámara
        btnCamara1.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_1) }
        btnCamara2.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_2) }
        btnCamara3.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_3) }
        btnCamara4.setOnClickListener { tomarFoto(TAKE_PHOTO_REQUEST_4) }

        // Botones de eliminar
        btnEliminarFoto1.setOnClickListener { eliminarFoto(0) }
        btnEliminarFoto2.setOnClickListener { eliminarFoto(1) }
        btnEliminarFoto3.setOnClickListener { eliminarFoto(2) }
        btnEliminarFoto4.setOnClickListener { eliminarFoto(3) }

        // Botones de acción
        btnGuardar.setOnClickListener { guardarCambios() }
        btnCancelar.setOnClickListener { finish() }
    }

    private fun seleccionarFoto(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    private fun tomarFoto(requestCode: Int) {
        val intent = Intent(this, CameraWithOverlayActivity::class.java)
        currentPhotoRequestCode = requestCode
        startActivityForResult(intent, requestCode)
    }

    private fun eliminarFoto(index: Int) {
        fotosSeleccionadas[index] = null
        when (index) {
            0 -> fotoUri1 = null
            1 -> fotoUri2 = null
            2 -> fotoUri3 = null
            3 -> fotoUri4 = null
        }

        val imageView = when (index) {
            0 -> ivFoto1
            1 -> ivFoto2
            2 -> ivFoto3
            3 -> ivFoto4
            else -> null
        }
        imageView?.setImageResource(android.R.drawable.ic_menu_add)

        val btnEliminar = when (index) {
            0 -> btnEliminarFoto1
            1 -> btnEliminarFoto2
            2 -> btnEliminarFoto3
            3 -> btnEliminarFoto4
            else -> null
        }
        btnEliminar?.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST_1 -> {
                    fotoUri1 = data?.data
                    fotoUri1?.let {
                        fotosSeleccionadas[0] = it
                        ivFoto1.setImageURI(it)
                        btnEliminarFoto1.isVisible = true
                    }
                }
                PICK_IMAGE_REQUEST_2 -> {
                    fotoUri2 = data?.data
                    fotoUri2?.let {
                        fotosSeleccionadas[1] = it
                        ivFoto2.setImageURI(it)
                        btnEliminarFoto2.isVisible = true
                    }
                }
                PICK_IMAGE_REQUEST_3 -> {
                    fotoUri3 = data?.data
                    fotoUri3?.let {
                        fotosSeleccionadas[2] = it
                        ivFoto3.setImageURI(it)
                        btnEliminarFoto3.isVisible = true
                    }
                }
                PICK_IMAGE_REQUEST_4 -> {
                    fotoUri4 = data?.data
                    fotoUri4?.let {
                        fotosSeleccionadas[3] = it
                        ivFoto4.setImageURI(it)
                        btnEliminarFoto4.isVisible = true
                    }
                }
                in arrayOf(TAKE_PHOTO_REQUEST_1, TAKE_PHOTO_REQUEST_2,
                    TAKE_PHOTO_REQUEST_3, TAKE_PHOTO_REQUEST_4) -> {

                    val photoPath = data?.getStringExtra(CameraWithOverlayActivity.EXTRA_OUTPUT_URI)
                    photoPath?.let { path ->
                        val processedUri = procesarFotoCircular(path)
                        processedUri?.let { uri ->
                            when (requestCode) {
                                TAKE_PHOTO_REQUEST_1 -> {
                                    fotoUri1 = uri
                                    fotosSeleccionadas[0] = uri
                                    ivFoto1.setImageURI(uri)
                                    btnEliminarFoto1.isVisible = true
                                }
                                TAKE_PHOTO_REQUEST_2 -> {
                                    fotoUri2 = uri
                                    fotosSeleccionadas[1] = uri
                                    ivFoto2.setImageURI(uri)
                                    btnEliminarFoto2.isVisible = true
                                }
                                TAKE_PHOTO_REQUEST_3 -> {
                                    fotoUri3 = uri
                                    fotosSeleccionadas[2] = uri
                                    ivFoto3.setImageURI(uri)
                                    btnEliminarFoto3.isVisible = true
                                }
                                TAKE_PHOTO_REQUEST_4 -> {
                                    fotoUri4 = uri
                                    fotosSeleccionadas[3] = uri
                                    ivFoto4.setImageURI(uri)
                                    btnEliminarFoto4.isVisible = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun procesarFotoCircular(imagePath: String): Uri? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            var scale = 1
            while (options.outWidth / scale / 2 >= 1024 && options.outHeight / scale / 2 >= 1024) {
                scale *= 2
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            val originalBitmap = BitmapFactory.decodeFile(imagePath, options)

            val size = minOf(originalBitmap.width, originalBitmap.height)
            val circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(circularBitmap)

            val left = (originalBitmap.width - size) / 2
            val top = (originalBitmap.height - size) / 2
            val srcRect = Rect(left, top, left + size, top + size)
            val dstRect = Rect(0, 0, size, size)

            canvas.drawBitmap(originalBitmap, srcRect, dstRect, null)

            val file = File.createTempFile(
                "circular_${System.currentTimeMillis()}",
                ".jpg",
                getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            )

            FileOutputStream(file).use { out ->
                circularBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            originalBitmap.recycle()
            circularBitmap.recycle()

            return Uri.fromFile(file)

        } catch (e: Exception) {
            return null
        }
    }

    private fun guardarCambios() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Actualizando moneda...")
            setCancelable(false)
            show()
        }

        // Crear MonedaRequest con los datos del formulario
        val monedaRequest = MonedaRequest(
            nombre = etNombre.text.toString(),
            descripcion = etDescripcion.text.toString(),
            idPais = objeto.idPais,
            anio = etAnio.text.toString().toIntOrNull() ?: 0,
            idTipoObjeto = objeto.idTipoObjeto,
            idUsuario = objeto.idUsuario,
            idColeccion = -1, //objeto.idColeccion,
            familia = etFamilia.text.toString(),
            idFamilia = -1,
            variante = etVariante.text.toString(),
            ceca = etCeca.text.toString(),
            tipo = etTipo.text.toString(),
            disenador = etDisenador.text.toString(),
            totalProducido = etTotalProducido.text.toString(),
            valorSinCircular = etValorSinCircular.text.toString(),
            valorComercial = etValorComercial.text.toString(),
            valorAdquirido = etValorAdquirido.text.toString(),
            estado = etEstado.text.toString(),
            observaciones = etObservaciones.text.toString(),
            orden = -1,
            acunada = etAcunada.text.toString()
        )

        // Primero actualizar la moneda
        NetworkUtils.actualizarMoneda(objeto.id.toLong(), monedaRequest) { success, error ->
            if (success) {
                // Luego subir las fotos nuevas
                subirFotos(progressDialog)
            } else {
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(this@EditCoinActivity, "Error al actualizar: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun subirFotos(progressDialog: ProgressDialog) {
        val fotos = mutableListOf<Pair<Uri, Int>>()

        // Agregar las fotos que existen a la lista con su número correspondiente
        fotoUri1?.let { fotos.add(Pair(it, 1)) }
        fotoUri2?.let { fotos.add(Pair(it, 2)) }
        fotoUri3?.let { fotos.add(Pair(it, 3)) }
        fotoUri4?.let { fotos.add(Pair(it, 4)) }

        var fotosSubidasExitosamente = 0
        val totalFotos = fotos.size

        if (totalFotos == 0) {
            runOnUiThread {
                progressDialog.dismiss()
                finalizarConExito()
            }
            return
        }

        for ((index, fotoPair) in fotos.withIndex()) {
            val fotoUri = fotoPair.first
            val numeroFoto = fotoPair.second

            NetworkUtils.uploadPhoto(objeto.id.toLong(), fotoUri, this, numeroFoto) { success, error ->
                if (success) {
                    fotosSubidasExitosamente++
                }

                // Verificar si todas las fotos han sido procesadas
                if (fotosSubidasExitosamente + (index + 1 - fotosSubidasExitosamente) == totalFotos) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        if (fotosSubidasExitosamente > 0) {
                            finalizarConExito()
                        } else {
                            Toast.makeText(this@EditCoinActivity, "Error al subir las fotos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun finalizarConExito() {
        Toast.makeText(this, "Moneda actualizada exitosamente", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_MONEDA_ACTUALIZADA, true)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}

/*package cl.numiscoin2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.gson.Gson
import java.io.File

class EditCoinActivity : AppCompatActivity() {

    private lateinit var objeto: ObjetoColeccion
    private val fotosSeleccionadas = mutableListOf<Uri?>()
    private val gson = Gson()

    companion object {
        private const val PICK_IMAGE_REQUEST_1 = 101
        private const val PICK_IMAGE_REQUEST_2 = 102
        private const val PICK_IMAGE_REQUEST_3 = 103
        private const val PICK_IMAGE_REQUEST_4 = 104

        const val EXTRA_MONEDA_ACTUALIZADA = "moneda_actualizada"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_coin)

        // Obtener el objeto de la moneda
        objeto = intent.getParcelableExtra("moneda") ?: run {
            Toast.makeText(this, "Error: No se recibió la moneda", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar lista de fotos seleccionadas con las existentes (null para slots vacíos)
        initFotosSeleccionadas()

        // Configurar la UI con los datos actuales
        setupUI()

        // Configurar botones
        setupButtons()
    }

    private fun initFotosSeleccionadas() {
        fotosSeleccionadas.clear()
        // Inicializar con 4 slots (pueden ser null)
        for (i in 0 until 4) {
            if (i < (objeto.fotos?.size ?: 0)) {
                // Para fotos existentes, usamos un URI placeholder
                val fotoUrl = objeto.fotos?.get(i)?.url
                if (fotoUrl != null) {
                    fotosSeleccionadas.add(Uri.parse(fotoUrl))
                } else {
                    fotosSeleccionadas.add(null)
                }
            } else {
                fotosSeleccionadas.add(null)
            }
        }
    }

    private fun setupUI() {
        // Información básica
        findViewById<EditText>(R.id.etNombre).setText(objeto.nombre)
        findViewById<EditText>(R.id.etDescripcion).setText(objeto.descripcion)
        findViewById<EditText>(R.id.etAnio).setText(objeto.anio.toString())

        // Información de moneda
        objeto.monedaInfo?.let { info ->
            findViewById<EditText>(R.id.etFamilia).setText(info.familia ?: "")
            findViewById<EditText>(R.id.etVariante).setText(info.variante ?: "")
            findViewById<EditText>(R.id.etCeca).setText(info.ceca ?: "")
            findViewById<EditText>(R.id.etTipo).setText(info.tipo ?: "")
            findViewById<EditText>(R.id.etDisenador).setText(info.disenador ?: "")
            findViewById<EditText>(R.id.etTotalProducido).setText(info.totalProducido ?: "")
            findViewById<EditText>(R.id.etValorSinCircular).setText(info.valorSinCircular ?: "")
            findViewById<EditText>(R.id.etValorComercial).setText(info.valorComercial ?: "")
            findViewById<EditText>(R.id.etValorAdquirido).setText(info.valorAdquirido ?: "")
            findViewById<EditText>(R.id.etEstado).setText(info.estado ?: "")
            findViewById<EditText>(R.id.etObservaciones).setText(info.observaciones ?: "")
            findViewById<EditText>(R.id.etAcunada).setText(info.acunada ?: "")
        }

        // Configurar vistas de imágenes
        setupImageViews()
    }

    private fun setupImageViews() {
        for (i in 0 until 4) {
            val imageView = when (i) {
                0 -> findViewById<ImageView>(R.id.ivFoto1)
                1 -> findViewById<ImageView>(R.id.ivFoto2)
                2 -> findViewById<ImageView>(R.id.ivFoto3)
                3 -> findViewById<ImageView>(R.id.ivFoto4)
                else -> null
            }

            val btnEliminar = when (i) {
                0 -> findViewById<Button>(R.id.btnEliminarFoto1)
                1 -> findViewById<Button>(R.id.btnEliminarFoto2)
                2 -> findViewById<Button>(R.id.btnEliminarFoto3)
                3 -> findViewById<Button>(R.id.btnEliminarFoto4)
                else -> null
            }

            if (i < (objeto.fotos?.size ?: 0)) {
                // Cargar imagen existente (usar una librería como Glide o Picasso en producción)
                // Por ahora solo mostramos un placeholder
                imageView?.setImageResource(android.R.drawable.ic_menu_gallery)
                btnEliminar?.isVisible = true
            } else {
                imageView?.setImageResource(android.R.drawable.ic_menu_add)
                btnEliminar?.isVisible = false
            }

            // Configurar click listener para seleccionar imagen
            imageView?.setOnClickListener {
                seleccionarImagen(i + 1)
            }

            // Configurar click listener para eliminar imagen
            btnEliminar?.setOnClickListener {
                eliminarFoto(i)
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            guardarCambios()
        }

        findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            finish()
        }
    }

    private fun seleccionarImagen(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, requestCode)
    }

    private fun eliminarFoto(index: Int) {
        fotosSeleccionadas[index] = null
        val imageView = when (index) {
            0 -> findViewById<ImageView>(R.id.ivFoto1)
            1 -> findViewById<ImageView>(R.id.ivFoto2)
            2 -> findViewById<ImageView>(R.id.ivFoto3)
            3 -> findViewById<ImageView>(R.id.ivFoto4)
            else -> null
        }
        imageView?.setImageResource(android.R.drawable.ic_menu_add)

        val btnEliminar = when (index) {
            0 -> findViewById<Button>(R.id.btnEliminarFoto1)
            1 -> findViewById<Button>(R.id.btnEliminarFoto2)
            2 -> findViewById<Button>(R.id.btnEliminarFoto3)
            3 -> findViewById<Button>(R.id.btnEliminarFoto4)
            else -> null
        }
        btnEliminar?.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            val index = when (requestCode) {
                PICK_IMAGE_REQUEST_1 -> 0
                PICK_IMAGE_REQUEST_2 -> 1
                PICK_IMAGE_REQUEST_3 -> 2
                PICK_IMAGE_REQUEST_4 -> 3
                else -> -1
            }

            if (index != -1 && imageUri != null) {
                fotosSeleccionadas[index] = imageUri

                val imageView = when (index) {
                    0 -> findViewById<ImageView>(R.id.ivFoto1)
                    1 -> findViewById<ImageView>(R.id.ivFoto2)
                    2 -> findViewById<ImageView>(R.id.ivFoto3)
                    3 -> findViewById<ImageView>(R.id.ivFoto4)
                    else -> null
                }

                imageView?.setImageURI(imageUri)

                val btnEliminar = when (index) {
                    0 -> findViewById<Button>(R.id.btnEliminarFoto1)
                    1 -> findViewById<Button>(R.id.btnEliminarFoto2)
                    2 -> findViewById<Button>(R.id.btnEliminarFoto3)
                    3 -> findViewById<Button>(R.id.btnEliminarFoto4)
                    else -> null
                }
                btnEliminar?.isVisible = true
            }
        }
    }

    private fun guardarCambios() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage("Actualizando moneda...")
            setCancelable(false)
            show()
        }

        // Crear MonedaRequest con los datos del formulario
        val monedaRequest = MonedaRequest(
            nombre = findViewById<EditText>(R.id.etNombre).text.toString(),
            descripcion = findViewById<EditText>(R.id.etDescripcion).text.toString(),
            idPais = objeto.idPais,
            anio = findViewById<EditText>(R.id.etAnio).text.toString().toIntOrNull() ?: 0,
            idTipoObjeto = objeto.idTipoObjeto,
            idUsuario = objeto.idUsuario,
            idColeccion = -1,
            // Campos opcionales
            familia = findViewById<EditText>(R.id.etFamilia).text.toString(),
            idFamilia = -1,
            variante = findViewById<EditText>(R.id.etVariante).text.toString(),
            ceca = findViewById<EditText>(R.id.etCeca).text.toString(),
            tipo = findViewById<EditText>(R.id.etTipo).text.toString(),
            disenador = findViewById<EditText>(R.id.etDisenador).text.toString(),
            totalProducido = findViewById<EditText>(R.id.etTotalProducido).text.toString(),
            valorSinCircular = findViewById<EditText>(R.id.etValorSinCircular).text.toString(),
            valorComercial = findViewById<EditText>(R.id.etValorComercial).text.toString(),
            valorAdquirido = findViewById<EditText>(R.id.etValorAdquirido).text.toString(),
            estado = findViewById<EditText>(R.id.etEstado).text.toString(),
            observaciones = findViewById<EditText>(R.id.etObservaciones).text.toString(),
            orden = -1,
            acunada = findViewById<EditText>(R.id.etAcunada).text.toString()
        )

        // Primero actualizar la moneda
        NetworkUtils.actualizarMoneda(objeto.id.toLong(), monedaRequest) { success, error ->
            if (success) {
                // Luego subir las fotos nuevas
                subirFotos(progressDialog)
            } else {
                runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(this@EditCoinActivity, "Error al actualizar: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun subirFotos(progressDialog: ProgressDialog) {
        var fotosSubidas = 0
        val totalFotos = fotosSeleccionadas.count { it != null }

        if (totalFotos == 0) {
            runOnUiThread {
                progressDialog.dismiss()
                finalizarConExito()
            }
            return
        }

        fotosSeleccionadas.forEachIndexed { index, uri ->
            if (uri != null && uri.scheme != null && uri.scheme == "content") {
                // Es una foto nueva seleccionada
                NetworkUtils.uploadPhoto(objeto.id.toLong(), uri, this, index + 1) { success, error ->
                    fotosSubidas++
                    if (fotosSubidas >= totalFotos) {
                        runOnUiThread {
                            progressDialog.dismiss()
                            if (success) {
                                finalizarConExito()
                            } else {
                                Toast.makeText(this@EditCoinActivity, "Error al subir fotos: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                // Es una foto existente o null, contar como subida
                fotosSubidas++
                if (fotosSubidas >= totalFotos) {
                    runOnUiThread {
                        progressDialog.dismiss()
                        finalizarConExito()
                    }
                }
            }
        }
    }

    private fun finalizarConExito() {
        Toast.makeText(this, "Moneda actualizada exitosamente", Toast.LENGTH_SHORT).show()
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_MONEDA_ACTUALIZADA, true)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


}*/