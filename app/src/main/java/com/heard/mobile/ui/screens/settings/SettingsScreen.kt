package com.heard.mobile.ui.screens.settings

import android.Manifest
import android.os.Build
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.ui.HeardRoute
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import com.heard.mobile.ui.screens.login.AuthViewModel
import com.heard.mobile.viewmodel.ThemeViewModel
import com.heard.mobile.ui.screens.personal.components.SettingItem
import com.heard.mobile.datastore.ThemeOption
import com.heard.mobile.utils.rememberMultiplePermissions
import com.heard.mobile.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val theme by themeViewModel.theme.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val locationEnabled by settingsViewModel.locationEnabled.collectAsState()

    val notificationPermissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val notificationPermissionHandler = rememberMultiplePermissions(
        permissions = notificationPermissions,
        onResult = { results ->
            val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                results[Manifest.permission.POST_NOTIFICATIONS]?.isGranted == true
            } else {
                true
            }

            if (hasNotificationPermission) {
                settingsViewModel.setNotificationsEnabled(true)
            }
        }
    )

    val locationPermissionHandler = rememberMultiplePermissions(
        permissions = locationPermissions,
        onResult = { results ->
            val hasLocationPermission = results[Manifest.permission.ACCESS_FINE_LOCATION]?.isGranted == true ||
                    results[Manifest.permission.ACCESS_COARSE_LOCATION]?.isGranted == true

            if (hasLocationPermission) {
                settingsViewModel.setLocationEnabled(true)
            }
        }
    )

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
                AccountSettingsCard(
                    themeViewModel = themeViewModel,
                    settingsViewModel = settingsViewModel,
                    currentTheme = theme,
                    notificationsEnabled = notificationsEnabled,
                    locationEnabled = locationEnabled,
                    notificationPermissionHandler = notificationPermissionHandler,
                    locationPermissionHandler = locationPermissionHandler,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            item {

            }
        }
    }
}

@Composable
private fun AccountSettingsCard(
    themeViewModel: ThemeViewModel,
    settingsViewModel: SettingsViewModel,
    currentTheme: ThemeOption,
    notificationsEnabled: Boolean,
    locationEnabled: Boolean,
    notificationPermissionHandler: com.heard.mobile.utils.MultiplePermissionHandler,
    locationPermissionHandler: com.heard.mobile.utils.MultiplePermissionHandler,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                switchState = notificationsEnabled,
                onSwitchChanged = { enabled ->
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notificationPermission = notificationPermissionHandler.statuses[Manifest.permission.POST_NOTIFICATIONS]
                            if (notificationPermission?.isGranted != true) {
                                notificationPermissionHandler.launchPermissionRequest()
                            } else {
                                settingsViewModel.setNotificationsEnabled(true)
                            }
                        } else {
                            settingsViewModel.setNotificationsEnabled(true)
                        }
                    } else {
                        settingsViewModel.setNotificationsEnabled(false)
                    }
                }
            )

            SettingItem(
                icon = Icons.Default.LocationOn,
                title = "Localizzazione",
                subtitle = "Permetti il tracciamento GPS",
                hasSwitch = true,
                switchState = locationEnabled,
                onSwitchChanged = { enabled ->
                    if (enabled) {
                        val hasLocationPermission = locationPermissionHandler.statuses[Manifest.permission.ACCESS_FINE_LOCATION]?.isGranted == true ||
                                locationPermissionHandler.statuses[Manifest.permission.ACCESS_COARSE_LOCATION]?.isGranted == true

                        if (!hasLocationPermission) {
                            locationPermissionHandler.launchPermissionRequest()
                        } else {
                            settingsViewModel.setLocationEnabled(true)
                        }
                    } else {
                        settingsViewModel.setLocationEnabled(false)
                    }
                }
            )

            SettingItem(
                icon = Icons.Default.Security,
                title = "Privacy",
                subtitle = "Gestisci le tue impostazioni privacy",
                onClick = {
                    navController.navigate(HeardRoute.Privacy)
                }
            )

            SettingItem(
                icon = Icons.Default.Help,
                title = "Aiuto e Supporto",
                subtitle = "FAQ e contatta il supporto",
                onClick = {
                    navController.navigate(HeardRoute.Support)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentTheme.label,
                    onValueChange = { _: String -> },
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
            Spacer(modifier = Modifier.height(16.dp))
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