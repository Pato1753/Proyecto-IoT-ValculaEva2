package com.example.proyecto_iot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Crear el canal de notificaciones al iniciar la app
        crearCanalDeNotificaciones()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // 2. Iniciar la navegación
                AppNavigation()
            }
        }
    }

    // ESTA FUNCIÓN DEBE ESTAR DENTRO DE LA CLASE MainActivity
    private fun crearCanalDeNotificaciones() {
        // Solo es necesario en Android 8.0 (Oreo) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas IoT"
            val descriptionText = "Notificaciones de sensores del estanque"
            val importance = NotificationManager.IMPORTANCE_HIGH // ¡Alta importancia para que suene!

            val channel = NotificationChannel("canal_iot_alertas", name, importance).apply {
                description = descriptionText
            }

            // Ahora sí reconoce getSystemService porque estamos dentro de la clase
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

// La función de navegación puede estar fuera de la clase sin problemas
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // 1. Ruta Login
        composable(route = "login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 2. Ruta Registro
        composable(route = "register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Ruta Home (Panel de Control)
        composable(route = "home") {
            HomeScreen(
                onNavigateToHistorial = {
                    navController.navigate("historial")
                },
                onNavigateToNotificaciones = {
                    navController.navigate("notificaciones")
                },
                onNavigateToHorarios = {
                    navController.navigate("horarios")
                }
            )
        }

        // 4. Ruta Historial
        composable(route = "historial") {
            HistorialScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Ruta Notificaciones
        composable(route = "notificaciones") {
            NotificacionesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 6. Ruta Horarios
        composable(route = "horarios") {
            HorariosScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}