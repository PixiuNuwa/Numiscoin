package cl.numiscoin2

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object NetworkDataUtils {
    private val gson = Gson()

    fun getPaises(callback: (List<Pais>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/paises")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
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

    fun getDivisas(callback: (List<Divisa>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/divisas")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val divisas = gson.fromJson(response, Array<Divisa>::class.java).toList()
                    callback(divisas, null)
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

    fun getMetales(callback: (List<Metal>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/metales")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val metales = gson.fromJson(response, Array<Metal>::class.java).toList()
                    callback(metales, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader().use { it?.readText() }
                        ?: "Error sin mensaje"
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    fun getEventos(callback: (List<Evento>?, String?) -> Unit) {
        NetworkEventUtils.getEventos(callback)
    }

    fun getEventosFuturos(callback: (List<Evento>?, String?) -> Unit) {
        NetworkEventUtils.getEventosFuturos(callback)
    }

    fun getEventosPorMes(año: Int, mes: Int, callback: (List<Evento>?, String?) -> Unit) {
        NetworkEventUtils.getEventosPorMes(año, mes, callback)
    }
}