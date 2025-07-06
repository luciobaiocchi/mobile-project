package com.heard.mobile.ui.screens.pathDetail

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.R
import com.heard.mobile.ui.HeardRoute
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.personal.CustomTabRow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


data class UserProfile(
    val Nome: String = "",
    val Cognome: String = "",
    val preferiti: List<DocumentReference>? = null,
    val Badge: String = ""
)

data class PathData(
    val Nome: String = "",
    val Data: Timestamp? = null,
    val Descrizione: String = "",
    val Durata: Int? = null,
    val PassoMedio: Float? = null,
    val BattitiMedi: Int? = null,
    val Calorie: Int? = null,
    val Lunghezza: Int? = null,
    val Creatore: DocumentReference? = null,
    val file: String = "" // Campo per il nome del file immagine
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
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isImageLoading by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Nuove variabili per gestire i permessi
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    var showRemoveDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var mapView: MapView? by remember { mutableStateOf(null) }
    var placeholderView: ImageView? by remember { mutableStateOf(null) }

    var paths: List<Pair<String, String>> = emptyList()

    // Launcher per selezionare l'immagine
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                saveImageLocally(ctx, it, travelId, db) { success, newImageBitmap, fileName ->
                    if (success && newImageBitmap != null) {
                        imageBitmap = newImageBitmap
                        // Aggiorna anche pathData con il nuovo nome file
                        pathData = pathData?.copy(file = fileName)
                        Toast.makeText(ctx, "Immagine caricata con successo!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, "Errore durante il caricamento dell'immagine", Toast.LENGTH_SHORT).show()
                    }
                    isUploadingImage = false
                }
            }
        } ?: run {
            isUploadingImage = false
        }
    }

    // Launcher per i permessi migliorato
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permesso concesso - apri la galleria
            isUploadingImage = true
            imagePickerLauncher.launch("image/*")
        } else {
            // Permesso negato - mostra dialog per le impostazioni
            showPermissionDialog = true
        }
    }



    // Funzione per controllare i permessi e aprire la galleria
    fun openImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permesso già concesso
                isUploadingImage = true
                imagePickerLauncher.launch("image/*")
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    (ctx as? androidx.activity.ComponentActivity)?.let { activity ->
                        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    } == true -> {
                // Mostra spiegazione del permesso
                showRationaleDialog = true
            }
            else -> {
                // Richiedi il permesso
                permissionLauncher.launch(permission)
            }
        }
    }

    // Dialog per spiegare il permesso
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Permesso necessario") },
            text = {
                Text("Per aggiungere un'immagine al percorso, l'app ha bisogno di accedere alla galleria del dispositivo. Questo permesso viene utilizzato esclusivamente per selezionare le immagini che desideri condividere.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = false
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            android.Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        permissionLauncher.launch(permission)
                    }
                ) {
                    Text("Concedi permesso")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per indirizzare alle impostazioni
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permesso negato") },
            text = {
                Text("Per aggiungere immagini ai percorsi, è necessario concedere il permesso di accesso alla galleria. Puoi abilitarlo dalle impostazioni dell'app.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Apri le impostazioni dell'app
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", ctx.packageName, null)
                            }
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback: apri le impostazioni generali
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                ctx.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "Impossibile aprire le impostazioni", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Apri impostazioni")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Rimuovi immagine") },
            text = {
                Text("Sei sicuro di voler rimuovere l'immagine dal percorso? Questa azione non può essere annullata.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        // Rimuovi l'immagine
                        pathData?.file?.let { filePath ->
                            if (filePath.isNotEmpty()) {
                                scope.launch {
                                    removeImageLocally(ctx, travelId, filePath, db) { success ->
                                        if (success) {
                                            // Aggiorna immediatamente lo stato
                                            imageBitmap = null
                                            pathData = pathData?.copy(file = "")
                                            Toast.makeText(ctx, "Immagine rimossa con successo!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(ctx, "Errore durante la rimozione dell'immagine", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Rimuovi", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance()
            .load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        // 1. Recupera i dati specifici del percorso
        val pathDoc = db.collection("Percorsi").document(travelId).get().await()
        val fetchedPathData = pathDoc.toObject(PathData::class.java)
        pathData = fetchedPathData

        // 2. Carica l'immagine dal campo "file" (path locale)
        fetchedPathData?.file?.let { filePath ->
            if (filePath.isNotEmpty()) {
                isImageLoading = true
                loadImageFromLocalPath(ctx, filePath) { bitmap ->
                    imageBitmap = bitmap
                    isImageLoading = false
                }
            }
        }

        // 3. Recupera i dati dell'utente CREATORE
        val creatorUid = fetchedPathData?.Creatore?.id
        if (creatorUid != null) {
            val creatorUserDoc = fetchedPathData.Creatore.get().await()
            userProfileForCreator = creatorUserDoc?.toObject(UserProfile::class.java)
        }

        // 4. Recupera i dati dell'utente LOGGATO (per i preferiti, se necessario)
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            val currentUserDoc = db.collection("Utenti").document(currentUserUid).get().await()
            userProfileCurrentViewer = currentUserDoc.toObject(UserProfile::class.java)
        }

        paths = com.heard.mobile.ui.screens.path.getPathsName(db)

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
        topBar = { AppBar(navController, title = "Dettagli Percorso") }
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
                AndroidView(
                    factory = { context ->
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
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
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
            val tabItems = listOf("Panoramica", "Descrizione", "Percorsi simili")

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
                        val creatorUserName =
                            "${userProfileForCreator?.Nome ?: ""} ${userProfileForCreator?.Cognome ?: ""}".trim()
                        val activityTimestamp = pathData?.Data
                        val activityDateFormatted = activityTimestamp?.toDate()?.let {
                            SimpleDateFormat("dd MMMM", Locale.getDefault()).format(it)
                        } ?: "Data sconosciuta"
                        val activityTimeFormatted = activityTimestamp?.toDate()?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: "Ora sconosciuta"

                        Spacer(modifier = Modifier.height(24.dp))

                        UserInfo(
                            userName = creatorUserName,
                            activityDate = activityDateFormatted,
                            activityTime = activityTimeFormatted
                        )

                        Spacer(modifier = Modifier.height(24.dp))

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

                1 -> {
                    ExpandableTextWithImage(
                        text = pathData?.Descrizione.orEmpty(),
                        imageBitmap = imageBitmap,
                        isImageLoading = isImageLoading,
                        isUploadingImage = isUploadingImage,
                        onImageClick = { openImagePicker() },
                        onRemoveImage = { showRemoveDialog = true }
                    )
                }

                2 -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 80.dp),
                        modifier = Modifier.padding(contentPadding)
                    ) {
                        items(paths) { path ->
                            com.heard.mobile.ui.screens.path.PathItem(
                                item = path.second,
                                onClick = { navController.navigate(HeardRoute.TravelDetails(path.first)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableTextWithImage(
    text: String,
    minimizedMaxLines: Int = 3,
    imageBitmap: Bitmap?,
    isImageLoading: Boolean,
    isUploadingImage: Boolean,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit // Nuovo parametro
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Descrizione del percorso",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )

        ExpandableText(
            text = text,
            minimizedMaxLines = minimizedMaxLines
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Immagine del percorso",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    if (imageBitmap == null && !isImageLoading && !isUploadingImage) {
                        onImageClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                isUploadingImage -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Caricamento in corso...")
                    }
                }
                isImageLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                imageBitmap != null -> {
                    Image(
                        bitmap = imageBitmap.asImageBitmap(),
                        contentDescription = "Immagine del percorso",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay con pulsanti per modificare/rimuovere l'immagine
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pulsante per cambiare immagine
                            FloatingActionButton(
                                onClick = onImageClick,
                                modifier = Modifier.size(40.dp),
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    Icons.Outlined.CameraAlt,
                                    contentDescription = "Cambia immagine",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                            }

                            // Pulsante per rimuovere immagine
                            FloatingActionButton(
                                onClick = onRemoveImage,
                                modifier = Modifier.size(40.dp),
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                    contentDescription = "Rimuovi immagine",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = "Aggiungi immagine",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tocca per aggiungere un'immagine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

    Column(
        modifier = Modifier
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
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFF3F51B5),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(12.dp)
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFC5CAE9)),
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

fun loadImageFromLocalPath(
    context: Context,
    filePath: String,
    onImageLoaded: (Bitmap?) -> Unit
) {
    try {
        val file = File(context.filesDir, filePath)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            onImageLoaded(bitmap)
        } else {
            try {
                val inputStream = context.assets.open(filePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                onImageLoaded(bitmap)
            } catch (e: Exception) {
                onImageLoaded(null)
            }
        }
    } catch (e: Exception) {
        onImageLoaded(null)
    }
}

suspend fun saveImageLocally(
    context: Context,
    imageUri: Uri,
    travelId: String,
    db: FirebaseFirestore,
    onSaveComplete: (Boolean, Bitmap?, String) -> Unit // Aggiungi fileName come parametro
) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val compressedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            minOf(bitmap.width, 1024),
            minOf(bitmap.height, 1024),
            true
        )

        val fileName = "path_${travelId}_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        val outputStream = FileOutputStream(file)
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()

        db.collection("Percorsi").document(travelId)
            .update("file", fileName)
            .await()

        onSaveComplete(true, compressedBitmap, fileName)
    } catch (e: Exception) {
        onSaveComplete(false, null, "")
    }
}

suspend fun removeImageLocally(
    context: Context,
    travelId: String,
    filePath: String,
    db: FirebaseFirestore,
    onRemoveComplete: (Boolean) -> Unit
) {
    try {
        // Rimuovi il file locale
        val file = File(context.filesDir, filePath)
        if (file.exists()) {
            file.delete()
        }

        // Aggiorna Firestore rimuovendo il campo file
        db.collection("Percorsi").document(travelId)
            .update("file", "")
            .await()

        onRemoveComplete(true)
    } catch (e: Exception) {
        onRemoveComplete(false)
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
    return snapshot.toObject(PathData::class.java)
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