package com.heard.mobile.ui.composables

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import com.heard.mobile.ui.HeardRoute
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CustomBottomBar(navController: NavController, active: String) {
    NavigationBar(
        modifier = Modifier.wrapContentHeight(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = NavigationBarDefaults.Elevation,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        NavigationBarItem(
            selected = active == "Home",
            onClick = { navController.navigate(HeardRoute.Home) },
            icon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home",
                )
            },
            label = {
                Text(
                    text = "Home",
                )
            },
            colors = iconTheme()
        )

        NavigationBarItem(
            selected = active == "Percorsi",
            onClick = { navController.navigate(HeardRoute.Path) },
            icon = {
                Icon(
                    Icons.Rounded.DirectionsWalk,
                    contentDescription = "Percorsi",
                )
            },
            label = {
                Text(
                    text = "Percorsi",
                )
            },
            colors = iconTheme()
        )

        NavigationBarItem(
            selected = active == "Impostazioni",
            onClick = { navController.navigate(HeardRoute.Settings) },
            icon = {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Impostazioni",
                )
            },
            label = {
                Text(
                    text = "Impostazioni",
                )
            },
            colors = iconTheme()
        )
    }
}

@Composable
fun iconTheme() : NavigationBarItemColors{
    return NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.surface, // <--- colore icona selezionata
        selectedTextColor = MaterialTheme.colorScheme.secondary, // <--- colore testo selezionato
        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
        indicatorColor = MaterialTheme.colorScheme.secondary // <--- colore dello sfondo selezionato
    )
}