package com.example.proyecto_iot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {

    // 1. Obtenemos la instancia de Autenticación
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // 2. ¡NUEVO! Obtenemos la instancia de la Base de Datos
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 3. Estados para los TRES campos de texto
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("") // <-- Nuevo
        private set

    // 4. Estados para errores y carga
    var registerError by mutableStateOf<String?>(null)
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
    fun onConfirmPasswordChange(newConfirm: String) { // <-- Nuevo
        confirmPassword = newConfirm
    }

    // 6. ¡AQUÍ ESTÁ LA LÓGICA DEL REGISTRO!
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
        // (Podrías añadir más, como 'password.length < 6')

        // --- Petición a Firebase ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // ¡Éxito en Autenticación!
                    // Ahora guardamos el usuario en nuestra base de datos Firestore
                    saveUserToFirestore(onRegisterSuccess)
                } else {
                    // Error (ej. email ya existe, contraseña débil)
                    registerError = task.exception?.message ?: "Error desconocido"
                    isLoading = false
                }
            }
    }

    // 7. Función "ayudante" para guardar en Firestore
    private fun saveUserToFirestore(onRegisterSuccess: () -> Unit) {
        val userId = auth.currentUser!!.uid // Obtenemos el ID del usuario recién creado
        val userMap = mapOf(
            "email" to email
            // ... (aquí podríamos añadir 'nombre', 'apellido', etc. si los tuviéramos)
        )

        db.collection("users").document(userId) // Colección "users", documento con su ID
            .set(userMap) // Guardamos el mapa de datos
            .addOnSuccessListener {
                // ¡Éxito en Firestore!
                isLoading = false
                onRegisterSuccess() // Ahora sí, llamamos a la navegación
            }
            .addOnFailureListener { e ->
                // Error al guardar en Firestore
                registerError = "Error al guardar datos: ${e.message}"
                isLoading = false
            }
    }
}