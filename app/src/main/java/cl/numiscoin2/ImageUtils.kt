package cl.numiscoin2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    private const val TAG = "ImageUtils"
    private const val DEFAULT_MAX_SIZE_MB = 1.0
    private const val DEFAULT_MAX_DIMENSION = 2048
    private const val DEFAULT_MIN_QUALITY = 40
    private const val DEFAULT_MAX_SCALE = 16

    // Request codes estandarizados
    const val PICK_IMAGE_REQUEST_1 = 1
    const val PICK_IMAGE_REQUEST_2 = 2
    const val PICK_IMAGE_REQUEST_3 = 3
    const val PICK_IMAGE_REQUEST_4 = 4
    const val TAKE_PHOTO_REQUEST_1 = 101
    const val TAKE_PHOTO_REQUEST_2 = 102
    const val TAKE_PHOTO_REQUEST_3 = 103
    const val TAKE_PHOTO_REQUEST_4 = 104

    /**
     * Procesa una imagen desde Uri (galería) optimizándola
     */
    fun procesarFotoDesdeUri(context: Context, uri: Uri, maxSizeMB: Double = DEFAULT_MAX_SIZE_MB): Uri? {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            val maxSizeBytes = (maxSizeMB * 1024 * 1024).toLong()

            inputStream = context.contentResolver.openInputStream(uri)

            // Primera pasada: obtener dimensiones
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calcular escala inicial basada en dimensiones
            var scale = 1
            while (options.outWidth / scale > DEFAULT_MAX_DIMENSION || options.outHeight / scale > DEFAULT_MAX_DIMENSION) {
                scale *= 2
            }

            var quality = 90 // Calidad inicial
            var resultBitmap: Bitmap?
            var outputFile: File

            // Bucle para ajustar calidad hasta cumplir con el tamaño máximo
            do {
                inputStream = context.contentResolver.openInputStream(uri)
                options.inJustDecodeBounds = false
                options.inSampleSize = scale
                options.inPreferredConfig = Bitmap.Config.RGB_565

                resultBitmap = BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                if (resultBitmap == null) {
                    Log.e(TAG, "No se pudo decodificar el bitmap")
                    return null
                }

                // Crear recorte cuadrado (igual que la cámara)
                val size = minOf(resultBitmap.width, resultBitmap.height)
                val squareBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                val canvas = Canvas(squareBitmap)

                val left = (resultBitmap.width - size) / 2
                val top = (resultBitmap.height - size) / 2
                val srcRect = Rect(left, top, left + size, top + size)
                val dstRect = Rect(0, 0, size, size)

                canvas.drawBitmap(resultBitmap, srcRect, dstRect, null)
                resultBitmap.recycle()

                // Guardar temporalmente para medir tamaño
                outputFile = File.createTempFile(
                    "optimized_${System.currentTimeMillis()}",
                    ".jpg",
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )

                outputStream = FileOutputStream(outputFile)
                squareBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.close()
                squareBitmap.recycle()

                // Verificar tamaño del archivo
                val fileSizeBytes = outputFile.length()
                val fileSizeMB = fileSizeBytes / (1024.0 * 1024.0)

                Log.d(TAG, "Optimización - Calidad: $quality%, Escala: $scale, Tamaño: ${String.format("%.2f", fileSizeMB)} MB")

                if (fileSizeBytes > maxSizeBytes) {
                    // Reducir calidad para siguiente iteración
                    quality -= 15

                    // Si la calidad es muy baja, aumentar la escala
                    if (quality <= 50 && scale < DEFAULT_MAX_SCALE) {
                        scale *= 2
                        quality = 80
                        Log.d(TAG, "Aumentando escala a: $scale")
                    }

                    // Eliminar archivo temporal si no cumple
                    outputFile.delete()
                }

            } while (outputFile.exists() && outputFile.length() > maxSizeBytes && quality >= DEFAULT_MIN_QUALITY && scale <= DEFAULT_MAX_SCALE)

            if (outputFile.exists()) {
                val finalSizeMB = outputFile.length() / (1024.0 * 1024.0)
                if (finalSizeMB > maxSizeMB) {
                    Log.w(TAG, "No se pudo reducir a ${maxSizeMB}MB. Tamaño final: ${String.format("%.2f", finalSizeMB)} MB")
                } else {
                    Log.d(TAG, "✓ Imagen optimizada: ${String.format("%.2f", finalSizeMB)} MB")
                }
                return Uri.fromFile(outputFile)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error procesando foto desde galería: ${e.message}")
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error cerrando streams: ${e.message}")
            }
        }
        return null
    }

    /**
     * Procesa una imagen desde path (cámara) - igual que tu función original pero mejorada
     */
    fun procesarFotoDesdePath(context: Context, imagePath: String): Uri? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, options)

            var scale = 1
            while (options.outWidth / scale > DEFAULT_MAX_DIMENSION || options.outHeight / scale > DEFAULT_MAX_DIMENSION) {
                scale *= 2
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = scale
            options.inPreferredConfig = Bitmap.Config.RGB_565

            val originalBitmap = BitmapFactory.decodeFile(imagePath, options) ?: return null

            val size = minOf(originalBitmap.width, originalBitmap.height)
            val circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            val canvas = Canvas(circularBitmap)

            val left = (originalBitmap.width - size) / 2
            val top = (originalBitmap.height - size) / 2
            val srcRect = Rect(left, top, left + size, top + size)
            val dstRect = Rect(0, 0, size, size)

            canvas.drawBitmap(originalBitmap, srcRect, dstRect, null)

            val file = File.createTempFile(
                "circular_${System.currentTimeMillis()}",
                ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            FileOutputStream(file).use { out ->
                circularBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }

            originalBitmap.recycle()
            circularBitmap.recycle()

            return Uri.fromFile(file)

        } catch (e: Exception) {
            Log.e(TAG, "Error procesando foto desde path: ${e.message}")
            return null
        }
    }

    /**
     * Maneja el resultado de la actividad de forma estandarizada
     */
    fun manejarResultadoFoto(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onFotoProcesada: (Uri?, Int) -> Unit
    ) {
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            PICK_IMAGE_REQUEST_1, PICK_IMAGE_REQUEST_2,
            PICK_IMAGE_REQUEST_3, PICK_IMAGE_REQUEST_4 -> {

                data?.data?.let { uri ->
                    // Procesar foto de galería
                    val processedUri = procesarFotoDesdeUri(context, uri)
                    val fotoIndex = when (requestCode) {
                        PICK_IMAGE_REQUEST_1 -> 1
                        PICK_IMAGE_REQUEST_2 -> 2
                        PICK_IMAGE_REQUEST_3 -> 3
                        PICK_IMAGE_REQUEST_4 -> 4
                        else -> 1
                    }
                    onFotoProcesada(processedUri ?: uri, fotoIndex)
                }
            }
            TAKE_PHOTO_REQUEST_1, TAKE_PHOTO_REQUEST_2,
            TAKE_PHOTO_REQUEST_3, TAKE_PHOTO_REQUEST_4 -> {

                val photoPath = data?.getStringExtra(CameraWithOverlayActivity.EXTRA_OUTPUT_URI)
                photoPath?.let { path ->
                    val processedUri = procesarFotoDesdePath(context, path)
                    val fotoIndex = when (requestCode) {
                        TAKE_PHOTO_REQUEST_1 -> 1
                        TAKE_PHOTO_REQUEST_2 -> 2
                        TAKE_PHOTO_REQUEST_3 -> 3
                        TAKE_PHOTO_REQUEST_4 -> 4
                        else -> 1
                    }
                    onFotoProcesada(processedUri, fotoIndex)
                }
            }
        }
    }

    /**
     * Crea un Intent para seleccionar foto de galería
     */
    fun crearIntentGaleria(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    /**
     * Crea un Intent para tomar foto con cámara
     */
    fun crearIntentCamara(context: Context): Intent {
        return Intent(context, CameraWithOverlayActivity::class.java)
    }

    /**
     * Obtiene el request code para una posición específica de foto
     */
    fun getGaleriaRequestCode(posicion: Int): Int {
        return when (posicion) {
            1 -> PICK_IMAGE_REQUEST_1
            2 -> PICK_IMAGE_REQUEST_2
            3 -> PICK_IMAGE_REQUEST_3
            4 -> PICK_IMAGE_REQUEST_4
            else -> PICK_IMAGE_REQUEST_1
        }
    }

    fun getCamaraRequestCode(posicion: Int): Int {
        return when (posicion) {
            1 -> TAKE_PHOTO_REQUEST_1
            2 -> TAKE_PHOTO_REQUEST_2
            3 -> TAKE_PHOTO_REQUEST_3
            4 -> TAKE_PHOTO_REQUEST_4
            else -> TAKE_PHOTO_REQUEST_1
        }
    }
}