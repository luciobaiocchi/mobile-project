package com.heard.mobile.ui.screens.path

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Menu

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

import com.heard.mobile.ui.HeardRoute
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.composables.CustomBottomBar
import kotlinx.coroutines.tasks.await

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PathScreen(navController: NavController) {
    var paths by remember { mutableStateOf<List<Triple<String, String, String?>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        paths = getAllPaths(db)
        isLoading = false
    }

    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { menuExpanded = true }
                ) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Apri menu")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Aggiungi percorso") },
                        onClick = {
                            menuExpanded = false
                            navController.navigate(HeardRoute.AddTravel)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Filtra preferiti") },
                        onClick = {
                            menuExpanded = false
                            isLoading = true
                            coroutineScope.launch {
                                paths = getFavoritesPaths(db)
                                isLoading = false
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tutti i percorsi") },
                        onClick = {
                            menuExpanded = false
                            isLoading = true
                            coroutineScope.launch {
                                paths = getAllPaths(db)
                                isLoading = false
                            }
                        }
                    )
                }
            }
        },
        topBar = { AppBar(navController, title = "Percorsi") },
        bottomBar = { CustomBottomBar(navController, active = "Percorsi") }
    ) { contentPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.size(8.dp))
                Text("Caricamento in corso...")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 80.dp),
                modifier = Modifier.padding(contentPadding)
            ) {
                items(paths) { path ->
                    PathItem(
                        item = path.second,
                        fileName = path.third,
                        onClick = { navController.navigate(HeardRoute.TravelDetails(path.first)) }
                    )
                }
            }
        }
    }
}


suspend fun getAllPaths(db: FirebaseFirestore): List<Triple<String, String, String?>> {
    val paths = db.collection("Percorsi").get().await()
    return paths.documents.mapNotNull { doc ->
        val name = doc.getString("Nome")
        val file = doc.getString("file")
        val id = doc.id
        if (name != null) Triple(id, name, file) else null
    }
}

suspend fun getFavoritesPaths(
    db: FirebaseFirestore,
): List<Triple<String, String, String?>> {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
    val userDoc = db.collection("Utenti").document(userId).get().await()
    val favoriteRefs = userDoc["Preferiti"] as? List<DocumentReference> ?: return emptyList()

    val favoritePaths = mutableListOf<Triple<String, String, String?>>()

    for (ref in favoriteRefs) {
        val doc = ref.get().await()
        if (doc.exists()) {
            val id = doc.id
            val title = doc.getString("Nome") ?: "Senza titolo"
            val img = doc.getString("file")
            favoritePaths.add(Triple(id, title, img))
        }
    }

    return favoritePaths
}


@Composable
fun PathItem(item: String, fileName: String?, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageBitmap = remember(fileName) {
        fileName?.let {
            val file = File(context.filesDir, it)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else null
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .size(150.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Immagine percorso",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = "Icona placeholder",
                    modifier = Modifier
                        .height(72.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.size(8.dp))
            Text(
                item,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

