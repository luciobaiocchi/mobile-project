package com.heard.mobile.ui.screens.personal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.personal.components.ProfileHeader
import com.heard.mobile.ui.screens.personal.components.CustomTabRow
import com.heard.mobile.ui.screens.personal.tabs.AttivitaTab
import com.heard.mobile.ui.screens.personal.tabs.StatisticheTab
import com.heard.mobile.ui.screens.personal.tabs.DatiPersonaliTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalProfile(navController: NavController) {
    val tabItems = listOf("Attività", "Statistiche", "Dati Personali")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { AppBar(navController, title = "Profilo Personale") },
        containerColor = MaterialTheme.colorScheme.background
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            ProfileHeader()

            CustomTabRow(
                selectedTabIndex = selectedTabIndex,
                tabItems = tabItems,
                onTabSelected = { selectedTabIndex = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> AttivitaTab(navController = navController)
                1 -> StatisticheTab()
                2 -> DatiPersonaliTab()
            }
        }
    }
}