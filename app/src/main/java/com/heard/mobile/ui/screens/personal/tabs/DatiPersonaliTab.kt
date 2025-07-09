package com.heard.mobile.ui.screens.personal.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.heard.mobile.ui.screens.personal.components.PersonalInfoItem
import com.heard.mobile.ui.screens.personal.components.PreferenceItem
import com.heard.mobile.ui.screens.personal.dialogs.EditProfileDialog
import java.text.SimpleDateFormat
import java.util.Locale

// Data class per i dati utente
data class UserData(
    val badge: String = "",
    val nome: String = "",
    val cognome: String = "",
    val telefono: String = "",
    val dataNascita: String = "",
    val cittaNascita: String = "",
    val viaggi: String = "",
    val km: String = "",
    val foto: String = "",
    val tempoViaggio: String = "",
    val mediaKmGiorno: String = ""
)

@Composable
fun DatiPersonaliTab() {
    var showEditDialog by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf(UserData()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Carica i dati da Firestore all'avvio
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (isLoading) {
                LoadingCard()
            } else if (errorMessage != null) {
                ErrorCard(errorMessage = errorMessage!!)
            } else {
                PersonalInfoCard(
                    userData = userData,
                    onEditClick = { showEditDialog = true }
                )
            }
        }

        item {
            //TravelPreferencesCard()
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentData = userData,
            onDismiss = { showEditDialog = false },
            onSave = { updatedData ->
                saveUserDataToFirestore(
                    updatedData,
                    onSuccess = {
                        userData = updatedData
                        showEditDialog = false
                    },
                    onError = {
                        showEditDialog = false
                    }
                )
            }
        )
    }
}

private fun saveUserDataToFirestore(
    data: UserData,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid

    if (currentUser == null) {
        onError("Utente non autenticato")
        return
    }

    val userMap = mapOf(
        "Nome" to data.nome,
        "Cognome" to data.cognome,
        "Telefono" to data.telefono,
        "LuogoNascita" to data.cittaNascita
    )

    firestore.collection("Utenti")
        .document(currentUser)
        .update(userMap)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e.message ?: "Errore sconosciuto") }
}


// Funzione per caricare i dati da Firestore
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
                        telefono = document.getString("Telefono") ?: "",
                        cittaNascita = document.getString("LuogoNascita") ?: "",
                        dataNascita = document.getTimestamp("Nascita")?.toDate()?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: ""
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
private fun PersonalInfoCard(
    userData: UserData,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Informazioni Personali",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PersonalInfoItem(
                icon = Icons.Default.Person,
                label = "Nome Completo",
                value = "${userData.nome} ${userData.cognome}".trim()
            )

            PersonalInfoItem(
                icon = Icons.Default.Phone,
                label = "Telefono",
                value = userData.telefono
            )

            PersonalInfoItem(
                icon = Icons.Default.Cake,
                label = "Data di Nascita",
                value = userData.dataNascita
            )

            PersonalInfoItem(
                icon = Icons.Default.LocationCity,
                label = "Città di Nascita",
                value = userData.cittaNascita
            )
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Caricamento dati...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Errore",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Errore nel caricamento",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/*@Composable
private fun TravelPreferencesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Preferenze di Viaggio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceItem(
                icon = Icons.Default.DirectionsCar,
                label = "Mezzo Preferito",
                value = "Auto"
            )

            PreferenceItem(
                icon = Icons.Default.Landscape,
                label = "Tipo di Destinazione",
                value = "Montagna, Mare"
            )

            PreferenceItem(
                icon = Icons.Default.Schedule,
                label = "Durata Media Viaggio",
                value = "2-4 giorni"
            )

            PreferenceItem(
                icon = Icons.Default.Group,
                label = "Compagnia Preferita",
                value = "Famiglia, Amici"
            )
        }
    }
}*/