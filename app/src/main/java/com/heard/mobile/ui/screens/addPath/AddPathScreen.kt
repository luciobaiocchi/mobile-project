package com.heard.mobile.ui.screens.addPath

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.heard.mobile.ui.composables.AppBar
import com.heard.mobile.ui.screens.pathDetail.ImageBox
import com.heard.mobile.ui.screens.pathDetail.saveImageLocally
import com.heard.mobile.utils.rememberCameraLauncher
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddPathScreen(
    navController: NavController,
    viewModel: AddPathViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsState().value
    val actions = viewModel.actions

    val context = LocalContext.current
    val gpxFileUri = remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            gpxFileUri.value = it

            val inputStream = context.contentResolver.openInputStream(it)
            val content = inputStream?.bufferedReader().use { reader -> reader?.readText() }

            if (content != null) {
                actions.setFile(content)
            }
        }
    }



    Scaffold(
        topBar = { AppBar(navController, title = "Aggiungi percorso") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    launcher.launch("*/*")
                }) {
                Text("GPX")
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .verticalScroll(scrollState)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.nome,
                onValueChange = actions::setNome,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.descrizione,
                onValueChange = actions::setDescrizione,
                label = { Text("Descrizione") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.durata,
                onValueChange = { newValue ->
                    actions.setDurata(newValue)
                },
                label = { Text("Durata") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.calorie,
                onValueChange = { newValue ->
                    actions.setCalorie(newValue)
                },
                label = { Text("Calorie") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.lunghezza,
                onValueChange = { newValue ->
                    actions.setLunghezza(newValue)
                },
                label = { Text("Lunghezza") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.battitiMedi,
                onValueChange = { newValue ->
                    actions.setBattitiMedi(newValue)
                },
                label = { Text("Battiti medi") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.passoMedio,
                onValueChange = { newValue ->
                    actions.setPassoMedio(newValue)
                },
                label = { Text("Passo medio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(24.dp))

            ClickableImagePicker(
                imageUri = imageUri,
                onImagePicked = {
                    imageUri = it
                },
                modifier = Modifier
                    .padding(8.dp)
                    .width(300.dp)
                    .height(200.dp)
            )

            Spacer(Modifier.size(24.dp))


            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    if (state.canSubmit) {
                        scope.launch {
                            val db = FirebaseFirestore.getInstance()
                            val newDocRef = db.collection("Percorsi").document()
                            val travelId = newDocRef.id

                            try {
                                // 1. Esegui submit con travelId
                                state.submit(navController, travelId) { e ->
                                    Toast.makeText(context, "Errore: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }

                                // 2. Salva immagine se esiste
                                if (imageUri != null) {
                                    saveImageLocally(
                                        context = context,
                                        imageUri = imageUri!!,
                                        travelId = travelId,
                                        db = db
                                    ) { success, _, _ ->
                                        if (!success) {
                                            Toast.makeText(context, "Errore nel salvataggio immagine", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Errore nella creazione del percorso", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Aggiungi percorso", color = Color.White)
            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableImagePicker(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    onImagePicked: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult (
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onImagePicked(it)
        }
    }

    val cameraLauncher = rememberCameraLauncher(
        onPictureTaken = {
            uri: Uri? ->
            uri?.let {
                onImagePicked(it)
            }

        })


    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Scegli un'opzione", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showBottomSheet = false
                        launcher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scegli dalla galleria")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        showBottomSheet = false
                        cameraLauncher.captureImage()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scatta foto")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { showBottomSheet = true }
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Pick Image",
                tint = Color.DarkGray,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
