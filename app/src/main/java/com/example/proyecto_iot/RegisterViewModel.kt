package com.example.proyecto_iot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("") // <-- Nuevo
        private set


    var registerError by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set


    fun onEmailChange(newEmail: String) {
        email = newEmail
    }
    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }
    fun onConfirmPasswordChange(newConfirm: String) {
        confirmPassword = newConfirm
    }


    fun onRegisterClick(onRegisterSuccess: () -> Unit) {
        isLoading = true
        registerError = null

        // --- Validación Primero ---
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            registerError = "Todos los campos son obligatorios"
            isLoading = false
            return
        }
        if (password != confirmPassword) {
            registerError = "Las contraseñas no coinciden"
            isLoading = false
            return
        }


        // --- Petición a Firebase ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ¡Éxito en Autenticación!

                    saveUserToFirestore(onRegisterSuccess)
                } else {
                    // Error (ej. email ya existe, contraseña débil)
                    registerError = task.exception?.message ?: "Error desconocido"
                    isLoading = false
                }
            }
    }

    private fun saveUserToFirestore(onRegisterSuccess: () -> Unit) {
        val userId = auth.currentUser!!.uid
        val userMap = mapOf(
            "email" to email

        )

        db.collection("users").document(userId) // Colección "users", documento con su ID
            .set(userMap) // Guardamos el mapa de datos
            .addOnSuccessListener {
                // ¡Éxito en Firestore!
                isLoading = false
                onRegisterSuccess()
            }
            .addOnFailureListener { e ->
                // Error al guardar en Firestore
                registerError = "Error al guardar datos: ${e.message}"
                isLoading = false
            }
    }
}