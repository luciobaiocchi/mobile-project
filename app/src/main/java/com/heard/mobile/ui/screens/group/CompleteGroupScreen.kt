package com.heard.mobile.ui.screens.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.heard.mobile.ui.screens.group.components.UserGroupSection

@Composable
fun CompleteGroupScreen(
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onDismiss = { viewModel.dismissError() },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                uiState.userGroupPlaceHolder != null -> {
                    CompleteUserGroupContent(
                        groupPlaceHolder = uiState.userGroupPlaceHolder!!,
                        onLeaveGroup = { viewModel.leaveGroup() },
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
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
fun CompleteUserGroupContent(
    groupPlaceHolder: GroupPlaceHolder,
    onLeaveGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sezione principale del gruppo
        item {
            UserGroupSection(
                groupPlaceHolder = groupPlaceHolder,
                onLeaveGroup = onLeaveGroup
            )
        }

        // Statistiche del gruppo
        item {
            GroupStatsSection(groupPlaceHolder = groupPlaceHolder)
        }

        // Attività recenti
        item {
            GroupActivitiesSection(
                activities = mockGroupActivities
            )
        }

        // Sfide del gruppo
        item {
            GroupChallengesSection(
                challenges = mockGroupChallenges
            )
        }

        // Sezione per invitare nuovi membri (futura implementazione)
        item {
            InviteMembersSection()
        }
    }
}

@Composable
fun InviteMembersSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Invita Amici",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Invita i tuoi amici a unirsi al gruppo per condividere obiettivi e motivarvi a vicenda!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Implementa condivisione link */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Condividi Link")
                }

                Button(
                    onClick = { /* Implementa invito diretto */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Invita Contatti")
                }
            }
        }
    }
}

// Componente per mostrare le notifiche del gruppo
@Composable
fun GroupNotificationsSection(
    notifications: List<GroupNotification> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Notifiche Gruppo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (notifications.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Nessuna notifica",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            notifications.forEach { notification ->
                NotificationCard(notification = notification)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: GroupNotification,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (!notification.isRead) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}

// Data class per le notifiche
data class GroupNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean
)

// Mock data per le notifiche
val mockGroupNotifications = listOf(
    GroupNotification(
        id = "1",
        title = "Nuova sfida disponibile",
        message = "È stata creata una nuova sfida: 10.000 passi al giorno",
        timestamp = "2 ore fa",
        isRead = false
    ),
    GroupNotification(
        id = "2",
        title = "Nuovo membro",
        message = "Sara si è unita al gruppo!",
        timestamp = "1 giorno fa",
        isRead = true
    )
)

data class GroupPlaceHolder(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val maxMembers: Int,
    val leader: User,
    val members: List<User> = emptyList()
)

data class User(
    val id: String,
    val name: String,
    val avatar: Int? = null,
    val isLeader: Boolean = false
)



data class AvailableGroup(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val maxMembers: Int,
    val category: String
)