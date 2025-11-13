package com.example.proyecto_iot

// 1. Importa lo necesario
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel: ViewModel() {
    // 3. Obtenemos la instancia de Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 4. Creamos los "Estados" que la UI va a observar
    //    Esto es lo que el usuario escribe en los campos de texto
    var email by mutableStateOf("")
        private set // La UI puede leer, pero solo el ViewModel puede escribir

    var password by mutableStateOf("")
        private set

    // (Opcional pero recomendado) Estados para mostrar errores o un spinner
    var loginError by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set

    // 5. Funciones para que la UI actualice el estado
    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    // 6. ¡AQUÍ ESTÁ TU LÓGICA!
    //    La UI llamará a esta función cuando el usuario haga clic en "Iniciar Sesión"
    fun onLoginClick(onLoginSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            loginError = "Correo y contraseña no pueden estar vacíos"
            return
        }

        isLoading = true // Muestra el spinner
        loginError = null // Limpia errores antiguos

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false // Oculta el spinner
                if (task.isSuccessful) {
                    // ¡Éxito!
                    // Llamamos a la función "onLoginSuccess" que nos pasó la UI
                    // para que la UI sepa que debe navegar a la pantalla principal.
                    onLoginSuccess()
                } else {
                    // Error (ej. contraseña incorrecta)
                    loginError = task.exception?.message ?: "Error desconocido"
                }
            }
    }
}