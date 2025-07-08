package com.heard.mobile.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.heard.mobile.ui.HeardRoute
import java.util.regex.Pattern

@Composable
fun RegisterScreen(
    navController: NavController,
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.background

    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        return emailPattern.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean = password.length >= 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registrati",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 caratteri)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Conferma Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        error = null
                        if (!isValidEmail(email.trim())) {
                            error = "Email non valida"
                            return@Button
                        }
                        if (!isValidPassword(password)) {
                            error = "La password deve essere almeno di 6 caratteri"
                            return@Button
                        }
                        if (password != confirmPassword) {
                            error = "Le password non coincidono"
                            return@Button
                        }
                        loading = true
                        auth.createUserWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                loading = false
                                if (task.isSuccessful) {
                                    // Registrazione ok, creiamo utente Firestore
                                    val user = auth.currentUser
                                    if (user != null) {
                                        val uid = user.uid
                                        val db = Firebase.firestore

                                        val userData = hashMapOf(
                                            "nome" to "",
                                            "cognome" to "",
                                            "email" to email.trim(),
                                            "Badge" to "Novizio",
                                        )


                                        val firestore = FirebaseFirestore.getInstance()

                                        user?.let {
                                            db.collection("Utenti").document(it.uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    onRegisterSuccess()
                                                }
                                                .addOnFailureListener { e ->
                                                    error = "Errore durante la creazione del profilo utente: ${e.localizedMessage}"
                                                }
                                        } ?: run {
                                            error = "Utente non trovato dopo la registrazione"
                                        }

                                    } else {
                                        error = "Errore: utente non trovato dopo la registrazione"
                                    }
                                } else {
                                    error = task.exception?.localizedMessage ?: "Errore durante la registrazione"
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    Text("Registrati")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        navController.navigate(HeardRoute.Login) {
                            popUpTo(HeardRoute.Register) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hai già un account? Accedi")
                }
            }
        }
    }
}
