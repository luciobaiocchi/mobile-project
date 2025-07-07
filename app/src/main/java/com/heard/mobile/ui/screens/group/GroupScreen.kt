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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import com.heard.mobile.ui.screens.group.components.AvailableGroupCard
import com.heard.mobile.ui.screens.group.components.UserGroupSection

@Composable
fun GroupScreen(
    navController: NavController,
    viewModel: GroupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                uiState.isLoading -> {
                    // Mostra indicatore di caricamento
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    // Mostra errore
                    ErrorMessage(
                        message = uiState.error!!,
                        onDismiss = { viewModel.dismissError() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.userGroup != null -> {
                    // Mostra il gruppo dell'utente
                    UserGroupContent(
                        group = uiState.userGroup!!,
                        onLeaveGroup = { viewModel.leaveGroup() },
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    // Mostra la lista dei gruppi disponibili
                    AvailableGroupsContent(
                        groups = uiState.availableGroups,
                        onJoinGroup = { groupId -> viewModel.joinGroup(groupId) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
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

        // Qui puoi aggiungere altri elementi come:
        // - Attività recenti del gruppo
        // - Sfide del gruppo
        // - Statistiche del gruppo
        // etc.
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