package com.heard.mobile.ui.screens.personal.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.R
import com.heard.mobile.ui.screens.personal.tabs.UserData
import androidx.compose.runtime.setValue

@Composable
fun ProfileHeader() {
    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadUserDataFromFirestore(
            onSuccess = { data ->
                userData = data
                isLoading = false
            },
            onError = { error ->
                errorMessage = error
                isLoading = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage()
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(
                    text = errorMessage ?: "Errore sconosciuto",
                    color = MaterialTheme.colorScheme.error
                )
                else -> {
                    userData?.let { user ->
                        UserInfo(user)
                        Spacer(modifier = Modifier.height(16.dp))
                        QuickStatsRow(user)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfo(userData: UserData) {
    Text(
        text = "${userData.nome} ${userData.cognome}",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    Text(
        text = userData.badge.ifEmpty { "Nessun badge" },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
    )
}

@Composable
private fun QuickStatsRow(userData: UserData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickStatCard(userData.viaggi.ifEmpty { "0" }, "Attività")
        QuickStatCard(userData.km.ifEmpty { "0" }, "Km")
        QuickStatCard(userData.foto.ifEmpty { "0" }, "Foto")
    }
}


@Composable
private fun ProfileImage() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = CircleShape
            )
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.user),
            contentDescription = "User profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}


private fun loadUserDataFromFirestore(
    onSuccess: (UserData) -> Unit,
    onError: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid

    if (currentUser == null) {
        onError("Utente non autenticato")
        return
    }

    firestore.collection("Utenti")
        .document(currentUser)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                try {
                    val userData = UserData(
                        nome = document.getString("Nome") ?: "",
                        cognome = document.getString("Cognome") ?: "",
                        badge = document.getString("Badge") ?: "",
                        viaggi = document.getString("Viaggi") ?: "",
                        km = document.getString("Km") ?: "",
                        foto = document.getString("Foto") ?: ""
                    )
                    onSuccess(userData)
                } catch (e: Exception) {
                    onError("Errore nel parsing dei dati: ${e.message}")
                }
            } else {
                onError("Dati utente non trovati")
            }
        }
        .addOnFailureListener { exception ->
            onError("Errore nel caricamento: ${exception.message} ${currentUser}")
        }
}


@Composable
fun QuickStatCard(value: String, label: String) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}