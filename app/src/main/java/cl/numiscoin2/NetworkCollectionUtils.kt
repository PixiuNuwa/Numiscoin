package cl.numiscoin2

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object NetworkCollectionUtils {
    private val gson = Gson()

    fun getUserCollections(userId: Long, callback: (List<Coleccion>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/colecciones/usuario/$userId")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
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

    fun getCollectionObjects(collectionId: Int, callback: (List<ObjetoColeccion>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/colecciones/$collectionId/objetos")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
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

    fun getCollectionObjectTypes(collectionId: Int, callback: (List<TipoObjeto>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/colecciones/$collectionId/tipos")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
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

    fun getPaisesPorColeccionYTipo(idColeccion: Int, idTipoObjeto: Int, callback: (List<Pais>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/colecciones/$idColeccion/tipos/$idTipoObjeto/paises")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
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

    fun createCollection(usuarioId: Long, nombre: String, descripcion: String, callback: (Coleccion?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/colecciones")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true

                // Crear JSON con los datos de la colección
                val jsonInputString = """
                {
                    "nombre": "$nombre",
                    "descripcion": "$descripcion",
                    "id_usuario": $usuarioId
                }
            """.trimIndent()

                connection.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_CREATED) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val coleccion = gson.fromJson<Coleccion>(response, Coleccion::class.java)
                    callback(coleccion, null)
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