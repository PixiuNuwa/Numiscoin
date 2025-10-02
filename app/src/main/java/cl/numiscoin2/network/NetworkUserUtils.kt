package cl.numiscoin2.network

import android.content.Context
import android.net.Uri
import android.util.Log
import cl.numiscoin2.Usuario
import com.google.gson.Gson
import java.io.*
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

object NetworkUserUtils {
    private val gson = Gson()

    fun performLogin(username: String, password: String, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/login")
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

    fun performRegister(nombre: String, apellido: String, email: String, password: String, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/registro")
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

    fun actualizarUsuario(idUsuario: Long, nombre: String, apellido: String, email: String, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "PUT"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                Log.d("UpdateUserDebug", "Actualizando usuario: $idUsuario")

                val postData = "idUsuario=${URLEncoder.encode(idUsuario.toString(), "UTF-8")}&" +
                        "nombre=${URLEncoder.encode(nombre, "UTF-8")}&" +
                        "apellido=${URLEncoder.encode(apellido, "UTF-8")}&" +
                        "email=${URLEncoder.encode(email, "UTF-8")}"

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("UpdateUserDebug", "Código de respuesta: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("UpdateUserDebug", "Respuesta del servidor: $response")

                    try {
                        val usuario = gson.fromJson(response, Usuario::class.java)
                        callback(true, "Usuario actualizado exitosamente", usuario)
                    } catch (e: Exception) {
                        Log.e("UpdateUserError", "Error al parsear JSON", e)
                        callback(true, "Usuario actualizado pero error al parsear respuesta", null)
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    Log.d("UpdateUserDebug", "Error del servidor: $errorResponse")
                    callback(false, "Error al actualizar usuario: $errorResponse", null)
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("UpdateUserError", "Excepción al actualizar usuario", e)
                callback(false, "Error de conexión: ${e.message ?: "Desconocido"}", null)
            }
        }.start()
    }

    fun cambiarPassword(idUsuario: Long, nuevaPassword: String, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/password")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "PUT"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                Log.d("ChangePasswordDebug", "Cambiando password para usuario: $idUsuario")

                val postData = "idUsuario=${URLEncoder.encode(idUsuario.toString(), "UTF-8")}&" +
                        "password=${URLEncoder.encode(nuevaPassword, "UTF-8")}"

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("ChangePasswordDebug", "Código de respuesta: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    callback(true, null)
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader().use { it?.readText() }
                        ?: "Error sin mensaje"
                    Log.d("ChangePasswordDebug", "Error del servidor: $errorResponse")
                    callback(false, "Error del servidor: $responseCode - $errorResponse")
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("ChangePasswordError", "Excepción al cambiar password", e)
                callback(false, "Error de conexión: ${e.message ?: "Desconocido"}")
            }
        }.start()
    }

    fun recoverPassword(email: String, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/password/forgot?email=${URLEncoder.encode(email, "UTF-8")}")
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

    fun uploadProfilePhoto(idUsuario: Long, fotoUri: Uri, context: Context, callback: (Boolean, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/upload/photos")
                val connection = url.openConnection() as HttpsURLConnection
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

    fun completeRegistrationWithFreeMembership(idUsuario: Long, callback: (Boolean, String, Usuario?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/activar-gratuita")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                Log.d("FreeMembershipDebug", "Activando membresía gratuita para usuario: $idUsuario")

                val postData = "idUsuario=${URLEncoder.encode(idUsuario.toString(), "UTF-8")}"

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("FreeMembershipDebug", "Código de respuesta: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("FreeMembershipDebug", "Respuesta del servidor: $response")

                    try {
                        val usuario = gson.fromJson(response, Usuario::class.java)
                        callback(true, "Membresía gratuita activada exitosamente", usuario)
                    } catch (e: Exception) {
                        Log.e("FreeMembershipError", "Error al parsear JSON", e)
                        callback(false, "Error al procesar la respuesta del servidor", null)
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    Log.d("FreeMembershipDebug", "Error del servidor: $errorResponse")

                    // Intentar parsear el mensaje de error
                    val errorMessage = try {
                        val errorJson = gson.fromJson(errorResponse, Map::class.java)
                        errorJson["message"]?.toString() ?: errorResponse
                    } catch (e: Exception) {
                        errorResponse
                    }

                    callback(false, "Error al activar membresía gratuita: $errorMessage", null)
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("FreeMembershipError", "Excepción al activar membresía gratuita", e)
                callback(false, "Error de conexión: ${e.message ?: "Desconocido"}", null)
            }
        }.start()
    }

    fun adquirirMembresia(idUsuario: Long, idMembresia: Int, idDivisa: Int = 1, callback: (Boolean, String, String?) -> Unit) {
        Thread {
            try {
                val url = URL("${NetworkConfig.BASE_URL}/api/jdbc/usuarios/adquirir-membresia")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Accept", "application/json")

                Log.d("PaymentDebug", "Solicitando pago para usuario: $idUsuario, membresía: $idMembresia")

                val postData = "idUsuario=${URLEncoder.encode(idUsuario.toString(), "UTF-8")}&" +
                        "idMembresia=${URLEncoder.encode(idMembresia.toString(), "UTF-8")}&" +
                        "idDivisa=${URLEncoder.encode(idDivisa.toString(), "UTF-8")}"

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = connection.responseCode
                Log.d("PaymentDebug", "Código de respuesta: $responseCode")

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("PaymentDebug", "Respuesta del servidor: $response")

                    try {
                        val responseMap = gson.fromJson(response, Map::class.java)
                        val success = responseMap["success"] as? Boolean ?: false
                        val message = responseMap["message"] as? String ?: "Sin mensaje"
                        val paymentUrl = responseMap["paymentUrl"] as? String

                        if (success && !paymentUrl.isNullOrEmpty()) {
                            callback(true, message, paymentUrl)
                        } else {
                            callback(false, "Error: $message", null)
                        }
                    } catch (e: Exception) {
                        Log.e("PaymentError", "Error al parsear JSON", e)
                        callback(false, "Error al procesar la respuesta del servidor", null)
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        ?: "Error sin mensaje"
                    Log.d("PaymentDebug", "Error del servidor: $errorResponse")

                    // Intentar parsear el mensaje de error
                    val errorMessage = try {
                        val errorJson = gson.fromJson(errorResponse, Map::class.java)
                        errorJson["message"]?.toString() ?: errorResponse
                    } catch (e: Exception) {
                        errorResponse
                    }

                    callback(false, "Error al procesar pago: $errorMessage", null)
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("PaymentError", "Excepción al procesar pago", e)
                callback(false, "Error de conexión: ${e.message ?: "Desconocido"}", null)
            }
        }.start()
    }
}