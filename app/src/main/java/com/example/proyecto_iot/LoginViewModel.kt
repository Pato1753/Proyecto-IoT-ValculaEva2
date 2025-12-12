package com.example.proyecto_iot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel: ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set


    var loginError by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set


    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }


    fun onLoginClick(onLoginSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            loginError = "Correo y contraseña no pueden estar vacíos"
            return
        }

        isLoading = true
        loginError = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false // Oculta el spinner
                if (task.isSuccessful) {

                    onLoginSuccess()
                } else {
                    // Error contraseña incorrecta
                    loginError = task.exception?.message ?: "Error desconocido"
                }
            }
    }
}