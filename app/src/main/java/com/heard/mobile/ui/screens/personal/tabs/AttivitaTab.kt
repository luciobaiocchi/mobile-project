package com.heard.mobile.ui.screens.personal.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.R
import com.heard.mobile.ui.HeardRoute
import com.heard.mobile.ui.screens.personal.components.TravelCard
import com.heard.mobile.ui.screens.personal.models.TravelItem

@Composable
fun AttivitaTab(navController: NavController) {
    val travels = remember {
        listOf(
            TravelItem("Viaggio a Roma", "15 Marzo 2024", "450 km", R.drawable.user),
            TravelItem("Weekend in Toscana", "22 Marzo 2024", "320 km", R.drawable.user),
            TravelItem("Gita al Mare", "28 Marzo 2024", "180 km", R.drawable.user),
            TravelItem("Montagna", "5 Aprile 2024", "280 km", R.drawable.user),
            TravelItem("City Break Milano", "12 Aprile 2024", "520 km", R.drawable.user)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(travels.size) { index ->
            TravelCard(
                travel = travels[index],
                onClick = {
                    navController.navigate(HeardRoute.TravelDetails(travels[index].title))
                }
            )
        }
    }
}