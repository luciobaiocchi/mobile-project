package com.heard.mobile.ui.screens.personal.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.screens.personal.components.StatCard
import com.heard.mobile.ui.screens.personal.components.MyLineChart
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment


@Composable
fun StatisticheTab() {
    var userData by remember { mutableStateOf(UserData()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Carica i dati da Firestore all'avvio
    LaunchedEffect(Unit) {
        loadUserStatsFromFirestore(
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
            ChartCard()
        }

        item {
            when {
                isLoading -> {
                    LoadingCard()
                }
                errorMessage != null -> {
                    ErrorCard(errorMessage = errorMessage!!)
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Attività Totali",
                            value = userData.viaggi.ifEmpty { "0" },
                            icon = Icons.Default.LocationOn,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Km Totali",
                            value = userData.km.ifEmpty { "0" },
                            icon = Icons.Default.Route,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Tempo Attività",
                            value = userData.tempoViaggio.ifEmpty { "0h" },
                            icon = Icons.Default.Schedule,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Media km/giorno",
                            value = userData.mediaKmGiorno.ifEmpty { "0" },
                            icon = Icons.Default.TrendingUp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}


private fun loadUserStatsFromFirestore(
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
                        viaggi = document.getString("Viaggi") ?: "",
                        km = document.getString("Km") ?: "",
                        tempoViaggio = document.getString("TempoViaggio") ?: "",
                        mediaKmGiorno = document.getString("MediaKmGiorno") ?: ""
                    )
                    onSuccess(userData)
                } catch (e: Exception) {
                    onError("Errore nel parsing dei dati: ${e.message}")
                }
            } else {
                onError("Dati statistici non trovati")
            }
        }
        .addOnFailureListener { exception ->
            onError("Errore nel caricamento: ${exception.message}")
        }
}


@Composable
private fun ChartCard() {
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
                text = "Distanza percorsa negli ultimi giorni",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            MyLineChart(modifier = Modifier.fillMaxWidth().height(200.dp))
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