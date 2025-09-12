package cl.numiscoin2

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

object NetworkUtils {
    private val gson = Gson()
    private const val BASE_URL = "https://dcf4be963faf.ngrok-free.app"
    public const val UPLOADS_BASE_URL = "https://numiscoin.store/uploads/"

    // Función existente
    fun performLogin(username: String, password: String, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/usuarios/login")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                Log.d("LoginDebug", "Intentando login para usuario: $username")

                val postData = "user=$username&pass=$password"
                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("LoginDebug", "Código de respuesta: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("LoginDebug", "Respuesta del servidor: $response")

                    try {
                        val usuario = gson.fromJson(response, Usuario::class.java)
                        callback(true, "Login exitoso", usuario)
                    } catch (e: Exception) {
                        Log.e("LoginError", "Error al parsear JSON", e)
                        callback(true, "Login exitoso pero error al parsear usuario: $response", null)
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    Log.d("LoginDebug", "Error del servidor: $errorResponse")
                    callback(false, "Error del servidor: $errorResponse", null)
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("LoginError", "Excepción al hacer login", e)
                callback(false, "Error de conexión: ${e.message ?: "Desconocido"}", null)
            }
        }.start()
    }

    // Nueva función para obtener colecciones de usuario
    fun getUserCollections(userId: Long, callback: (List<Coleccion>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/colecciones/usuario/$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val coleccionListType = object : TypeToken<List<Coleccion>>() {}.type
                    val colecciones = gson.fromJson<List<Coleccion>>(response, coleccionListType)
                    callback(colecciones, null)
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

    // Nueva función para obtener objetos de colección
    fun getCollectionObjects(collectionId: Int, callback: (List<ObjetoColeccion>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/colecciones/$collectionId/objetos")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("getCollectionObjects",response)
                    val objetoListType = object : TypeToken<List<ObjetoColeccion>>() {}.type
                    val objetos = gson.fromJson<List<ObjetoColeccion>>(response, objetoListType)
                    callback(objetos, null)
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

    // Nueva función para crear moneda/objeto
    fun createMoneda(monedaRequest: MonedaRequest, callback: (Long?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/monedas")
                val connection = url.openConnection() as HttpURLConnection
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
                if (responseCode == HttpURLConnection.HTTP_OK) {
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

    fun uploadPhoto(idObjeto: Long, fotoUri: Uri, context: Context, numeroFoto: Int, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/upload/images")
                val connection = url.openConnection() as HttpURLConnection
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
                if (responseCode == HttpURLConnection.HTTP_OK) {
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

    // Nueva función para eliminar moneda
    fun deleteMoneda(monedaId: Int, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/monedas/$monedaId")
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

    // Función auxiliar para construir URL completa de imágenes
    fun construirUrlCompleta(urlRelativa: String): String {
        return if (urlRelativa.startsWith("http")) {
            urlRelativa
        } else {
            UPLOADS_BASE_URL + urlRelativa
        }
    }

    // En NetworkUtils.kt, modificar la función performRegister
    fun performRegister(nombre: String, apellido: String, email: String, password: String, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/usuarios/registro")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                Log.d("RegisterDebug", "Intentando registrar usuario: $email")

                val postData = "nombre=${URLEncoder.encode(nombre, "UTF-8")}&" +
                        "apellido=${URLEncoder.encode(apellido, "UTF-8")}&" +
                        "email=${URLEncoder.encode(email, "UTF-8")}&" +
                        "password=${URLEncoder.encode(password, "UTF-8")}"

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("RegisterDebug", "Código de respuesta: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("RegisterDebug", "Respuesta del servidor: $response")

                    try {
                        val usuario = gson.fromJson(response, Usuario::class.java)
                        callback(true, "Cuenta creada exitosamente", usuario)
                    } catch (e: Exception) {
                        Log.e("RegisterError", "Error al parsear JSON", e)
                        callback(true, "Cuenta creada pero error al parsear respuesta", null)
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    Log.d("RegisterDebug", "Error del servidor: $errorResponse")
                    callback(false, "Error al crear cuenta: $errorResponse", null)
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("RegisterError", "Excepción al registrar usuario", e)
                callback(false, "Error de conexión: ${e.message ?: "Desconocido"}", null)
            }
        }.start()
    }

    fun uploadProfilePhoto(idUsuario: Long, fotoUri: Uri, context: Context, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/upload/photos")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.useCaches = false

                val boundary = "---------------------------${System.currentTimeMillis()}"
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                val outputStream = DataOutputStream(connection.outputStream)

                // Agregar parámetro idUsuario
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"idUsuario\"\r\n\r\n")
                outputStream.writeBytes("$idUsuario\r\n")
                outputStream.flush()

                // Agregar archivo de imagen
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"profile_$idUsuario.jpg\"\r\n")
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
                if (responseCode == HttpURLConnection.HTTP_OK) {
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

    fun recoverPassword(email: String, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/password/forgot?email=${URLEncoder.encode(email, "UTF-8")}")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    callback(true, response)
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

    fun getPaises(callback: (List<Pais>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/util/paises")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val paises = gson.fromJson(response, Array<Pais>::class.java).toList()
                    callback(paises, null)
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

    fun getCollectionObjectTypes(collectionId: Int, callback: (List<TipoObjeto>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/colecciones/$collectionId/tipos")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("getCollectionObjectTypes", response)
                    val tipoListType = object : TypeToken<List<TipoObjeto>>() {}.type
                    val tipos = gson.fromJson<List<TipoObjeto>>(response, tipoListType)
                    callback(tipos, null)
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

    // Agregar en NetworkUtils.kt
    fun getPaisesPorColeccionYTipo(idColeccion: Int, idTipoObjeto: Int, callback: (List<Pais>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("$BASE_URL/api/jdbc/colecciones/$idColeccion/tipos/$idTipoObjeto/paises")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("getPaisesPorColeccionYTipo", response)
                    val paisListType = object : TypeToken<List<Pais>>() {}.type
                    val paises = gson.fromJson<List<Pais>>(response, paisListType)
                    callback(paises, null)
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
}
