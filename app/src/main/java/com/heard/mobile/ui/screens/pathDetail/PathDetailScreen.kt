package com.heard.mobile.ui.screens.pathDetail

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.composables.AppBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun PathDetailScreen(navController: NavController, travelId: String) {
    val ctx = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    var userFavorites by remember { mutableStateOf<List<DocumentReference>>(emptyList()) }
    val scope = rememberCoroutineScope()
    var path by remember { mutableStateOf<Map<String, Any?>?>(null) }
    

    LaunchedEffect(Unit) {
        userFavorites = getFavorites(db);
        path = getPathSpecs(db, travelId);

    }

    fun shareDetails() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, travelId)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Travel")
        if (shareIntent.resolveActivity(ctx.packageManager) != null) {
            ctx.startActivity(shareIntent)
        }
    }


    Scaffold(
        topBar = { AppBar(navController, title = "Dettagli Percorso") },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (expanded) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Condividi") },
                            onClick = {
                                shareDetails()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Preferiti") },
                            onClick = {
                                scope.launch {
                                    toggleFavorite(db, travelId, userFavorites) {
                                        userFavorites = it
                                    }
                                }
                            },
                            leadingIcon = {
                                if( userFavorites.any{ it.id == travelId } ) {
                                    Icon(Icons.Outlined.Favorite, contentDescription = null)
                                } else {
                                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
                                }

                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Aggiungi foto") },
                            onClick = {
                                // Handle more actions
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.CameraAlt
                                    , contentDescription = null)
                            }
                        )
                    }
                }

                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { expanded = !expanded }
                ) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                }
            }
        }
        ,
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(contentPadding).padding(12.dp).fillMaxSize()
        ) {
            Image(
                Icons.Outlined.Image,
                "Travel picture",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(36.dp)
            )
            Text(
                path?.get("Nome")?.toString() ?: "Nome non disponibile",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "01/01/2024",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.size(8.dp))
            Text(
                "Descrizione",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


suspend fun getFavorites(db: FirebaseFirestore): List<DocumentReference> {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid

    val snapshot = db.collection("Utenti").document(uid!!).get().await()
    val favorites = (snapshot.get("Preferiti") as? List<DocumentReference>) ?: emptyList()

    return favorites
}

suspend fun getPathSpecs( db: FirebaseFirestore, travelId: String ): Map<String, Any?> {
    val snapshot = db.collection("Percorsi").document(travelId).get().await()
    return snapshot.data ?: emptyMap();
}

suspend fun toggleFavorite(
    db: FirebaseFirestore,
    travelId: String,
    userFavorites: List<DocumentReference>,
    onFavoritesChanged: (List<DocumentReference>) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val uid = currentUser.uid
    val userDoc = db.collection("Utenti").document(uid)

    val isFav = userFavorites.any { it.id == travelId }
    val newFavorites = if (isFav) {
        userFavorites.filter { it.id != travelId }
    } else {
        userFavorites + db.collection("Percorsi").document(travelId)
    }

    userDoc.update("Preferiti", newFavorites).await()
    onFavoritesChanged(newFavorites)
}
