package cl.numiscoin2.network

import android.content.Context
import android.net.Uri
import android.util.Log
import cl.numiscoin2.MonedaRequest
import cl.numiscoin2.ObjetoColeccion
import cl.numiscoin2.TotalesUsuarioResponse
import com.google.gson.Gson
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object NetworkObjectUtils {
    private val gson = Gson()

    fun createMoneda(monedaRequest: MonedaRequest, callback: (Long?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/monedas")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val json = gson.toJson(monedaRequest)
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(json)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val responseObj = gson.fromJson(response, Map::class.java)
                    val success = responseObj["success"] as? Boolean
                    val idObjeto = (responseObj["idObjeto"] as? Number)?.toLong()

                    if (success == true && idObjeto != null) {
                        callback(idObjeto, null)
                    } else {
                        callback(null, responseObj["message"] as? String ?: "Error desconocido")
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun actualizarMoneda(idObjeto: Long, monedaRequest: MonedaRequest, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/monedas/$idObjeto")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                val json = gson.toJson(monedaRequest)
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(json)
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val responseObj = gson.fromJson(response, Map::class.java)
                    val success = responseObj["success"] as? Boolean ?: false

                    if (success) {
                        callback(true, null)
                    } else {
                        callback(false, responseObj["message"] as? String ?: "Error desconocido")
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(false, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                callback(false, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun obtenerMonedaPorId(idObjeto: Int, callback: (ObjetoColeccion?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/monedas/$idObjeto/detalle")
                Log.i("ObjectBackendUtils","obteniendo moneda desde ${url}")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Log.i("ObjectBackendUtils","llego objeto moneda")
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val moneda = gson.fromJson(response, ObjetoColeccion::class.java)
                    callback(moneda, null)
                } else {
                    Log.i("ObjectBackendUtils","NO llego objeto moneda")
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.i("ObjectBackendUtils","Error de conexión: ${e.message}")
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun deleteMoneda(monedaId: Int, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/monedas/$monedaId")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(false, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                callback(false, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun uploadPhoto(idObjeto: Long, fotoUri: Uri, context: Context, numeroFoto: Int, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/upload/images")
                val connection = url.openConnection() as HttpsURLConnection
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

                // Agregar parámetro numeroFoto
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"numeroFoto\"\r\n\r\n")
                outputStream.writeBytes("$numeroFoto\r\n")
                outputStream.flush()

                // Agregar archivo de imagen
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"images\"; filename=\"foto_${idObjeto}_$numeroFoto.jpg\"\r\n")
                outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                outputStream.flush()

                // Escribir los bytes de la imagen
                val inputStream = context.contentResolver.openInputStream(fotoUri)
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
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Sin detalles"
                    callback(false, "Error $responseCode: $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                callback(false, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    /**
     * Obtiene los totales de colección, gasto e items para un usuario
     */
    fun obtenerTotalesPorUsuario(idUsuario: Long, callback: (TotalesUsuarioResponse?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/objetos/totalesporusuario/$idUsuario")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val totales = gson.fromJson<TotalesUsuarioResponse>(response, TotalesUsuarioResponse::class.java)
                    callback(totales, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun obtenerUltimosObjetosPorUsuario(idUsuario: Long, limite: Int = 10, callback: (List<ObjetoColeccion>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/objetos/ultimas/moneda/usuario/$idUsuario?limite=$limite")
                Log.d("NetworkObjectUtils", "URL: $url")

                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                Log.d("NetworkObjectUtils", "Response Code: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("NetworkObjectUtils", "Response: $response")

                    val listType = object : com.google.gson.reflect.TypeToken<List<ObjetoColeccion>>() {}.type
                    val objetos = gson.fromJson<List<ObjetoColeccion>>(response, listType)

                    // Log para ver las fotos de cada objeto
                    objetos?.forEachIndexed { index, objeto ->
                        Log.d("NetworkObjectUtils", "Objeto $index: ${objeto.nombre}, Fotos: ${objeto.fotos?.size ?: 0}")
                        objeto.fotos?.forEachIndexed { fotoIndex, foto ->
                            Log.d("NetworkObjectUtils", "  Foto $fotoIndex: ${foto.url}")
                        }
                    }

                    callback(objetos, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    Log.e("NetworkObjectUtils", "Error: $responseCode - $errorResponse")
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("NetworkObjectUtils", "Error de conexión: ${e.message}")
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }
}