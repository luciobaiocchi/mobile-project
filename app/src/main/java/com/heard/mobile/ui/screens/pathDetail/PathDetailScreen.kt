package com.heard.mobile.ui.screens.pathDetail

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.personal.CustomTabRow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import java.text.SimpleDateFormat
import java.util.Locale


data class UserProfile(
    val nome: String = "",
    val cognome: String = "",
    val preferiti: List<DocumentReference>? = null, // Assumiamo sia qui per semplicità
    val badge: String = ""
)

data class PathData(
    val Nome: String = "", // Nome del percorso
    val Data: Timestamp? = null, // Data dell'attività
    val Descrizione: String = "",
    val Durata: Int? = null,
    val PassoMedio: Float? = null,
    val BattitiMedi: Int? = null,
    val Calorie: Int? = null,
    val Lunghezza: Int? = null,
    val Creatore: DocumentReference? = null
)


@Composable
fun PathDetailScreen(navController: NavController, travelId: String) {
    val ctx = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()

    // userProfileForCreator: I dati del profilo dell'utente che ha CREATO il percorso
    var userProfileForCreator by remember { mutableStateOf<UserProfile?>(null) }
    // userProfileCurrentViewer: I dati del profilo dell'utente LOGGATO che STA VISUALIZZANDO (per i preferiti)
    var userProfileCurrentViewer by remember { mutableStateOf<UserProfile?>(null) }

    var pathData by remember { mutableStateOf<PathData?>(null) }
    val scope = rememberCoroutineScope()

    var mapView: MapView? by remember { mutableStateOf(null) }
    var placeholderView: ImageView? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        // 1. Recupera i dati specifici del percorso
        val pathDoc = db.collection("Percorsi").document(travelId).get().await()
        val fetchedPathData = pathDoc.toObject(PathData::class.java)
        pathData = fetchedPathData

        // 2. Recupera i dati dell'utente CREATORE
        val creatorUid = fetchedPathData?.Creatore?.id
        if (creatorUid != null) {
            val creatorUserDoc = fetchedPathData.Creatore.get().await()
            userProfileForCreator = creatorUserDoc?.toObject(UserProfile::class.java)
        }

        // 3. Recupera i dati dell'utente LOGGATO (per i preferiti, se necessario)
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val currentUserDoc = db.collection("Utenti").document(currentUserUid).get().await()
            userProfileCurrentViewer = currentUserDoc.toObject(UserProfile::class.java)
        }


        mapView?.let { map ->
            placeholderView?.let { image ->
                GpxLoader.loadPath(ctx, map, travelId, image)
            }
        }
    }

    // Qui estraiamo userFavorites dal profilo dell'utente LOGGATO per renderlo reattivo
    val userFavorites = userProfileCurrentViewer?.preferiti ?: emptyList()


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
                                    toggleFavorite(db, travelId, userFavorites) { updatedList ->
                                        // Aggiorna la lista dei preferiti nel profilo dell'utente LOGGATO
                                        userProfileCurrentViewer = userProfileCurrentViewer?.copy(preferiti = updatedList)
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
                                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
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
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                AndroidView(factory = { context ->
                    val container = FrameLayout(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val map = MapView(context)
                    val image = ImageView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }

                    map.layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )

                    container.addView(map)
                    container.addView(image)

                    mapView = map
                    placeholderView = image

                    container
                },
                    modifier = Modifier.fillMaxSize())

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .align(Alignment.BottomStart)
                        .padding(start = 6.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = pathData?.Nome ?: "Nome percorso",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        )
                        val timestamp = pathData?.Data
                        val date = timestamp?.toDate()
                        val formattedDate = date?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: "Data non disponibile"

                        Text(
                            text = formattedDate,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabItems = listOf("Panoramica", "Statistiche", "Mappa")

            CustomTabRow(
                selectedTabIndex = selectedTabIndex,
                tabItems = tabItems,
                onTabSelected = { selectedTabIndex = it }
            )

            // Contenuto basato sulla tab selezionata
            when (selectedTabIndex) {
                0 -> { // Panoramica
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // User Info (Popolato con i dati dell'utente CREATORE del percorso)
                        val creatorUserName = "${userProfileForCreator?.nome ?: ""} ${userProfileForCreator?.cognome ?: ""}".trim()
                        val activityTimestamp = pathData?.Data
                        val activityDateFormatted = activityTimestamp?.toDate()?.let {
                            // Formatta la data (es. "10 maggio")
                            SimpleDateFormat("dd MMMM", Locale.getDefault()).format(it)
                        } ?: "Data sconosciuta"
                        val activityTimeFormatted = activityTimestamp?.toDate()?.let {
                            // Formatta l'ora (es. "07:45")
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "Ora sconosciuta"



                        Spacer(modifier = Modifier.height(24.dp))

                        UserInfo(
                            userName = creatorUserName, // Usa il nome del creatore
                            activityDate = activityDateFormatted,
                            activityTime = activityTimeFormatted
                        )

                        // Distanza (dal pathData)
                        Text(
                            text = "${pathData?.Lunghezza ?: "N/D"} km",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )



                        // Statistiche Attività (dal pathData)
                        ActivityStats(
                            totalDurationMin = pathData?.Durata.toString(),
                            averagePace = pathData?.PassoMedio.toString(),
                            averageHeartRateBpm = pathData?.BattitiMedi.toString(),
                            totalCaloriesKcal = pathData?.Calorie.toString()
                        )
                    }
                }

                1 -> { // Statistiche (se hai un composable dedicato)
                    Text("Contenuto delle statistiche qui.")
                }
                2 -> { // Mappa (già gestita sopra per la descrizione)
                    ExpandableText(pathData?.Descrizione.orEmpty())
                }
            }
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    minimizedMaxLines: Int = 3
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .padding(5.dp)
        .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        )
        .background(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
        .padding(16.dp)
    ) {
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = if (expanded) "Mostra meno" else "Mostra di più",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 4.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { expanded = !expanded },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun UserInfo(
    userName: String,
    activityDate: String,
    activityTime: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFC5CAE9)), // Un colore simile al lilla/blu tenue dell'immagine
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = userName.firstOrNull()?.toString() ?: "",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$activityDate - $activityTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActivityStats(
    totalDurationMin: String,
    averagePace: String,
    averageHeartRateBpm: String,
    totalCaloriesKcal: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column(modifier = Modifier.weight(1f)) {
            StatItem(label = "Durata totale", value = "$totalDurationMin min")
            Spacer(modifier = Modifier.height(16.dp))
            StatItem(label = "Battiti medi", value = "$averageHeartRateBpm bpm")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            StatItem(label = "Passo medio", value = averagePace)
            Spacer(modifier = Modifier.height(16.dp))
            StatItem(label = "Calorie totali", value = "$totalCaloriesKcal kcal")
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}


suspend fun getFavorites(db: FirebaseFirestore): List<DocumentReference> {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uid = currentUser?.uid ?: return emptyList()

    val snapshot = db.collection("Utenti").document(uid).get().await()
    return snapshot.get("Preferiti") as? List<DocumentReference> ?: emptyList()
}

suspend fun getPathSpecs(db: FirebaseFirestore, travelId: String): PathData? {
    val snapshot = db.collection("Percorsi").document(travelId).get().await()
    return snapshot.toObject(PathData::class.java) // Mappa direttamente a PathData
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