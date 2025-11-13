// Reemplaza todo tu MainActivity.kt con ESTO:
package com.example.proyecto_iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                // ¡Aquí es donde "encendemos" todo el sistema de navegación!
                AppNavigation()
            }
        }
    }
}



@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // 1. Ruta "login" (Sin cambios)
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

        // 2. Ruta "register" (Sin cambios)
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

        // 3. Ruta "home" (¡MODIFICADA!)
        composable(route = "home") {
            HomeScreen(
                // Conexión al historial (ya estaba)
                onNavigateToHistorial = {
                    navController.navigate("historial")
                },
                // ¡NUEVA CONEXIÓN!
                onNavigateToNotificaciones = {
                    navController.navigate("notificaciones")
                }
            )
        }

        // 4. Ruta "historial" (Sin cambios)
        composable(route = "historial") {
            HistorialScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Ruta "notificaciones" (¡NUEVA!)
        composable(route = "notificaciones") {
            NotificacionesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

