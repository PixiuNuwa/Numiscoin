package cl.numiscoin2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.exifinterface.media.ExifInterface as ExifCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object Utils {

    private const val MAX_IMAGE_SIZE_KB = 500 // Tamaño máximo de 500KB
    private const val MAX_DIMENSION = 1024 // Máxima dimensión de 1024px

    /**
     * Comprime una imagen desde una URI y devuelve la URI del archivo comprimido
     */
    fun comprimirImagen(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calcular sample size para reducir la imagen
            val sampleSize = calcularSampleSize(options.outWidth, options.outHeight)

            val options2 = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            val inputStream2 = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream2, null, options2)
            inputStream2?.close()

            // Corregir orientación si es necesario
            bitmap = corregirOrientacion(context, uri, bitmap)

            // Comprimir a JPEG con calidad ajustable
            val compressedBitmap = comprimirBitmap(bitmap)

            // Guardar la imagen comprimida en un archivo temporal
            guardarBitmapEnArchivo(context, compressedBitmap, "compressed_profile")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calcula el sample size adecuado para redimensionar la imagen
     */
    private fun calcularSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1

        if (height > MAX_DIMENSION || width > MAX_DIMENSION) {
            val heightRatio = Math.round(height.toFloat() / MAX_DIMENSION.toFloat())
            val widthRatio = Math.round(width.toFloat() / MAX_DIMENSION.toFloat())
            sampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }

        // Asegurar que sampleSize sea al menos 1
        return sampleSize.coerceAtLeast(1)
    }

    /**
     * Corrige la orientación de la imagen basándose en los metadatos EXIF
     */
    private fun corregirOrientacion(context: Context, uri: Uri, bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null

        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifCompat(inputStream!!)
            val orientation = exif.getAttributeInt(
                ExifCompat.TAG_ORIENTATION,
                ExifCompat.ORIENTATION_UNDEFINED
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifCompat.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifCompat.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifCompat.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifCompat.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifCompat.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap // No rotation needed
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap // Devolver bitmap original si hay error
        }
    }

    /**
     * Comprime un bitmap ajustando la calidad hasta alcanzar el tamaño máximo
     */
    private fun comprimirBitmap(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null

        return try {
            val outputStream = ByteArrayOutputStream()
            var quality = 85
            var sizeKB: Int

            do {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                sizeKB = outputStream.size() / 1024
                quality -= 5
            } while (sizeKB > MAX_IMAGE_SIZE_KB && quality > 50)

            // Decodificar el byte array comprimido de vuelta a Bitmap
            val compressedByteArray = outputStream.toByteArray()
            BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Guarda un bitmap en un archivo temporal y devuelve su URI
     */
    private fun guardarBitmapEnArchivo(context: Context, bitmap: Bitmap?, prefix: String): Uri? {
        if (bitmap == null) return null

        return try {
            val tempFile = File.createTempFile(prefix, ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene el tamaño de archivo en KB desde una URI
     */
    fun obtenerTamañoArchivoKB(context: Context, uri: Uri): Long {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val size = inputStream?.available() ?: 0
            inputStream?.close()
            size / 1024L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    /**
     * Verifica si una imagen necesita compresión basándose en su tamaño
     */
    fun necesitaCompresion(context: Context, uri: Uri): Boolean {
        val tamañoKB = obtenerTamañoArchivoKB(context, uri)
        return tamañoKB > MAX_IMAGE_SIZE_KB
    }

    /**
     * Método completo para procesar imagen (comprimir si es necesario)
     */
    fun procesarImagen(context: Context, uri: Uri): Uri {
        return if (necesitaCompresion(context, uri)) {
            comprimirImagen(context, uri) ?: uri
        } else {
            uri
        }
    }


}