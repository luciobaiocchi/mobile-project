package com.heard.mobile.ui.screens.pathDetail

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.benchmark.perfetto.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.path.getPathsName
import com.heard.mobile.ui.screens.personal.CustomTabRow
import com.heard.mobile.utils.rememberCameraLauncher
import com.heard.mobile.utils.saveImageToStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import java.text.SimpleDateFormat
import java.util.Locale


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
    val Durata: String? = null,
    val PassoMedio: String? = null,
    val BattitiMedi: String? = null,
    val Calorie: String? = null,
    val Lunghezza: String? = null,
    val Creatore: DocumentReference? = null,
    val file: String = "" // Campo per il nome del file immagine
)

@Composable
fun PathDetailScreen(navController: NavController, travelId: String) {
    val ctx = LocalContext.current
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

    var paths by remember { mutableStateOf<List<Triple<String, String, String?>>>(emptyList()) }

    var imageOption by remember { mutableStateOf("") };


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


    val cameraLauncher = rememberCameraLauncher(
        onPictureTaken = { uri ->
            uri.let {
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
        })



    // Funzione per controllare i permessi e aprire la galleria
    fun openImagePicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permesso già concesso
                isUploadingImage = true

                if (imageOption == "Galleria"){
                    imagePickerLauncher.launch("image/*")
                } else if (imageOption == "Camera") {
                    cameraLauncher.captureImage()
                }


            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    (ctx as? ComponentActivity)?.let { activity ->
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
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
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
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
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", ctx.packageName, null)
                            }
                            ctx.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback: apri le impostazioni generali
                            try {
                                val intent = Intent(Settings.ACTION_SETTINGS).apply {
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

        paths = getPathsName(db)

        mapView?.let { map ->
            placeholderView?.let { image ->
                GpxLoader.loadPath(ctx, map, travelId, image)
            }
        }
    }

    var userFavorites = userProfileCurrentViewer?.preferiti ?: emptyList()

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
                        .height(130.dp) // Aumentata
                        .background(color = Color.Black.copy(alpha = 0.6f))
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = pathData?.Nome ?: "Nome percorso",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { shareDetails() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Condividi")
                            }

                            FavoriteButton(
                                db = db,
                                travelId = travelId,
                                userProfileCurrentViewer = userProfileCurrentViewer,
                                scope = scope
                            )

                        }
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
                            totalDurationMin = pathData?.Durata,
                            averagePace = pathData?.PassoMedio,
                            averageHeartRateBpm = pathData?.BattitiMedi,
                            totalCaloriesKcal = pathData?.Calorie
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
                        onRemoveImage = { showRemoveDialog = true },
                        onImageOptionChange = {newValue  ->
                            imageOption = newValue
                        }
                    )
                }

                2 -> {
//                    LazyVerticalGrid(
//                        columns = GridCells.Fixed(1),
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 80.dp),
//                        modifier = Modifier.padding(contentPadding)
//                    ) {
//                        items(paths) { path ->
//                            PathItem(
//                                item = path.second,
//                                fileName = path.third,
//                                onClick = { navController.navigate(HeardRoute.TravelDetails(path.first)) }
//                            )
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteButton(
    db: FirebaseFirestore,
    travelId: String,
    userProfileCurrentViewer: UserProfile?,
    scope: CoroutineScope
) {
    val travelRef = db.document("Percorsi/$travelId")

    // Usiamo remember per mantenere uno stato locale reattivo
    var userFavorites by remember { mutableStateOf(userProfileCurrentViewer?.preferiti ?: emptyList()) }

    // Verifica se l'id è già nei preferiti
    val isFavorite = userFavorites.any { it.path == travelRef.path }

    Button(
        onClick = {
            scope.launch {
                toggleFavorite(db, travelId, userFavorites) {
                    userFavorites = it // aggiorna lo stato locale
                }
            }
        },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFavorite) Color.Red else Color.Gray
        )
    ) {
        Text(if (isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti")
    }
}

