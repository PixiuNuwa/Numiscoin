package cl.numiscoin2

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraWithOverlayActivity : AppCompatActivity() {
    private lateinit var camera: Camera
    private lateinit var surfaceView: SurfaceView
    private lateinit var captureButton: Button
    private var cameraPreview: CameraPreview? = null
    private var outputFileUri: String? = null
    private var circleCenterX: Float = 0f
    private var circleCenterY: Float = 0f
    private var circleRadius: Float = 0f

    companion object {
        const val EXTRA_OUTPUT_URI = "output_uri"
        const val REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Forzar orientación portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_camera_overlay)

        surfaceView = findViewById(R.id.surface_view)
        captureButton = findViewById(R.id.btn_capture)

        // Crear archivo temporal para la foto
        outputFileUri = createImageFile().absolutePath

        setupCamera()
        setupCaptureButton()
    }

    private fun setupCamera() {
        cameraPreview = CameraPreview(this, surfaceView.holder)
        val previewLayout = findViewById<FrameLayout>(R.id.camera_preview)

        val overlayView = CameraOverlayView(this)
        previewLayout.addView(overlayView)

        // Obtener las coordenadas del círculo después de que la vista se haya dibujado
        overlayView.viewTreeObserver.addOnGlobalLayoutListener {
            circleCenterX = overlayView.width / 2f
            circleCenterY = overlayView.height / 2f
            circleRadius = minOf(overlayView.width, overlayView.height) * 0.35f
        }

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    camera = Camera.open()

                    // Configurar autoenfoque CONTINUO automático
                    setupAutoFocus()

                    // Configurar orientación para portrait
                    setCameraDisplayOrientation()

                    camera.setPreviewDisplay(holder)
                    camera.startPreview()

                } catch (e: Exception) {
                    Log.e("Camera", "Error setting camera preview: ${e.message}")
                    Toast.makeText(this@CameraWithOverlayActivity, "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                if (holder.surface == null) return

                try {
                    camera.stopPreview()
                    setCameraDisplayOrientation()
                    camera.setPreviewDisplay(holder)
                    camera.startPreview()
                } catch (e: Exception) {
                    Log.e("Camera", "Error restarting camera preview: ${e.message}")
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                try {
                    camera.stopPreview()
                    camera.release()
                } catch (e: Exception) {
                    Log.e("Camera", "Error releasing camera: ${e.message}")
                }
            }
        })
    }

    private fun setupAutoFocus() {
        try {
            val parameters = camera.parameters

            // Configurar enfoque automático CONTINUO
            if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                Log.d("Camera", "Modo de enfoque: CONTINUOUS_PICTURE")
            } else if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                Log.d("Camera", "Modo de enfoque: CONTINUOUS_VIDEO")
            } else if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                Log.d("Camera", "Modo de enfoque: AUTO")
            } else {
                Log.d("Camera", "Modo de enfoque: No soportado, usando default")
            }

            camera.parameters = parameters
        } catch (e: Exception) {
            Log.e("Camera", "Error setting auto focus: ${e.message}")
        }
    }

    private fun setCameraDisplayOrientation() {
        try {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(0, info)

            val rotation = windowManager.defaultDisplay.rotation
            var degrees = when (rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }

            // ⚡ CORRECCIÓN CLAVE ⚡
            var result: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360  // Compensar espejo
            } else {
                // Para cámara trasera en portrait
                result = when (degrees) {
                    0 -> 90    // Portrait → 90°
                    90 -> 0    // Landscape → 0°
                    180 -> 270 // Portrait invertido → 270°
                    270 -> 180 // Landscape invertido → 180°
                    else -> 90
                }
            }

            Log.i("CAMARA", "Orientación aplicada: ${result}°")

            camera.setDisplayOrientation(result)

            val parameters = camera.parameters
            // ⚡ IMPORTANTE: Para la rotación de la foto guardada usamos degrees, no result
            val captureRotation = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                (info.orientation + degrees) % 360
            } else {
                (info.orientation - degrees + 360) % 360
            }

            parameters.setRotation(captureRotation)
            camera.parameters = parameters

        } catch (e: Exception) {
            Log.e("Camera", "Error setting camera orientation: ${e.message}")
        }
    }

    private fun setupCaptureButton() {
        captureButton.setOnClickListener {
            // Tomar foto directamente sin enfoque adicional (ya está en modo continuo)
            takePicture()
        }
    }

    private fun takePicture() {
        camera.takePicture(null, null) { data, camera ->
            try {
                // Procesar la imagen para recortar el área cuadrada grande
                val processedBitmap = processSquareCrop(data)

                // Guardar la imagen procesada
                val pictureFile = File(outputFileUri)
                FileOutputStream(pictureFile).use { fos ->
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                }

                processedBitmap.recycle() // Liberar memoria

                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_OUTPUT_URI, outputFileUri)
                setResult(RESULT_OK, resultIntent)
                finish()

            } catch (e: Exception) {
                Log.e("Camera", "Error processing photo: ${e.message}")
                Toast.makeText(this@CameraWithOverlayActivity, "Error al procesar la foto", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun processSquareCrop(data: ByteArray): Bitmap {
        // Decodificar la imagen original
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false

        val originalBitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Obtener dimensiones
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height

        // Calcular el tamaño del cuadrado (diámetro del círculo * 2 = lado del cuadrado)
        val squareSize = (circleRadius * 4).toInt()

        Log.d("CAMARA", "Square size: $squareSize, Circle radius: $circleRadius")

        // Calcular las coordenadas de recorte en la imagen original
        val scaleX = originalWidth.toFloat() / surfaceView.width
        val scaleY = originalHeight.toFloat() / surfaceView.height

        val centerXInOriginal = circleCenterX * scaleX
        val centerYInOriginal = circleCenterY * scaleY

        // Calcular coordenadas de recorte para que el círculo quede centrado
        var cropX = (centerXInOriginal - squareSize / 2).toInt()
        var cropY = (centerYInOriginal - squareSize / 2).toInt()

        // Ajustar para que no se salga de los bordes
        cropX = cropX.coerceIn(0, originalWidth - squareSize)
        cropY = cropY.coerceIn(0, originalHeight - squareSize)

        Log.d("Camera", "Crop coordinates: X=$cropX, Y=$cropY, Size=$squareSize")

        // Crear bitmap cuadrado
        val finalSquareSize = minOf(squareSize, originalWidth - cropX, originalHeight - cropY)

        val squareBitmap = Bitmap.createBitmap(
            originalBitmap,
            cropX,
            cropY,
            finalSquareSize,
            finalSquareSize
        )

        originalBitmap.recycle() // Liberar la imagen original

        return squareBitmap
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()
        try {
            camera.stopPreview()
            camera.release()
        } catch (e: Exception) {
            Log.e("Camera", "Error releasing camera: ${e.message}")
        }
    }
}

class CameraOverlayView(context: Context) : View(context) {
    private val circlePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) * 0.35f

        // Dibujar círculo verde
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Dibujar texto de guía
        canvas.drawText("Coloque la moneda dentro del círculo", centerX, centerY - radius - 50, textPaint)

        // Dibujar texto informativo
        textPaint.textSize = 24f
        canvas.drawText("Enfoque automático activado", centerX, centerY + radius + 50, textPaint)
    }
}

class CameraPreview(context: Context, private val holder: SurfaceHolder) : SurfaceView(context) {
    init {
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
}