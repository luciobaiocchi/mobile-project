package com.heard.mobile.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.personal.components.SettingItem

// PRIVACY SCREEN
@Composable
fun PrivacyScreen(navController: NavController) {
    var deleteAccountDialogOpen by remember { mutableStateOf(false) }
    var dataExportDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppBar(navController, title = "Privacy") }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PrivacyCard(
                    onDeleteAccount = { deleteAccountDialogOpen = true },
                    onExportData = { dataExportDialogOpen = true }
                )
            }
        }
    }

    // Dialog per eliminazione account
    if (deleteAccountDialogOpen) {
        AlertDialog(
            onDismissRequest = { deleteAccountDialogOpen = false },
            title = { Text("Elimina Account") },
            text = {
                Text("Sei sicuro di voler eliminare il tuo account? Questa azione non può essere annullata e tutti i tuoi dati verranno persi definitivamente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteAccountDialogOpen = false
                    }
                ) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteAccountDialogOpen = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per export dati
    if (dataExportDialogOpen) {
        AlertDialog(
            onDismissRequest = { dataExportDialogOpen = false },
            title = { Text("Esporta Dati") },
            text = {
                Text("I tuoi dati verranno preparati e inviati all'email associata al tuo account entro 24 ore.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dataExportDialogOpen = false
                        // Qui implementeresti la logica per esportare i dati
                        // dataExportViewModel.exportUserData()
                    }
                ) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dataExportDialogOpen = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun PrivacyCard(
    onDeleteAccount: () -> Unit,
    onExportData: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Gestione Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                icon = Icons.Default.Download,
                title = "Esporta i tuoi dati",
                subtitle = "Scarica una copia dei tuoi dati personali",
                onClick = onExportData
            )

            SettingItem(
                icon = Icons.Default.Visibility,
                title = "Dati raccolti",
                subtitle = "Vedi quali dati raccogliamo e come li utilizziamo",
                onClick = {
                    // Potresti aprire una WebView o un'altra schermata
                }
            )

            SettingItem(
                icon = Icons.Default.Block,
                title = "Gestione consensi",
                subtitle = "Modifica le tue preferenze sui cookie e tracciamento",
                onClick = {
                    // Gestione consensi GDPR
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sezione pericolosa
            /*Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Zona Pericolosa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Elimina account",
                        subtitle = "Elimina definitivamente il tuo account e tutti i dati",
                        onClick = onDeleteAccount,
                    )
                }
            }*/
        }
    }
}

// SUPPORT SCREEN
@Composable
fun SupportScreen(navController: NavController) {
    val context = LocalContext.current

    val faqItems = listOf(
        FAQItem("Come posso cambiare la mia password?", "Vai nelle impostazioni account e clicca su 'Cambia password'. Riceverai un'email con le istruzioni."),
        FAQItem("Come funziona la localizzazione?", "La localizzazione viene utilizzata per tracciare i tuoi viaggi e fornirti informazioni rilevanti basate sulla tua posizione."),
        FAQItem("Posso eliminare i miei dati di viaggio?", "Sì, puoi eliminare i singoli viaggi dalla sezione 'I miei viaggi' o esportare/eliminare tutti i dati dalla sezione Privacy."),
        FAQItem("Come disattivare le notifiche?", "Vai nelle Impostazioni e disattiva l'interruttore delle notifiche. Puoi anche gestire le notifiche dalle impostazioni del sistema."),
        FAQItem("L'app funziona offline?", "Alcune funzioni base funzionano offline, ma per la sincronizzazione e le funzioni social è necessaria una connessione internet.")
    )

    Scaffold(
        topBar = { AppBar(navController, title = "Aiuto e Supporto") }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SupportCard()
            }

            item {
                Text(
                    text = "Domande Frequenti",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(faqItems) { faq ->
                FAQCard(faq = faq)
            }
        }
    }
}

@Composable
private fun SupportCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Hai bisogno di aiuto?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                icon = Icons.Default.Email,
                title = "Contatta il supporto",
                subtitle = "Invia una email al nostro team di supporto",
                onClick = {
                    // Apri l'app email
                    // val intent = Intent(Intent.ACTION_SENDTO).apply {
                    //     data = Uri.parse("mailto:support@heard.com")
                    //     putExtra(Intent.EXTRA_SUBJECT, "Richiesta Supporto - Heard App")
                    // }
                    // context.startActivity(intent)
                }
            )

            SettingItem(
                icon = Icons.Default.Phone,
                title = "Supporto telefonico",
                subtitle = "Chiamaci al numero verde: 800-123-456",
                onClick = {
                    // Apri l'app telefono
                    // val intent = Intent(Intent.ACTION_DIAL).apply {
                    //     data = Uri.parse("tel:800123456")
                    // }
                    // context.startActivity(intent)
                }
            )

            SettingItem(
                icon = Icons.Default.Chat,
                title = "Chat dal vivo",
                subtitle = "Parla con un operatore in tempo reale",
                onClick = {
                    // Apri chat di supporto
                }
            )

            SettingItem(
                icon = Icons.Default.BugReport,
                title = "Segnala un bug",
                subtitle = "Aiutaci a migliorare l'app segnalando problemi",
                onClick = {
                    // Apri form per segnalazione bug
                }
            )
        }
    }
}

@Composable
private fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Chiudi" else "Espandi"
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

data class FAQItem(
    val question: String,
    val answer: String
)