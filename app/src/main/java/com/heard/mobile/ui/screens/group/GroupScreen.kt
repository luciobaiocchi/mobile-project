package com.heard.mobile.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import com.heard.mobile.ui.screens.group.components.AvailableGroupCard
import com.heard.mobile.ui.screens.group.components.UserGroupSection
import kotlinx.coroutines.tasks.await


@Composable
fun GroupScreen(
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(true) }
    var userGroup by remember { mutableStateOf<Group?>(null) }
    var availableGroups by remember { mutableStateOf<List<AvailableGroup>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var userPath: String? = null;
    LaunchedEffect(Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            userPath = "/Utenti/$uid"

            if (uid != null) {
                // Query: cerco nella collezione "Gruppi" dove "componenti" contiene l'uid, limito a 1
                val querySnapshot = firestore.collection("Gruppi")
                    .whereArrayContains("Componenti", userPath)
                    .limit(1)
                    .get()
                    .await()

                userGroup = if (!querySnapshot.isEmpty) {
                    // Prendo il primo gruppo
                    querySnapshot.documents[0].toObject(Group::class.java)
                } else {
                    null
                }
            }

            // Carica anche i gruppi disponibili come fai ora
            val groupsSnapshot = firestore.collection("availableGroups").get().await()
            availableGroups = groupsSnapshot.documents.mapNotNull { it.toObject(AvailableGroup::class.java) }

        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            AppBar(navController, title = "Gruppo")
        },
        bottomBar = {
            CustomBottomBar(navController, active = "Gruppo")
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    ErrorMessage(
                        message = error ?: "Errore sconosciuto",
                        onDismiss = { error = null },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                userGroup != null -> {
                    UserGroupContent(
                        group = userGroup!!,
                        onLeaveGroup = {
                            // Qui puoi gestire la rimozione del gruppo (Firestore ecc.)
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    AvailableGroupsContent(
                        groups = availableGroups,
                        onJoinGroup = { groupId ->
                            // Qui puoi gestire l'iscrizione al gruppo (Firestore ecc.)
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Errore",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Riprova",
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
fun UserGroupContent(
    group: Group,
    onLeaveGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UserGroupSection(
                group = group,
                onLeaveGroup = onLeaveGroup
            )
        }

        // Puoi aggiungere qui eventuali altri contenuti della sezione gruppo
    }
}
@Composable
fun AvailableGroupsContent(
    groups: List<AvailableGroup>,
    onJoinGroup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Gruppi Disponibili",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (groups.isEmpty()) {
            item {
                EmptyGroupsMessage()
            }
        } else {
            items(groups) { group ->
                AvailableGroupCard(
                    group = group,
                    onJoinGroup = { onJoinGroup(group.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyGroupsMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nessun gruppo disponibile",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Al momento non ci sono gruppi a cui puoi iscriverti. Torna più tardi per vedere nuovi gruppi!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}