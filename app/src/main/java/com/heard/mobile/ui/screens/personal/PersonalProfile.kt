package com.heard.mobile.ui.screens.personal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.personal.components.ProfileHeader
import com.heard.mobile.ui.screens.personal.components.CustomTabRow
import com.heard.mobile.ui.screens.personal.tabs.StatisticheTab
import com.heard.mobile.ui.screens.personal.tabs.DatiPersonaliTab
import com.heard.mobile.ui.screens.personal.tabs.UserData
import kotlinx.coroutines.tasks.await

@Composable
fun PersonalProfile(navController: NavController) {
    val tabItems = listOf("Statistiche", "Dati Personali")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Stato per i dati utente
    var userData by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Caricamento dati da Firestore
    LaunchedEffect(Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val document = firestore.collection("Utenti")
                .document("ID_UTENTE")  // Sostituisci con il corretto ID utente
                .get()
                .await()

            userData = UserData(
                nome = document.getString("Nome") ?: "",
                cognome = document.getString("Cognome") ?: "",
                badge = document.getString("Badge") ?: "",
                viaggi = document.getString("Viaggi") ?: "",
                km = document.getString("Km") ?: "",
                foto = document.getString("Foto") ?: "",
                tempoViaggio = document.getString("TempoViaggio") ?: "",
                mediaKmGiorno = document.getString("MediaKmGiorno") ?: ""
            )
        } catch (e: Exception) {
            errorMessage = "Errore nel caricamento dati: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { AppBar(navController, title = "Profilo Personale") },
        containerColor = MaterialTheme.colorScheme.background
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            // Header fisso
            ProfileHeader()

            // Tab fissa
            CustomTabRow(
                selectedTabIndex = selectedTabIndex,
                tabItems = tabItems,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contenuto della tab selezionata
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        when (selectedTabIndex) {
                            //0 -> AttivitaTab(navController = navController)
                            0 -> StatisticheTab()  // Passaggio dei dati caricati
                            1 -> DatiPersonaliTab()
                        }
                    }
                }
            }
        }
    }
}
