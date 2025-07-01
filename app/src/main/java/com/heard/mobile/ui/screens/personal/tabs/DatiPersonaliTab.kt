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
import com.heard.mobile.ui.screens.personal.components.PersonalInfoItem
import com.heard.mobile.ui.screens.personal.components.PreferenceItem

import com.heard.mobile.ui.screens.personal.dialogs.EditProfileDialog

@Composable
fun DatiPersonaliTab() {
    var showEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PersonalInfoCard(onEditClick = { showEditDialog = true })
        }

        item {
            TravelPreferencesCard()
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            onDismiss = { showEditDialog = false },
            onSave = { showEditDialog = false }
        )
    }
}

@Composable
private fun PersonalInfoCard(onEditClick: () -> Unit) {
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
                value = "Luca Camillo"
            )

            PersonalInfoItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = "luca.camillo@email.com"
            )

            PersonalInfoItem(
                icon = Icons.Default.Phone,
                label = "Telefono",
                value = "+39 333 123 4567"
            )

            PersonalInfoItem(
                icon = Icons.Default.Cake,
                label = "Data di Nascita",
                value = "15 Maggio 1990"
            )

            PersonalInfoItem(
                icon = Icons.Default.LocationCity,
                label = "Città",
                value = "Milano, Italia"
            )
        }
    }
}

@Composable
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
}

