package com.heard.mobile.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.heard.mobile.R
import com.heard.mobile.ui.HeardRoute
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow

data class ActivityItem(
    val title: String,
    val distance: String,
    val imageRes: Int
)

@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            AppBar(navController, title = "Home")
        },
        bottomBar = {
            CustomBottomBar(navController, active = "Home")
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item {
                Text(text = "Gruppi", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GroupCard(
                            title = "Visualizza il tuo gruppo attuale",
                            navController
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_right_alt_24),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionWithCard(
                    title = "Le mie attività",
                    imageResList = listOf(R.drawable.act_1, R.drawable.act_2, R.drawable.act_3),
                    navController
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SectionWithCard(
                    title = "Migliori percorsi",
                    imageResList = listOf(R.drawable.act_4, R.drawable.act_5, R.drawable.act_6),
                    navController
                )
            }
        }
    }
}

@Composable
fun GroupCard(title: String, navController: NavController) {
    Column (
        modifier = Modifier.clickable {
            navController.navigate(HeardRoute.Group)
        }

    ){
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SectionWithCard(title: String, imageResList: List<Int>, navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                navController.navigate(HeardRoute.Path)
            }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.arrow_right_alt_24),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageResList.size) { index ->
                    Image(
                        painter = painterResource(id = imageResList[index]),
                        contentDescription = null,
                        modifier = Modifier
                            .width(200.dp)
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


        }
    }
}
