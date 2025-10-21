//<<NetworkEventUtils
package cl.numiscoin2.network

import android.util.Log
import cl.numiscoin2.Evento
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

object NetworkEventUtils {
    private val gson: Gson = createGsonWithDateFix()

    private fun createGsonWithDateFix(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .create()
    }

    /**
     * Custom deserializer para fechas que evita el cambio de zona horaria
     */
    private class DateDeserializer : JsonDeserializer<Date> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date {
            return try {
                val dateString = json?.asString
                if (dateString.isNullOrEmpty()) {
                    Date()
                } else {
                    // Parsear la fecha manteniendo la hora exacta sin ajustes de zona horaria
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                    format.timeZone = TimeZone.getTimeZone("UTC") // Importante: usar UTC
                    format.parse(dateString) ?: Date()
                }
            } catch (e: Exception) {
                Log.e("DateDeserializer", "Error parsing date: ${json?.asString}", e)
                Date()
            }
        }
    }

    /**
     * Obtiene todos los eventos activos
     */
    fun getEventos(callback: (List<Evento>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/eventos")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("NetworkEventUtils", "JSON response: $response")

                    val listType = object : TypeToken<List<Evento>>() {}.type
                    val eventos = gson.fromJson<List<Evento>>(response, listType)

                    // Log para debug de fechas
                    eventos?.forEach { evento ->
                        Log.d("NetworkEventUtils", "Evento: ${evento.nombreEvento}")
                        Log.d("NetworkEventUtils", "  fechaInicio raw: ${evento.fechaInicio}")
                        Log.d("NetworkEventUtils", "  fechaFin raw: ${evento.fechaFin}")
                    }

                    callback(eventos, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("NetworkEventUtils", "Error getting eventos", e)
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    /**
     * Obtiene eventos futuros (próximo año)
     */
    fun getEventosFuturos(callback: (List<Evento>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/eventos/futuros")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("NetworkEventUtils", "Eventos futuros JSON: $response")

                    val listType = object : TypeToken<List<Evento>>() {}.type
                    val eventos = gson.fromJson<List<Evento>>(response, listType)

                    // Debug de fechas
                    eventos?.forEach { evento ->
                        Log.d("NetworkEventUtils", "Evento Futuro: ${evento.nombreEvento}")
                        Log.d("NetworkEventUtils", "  Inicio: ${evento.fechaInicio} -> ${formatDateForDisplay(evento.fechaInicio)}")
                        Log.d("NetworkEventUtils", "  Fin: ${evento.fechaFin} -> ${formatDateForDisplay(evento.fechaFin)}")
                    }

                    callback(eventos, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    callback(null, "Error del servidor: $responseCode - $errorResponse")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("NetworkEventUtils", "Error getting eventos futuros", e)
                callback(null, "Error de conexión: ${e.message}")
            }
        }.start()
    }

    /**
     * Función auxiliar para formatear fechas en debug
     */
    private fun formatDateForDisplay(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z", Locale.getDefault())
        return format.format(date)
    }

    // ... (mantener el resto de las funciones igual)
    /**
     * Obtiene eventos por mes y año específicos
     */
    fun getEventosPorMes(anio: Int, mes: Int, callback: (List<Evento>?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/eventos/mes?anio=$anio&mes=$mes")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val listType = object : TypeToken<List<Evento>>() {}.type
                    val eventos = gson.fromJson<List<Evento>>(response, listType)
                    callback(eventos, null)
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

    fun getEventoPorId(idEvento: Int, callback: (Evento?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/util/eventos/$idEvento")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val evento = gson.fromJson<Evento>(response, Evento::class.java)
                    callback(evento, null)
                } else if (responseCode == HttpsURLConnection.HTTP_NOT_FOUND) {
                    callback(null, "Evento no encontrado")
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
//>>NetworkEventUtils