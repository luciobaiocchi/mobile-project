package com.heard.mobile.ui.screens.personal.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heard.mobile.ui.screens.personal.models.TravelItem

@Composable
fun TravelCard(
    travel: TravelItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TravelImage(travel)
            Spacer(modifier = Modifier.width(16.dp))
            TravelInfo(travel)
            NavigationIcon()
        }
    }
}

@Composable
private fun TravelImage(travel: TravelItem) {
    Image(
        painter = painterResource(id = travel.imageRes),
        contentDescription = travel.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
private fun RowScope.TravelInfo(travel: TravelItem) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = travel.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = travel.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = travel.distance,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NavigationIcon() {
    Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Vai ai dettagli",
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}