package cl.numiscoin2

import android.util.Log
import com.google.gson.Gson
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object NetworkUtils {

    fun performLogin(username: String, password: String, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("https://a05d441d8a25.ngrok-free.app/api/login")
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
                        val gson = Gson()
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
}
