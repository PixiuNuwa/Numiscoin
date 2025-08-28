package cl.numiscoin2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.numiscoin2.ui.theme.Numiscoin2Theme
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Numiscoin2Theme {
                AppNavigation()
            }
        }
    }
}

// Navegación entre pantallas
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { userName ->
                    navController.navigate("welcome/$userName")
                }
            )
        }
        composable("welcome/{userName}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            WelcomeScreen(userName = userName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "ENTRAR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // Tarjeta con campos de entrada
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                ) {
                    // Campo de usuario/email
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Email o Nombre de Usuario") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Enlace "Olvidaste tu contraseña?"
                    Text(
                        text = "¿Olvidaste tu contraseña? Haz click Aquí",
                        color = Color.Blue,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { /* TODO: Implementar recuperación de contraseña */ },
                        textAlign = TextAlign.End,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de continuar
                    Button(
                        onClick = {
                            isLoading = true
                            // Llamar al endpoint de login
                            performLogin(username, password) { success, message ->
                                // Usar CoroutineScope para volver al hilo principal
                                CoroutineScope(Dispatchers.Main).launch {
                                    isLoading = false
                                    if (success) {
                                        // Extraer el nombre del usuario del mensaje
                                        val userName = extractUserName(message)
                                        onLoginSuccess(userName)
                                    } else {
                                        errorMessage = message
                                        showErrorDialog = true
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4285F4)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("CONTINUAR", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Google
            Button(
                onClick = { /* TODO: Implementar login con Google */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_upload),
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp)
                )
                Text("  Continuar con Google", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enlace para crear cuenta
            TextButton(
                onClick = { /* TODO: Implementar navegación a creación de cuenta */ }
            ) {
                Text("¿No tienes una cuenta? CREAR CUENTA", color = Color.White)
            }
        }
    }

    // Diálogo de error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error de autenticación") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

// Función para extraer el nombre de usuario del mensaje de respuesta
fun extractUserName(message: String): String {
    return if (message.contains("Bienvenido")) {
        message.substringAfter("Bienvenido ").trim()
    } else {
        "Usuario"
    }
}

// Función para realizar el login (ejecutar en un hilo secundario)
fun performLogin(username: String, password: String, callback: (Boolean, String) -> Unit) {
    Thread {
        try {
            val url = URL("https://2f832ec5162a.ngrok-free.app/api/login?user=guido&pass=grl.1969")
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            Log.d("LoginDebug", "URL final: ${connection.url}")
            // Configurar cuerpo de la petición
            val postData = "user2=$username&pass2=$password"
            connection.outputStream.use { os ->
                os.write(postData.toByteArray())
                os.flush()
            }

            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage

            // 👉 Log código y mensaje de respuesta
            Log.d("LoginDebug", "Código respuesta: $responseCode, Mensaje: $responseMessage")
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Leer la respuesta del servidor
                Log.d("LoginDebug", "Credenciales correctas")
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("LoginDebug", "buffer leido")
                callback(true, response)
            } else {
                Log.d("LoginDebug", "Credenciales incorrectas")
                callback(false, "Credenciales incorrectas")
            }
        } catch (e: Exception) {
            Log.e("LoginError", "Excepción al hacer login", e)
            callback(false, "Error de conexión: ${e.message}")
        }
    }.start()
}

// Pantalla de bienvenida
@Composable
fun WelcomeScreen(userName: String) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Bienvenido!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Hola $userName, has iniciado sesión correctamente.",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { /* Podrías implementar cerrar sesión y volver al login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4)
                )
            ) {
                Text("Cerrar Sesión", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Numiscoin2Theme {
        LoginScreen(onLoginSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    Numiscoin2Theme {
        WelcomeScreen(userName = "Guido Rojas")
    }
}