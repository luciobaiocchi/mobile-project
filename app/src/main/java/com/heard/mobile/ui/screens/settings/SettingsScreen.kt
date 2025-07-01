package com.heard.mobile.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.ui.HeardRoute
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import com.heard.mobile.ui.screens.login.AuthViewModel
import com.heard.mobile.viewmodel.ThemeViewModel
import com.heard.mobile.ui.screens.personal.components.SettingItem

enum class ThemeOption(val label: String) {
    LIGHT("Chiaro"),
    DARK("Scuro"),
    SYSTEM("Sistema")
}

@Composable
fun SettingsScreen(navController: NavController , themeViewModel: ThemeViewModel, authViewModel: AuthViewModel) {
    // Stati per i dati personali
    var username by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }

    // Stato per il tema
    var expanded by remember { mutableStateOf(false) }
    var selectedTheme by rememberSaveable { mutableStateOf(ThemeOption.SYSTEM) }

    Scaffold(
        topBar = { AppBar(navController, title = "Impostazioni") },
        bottomBar = { CustomBottomBar(navController, active = "Impostazioni") }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AccountSettingsCard()
            }
            item {
                // Selettore tema
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedTheme.label,
                        onValueChange = {},
                        label = { Text("Tema") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Espandi tema")
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ThemeOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    expanded = false
                                    themeViewModel.setTheme(option)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        authViewModel.logout()
                        navController.navigate(HeardRoute.Login) {
                            popUpTo(HeardRoute.Home) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }

}

@Composable
private fun AccountSettingsCard() {
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
                text = "Impostazioni Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Notifiche",
                subtitle = "Ricevi aggiornamenti sui tuoi viaggi",
                hasSwitch = true,
                switchState = true,
                onSwitchChanged = {}
            )

            SettingItem(
                icon = Icons.Default.LocationOn,
                title = "Localizzazione",
                subtitle = "Permetti il tracciamento GPS",
                hasSwitch = true,
                switchState = true,
                onSwitchChanged = {}
            )

            SettingItem(
                icon = Icons.Default.Security,
                title = "Privacy",
                subtitle = "Gestisci le tue impostazioni privacy",
                onClick = {}
            )

            SettingItem(
                icon = Icons.Default.Help,
                title = "Aiuto e Supporto",
                subtitle = "FAQ e contatta il supporto",
                onClick = {}
            )
        }
    }
}
