package com.heard.mobile.ui.screens.pathDetail

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageBox(
    imageBitmap: Bitmap?,
    isImageLoading: Boolean,
    isUploadingImage: Boolean,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier,
    onImageOptionSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Scegli immagine", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                ListItem(
                    headlineContent = { Text("Scatta foto") },
                    leadingContent = {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        showSheet = false
                        onImageOptionSelected("Camera")
                        onImageClick();
                    }
                )

                ListItem(
                    headlineContent = { Text("Galleria") },
                    leadingContent = {
                        Icon(Icons.Outlined.Image, contentDescription = null)
                    },
                    modifier = Modifier.clickable {
                        showSheet = false
                        onImageOptionSelected("Galleria")
                        onImageClick()
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                if (imageBitmap == null && !isImageLoading && !isUploadingImage) {
                    coroutineScope.launch {
                        showSheet = true
                    }
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
            } else -> {
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

@Composable
fun ExpandableTextWithImage(
    text: String,
    minimizedMaxLines: Int = 3,
    imageBitmap: Bitmap?,
    isImageLoading: Boolean,
    isUploadingImage: Boolean,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    onImageOptionChange: (String) -> Unit
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

        ImageBox(
            imageBitmap = imageBitmap,
            isImageLoading = isImageLoading,
            isUploadingImage = isUploadingImage,
            onImageClick = onImageClick,
            onRemoveImage = onRemoveImage,
            onImageOptionSelected = onImageOptionChange

        )
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
    totalDurationMin: String? = "N/A",
    averagePace: String? = "N/A",
    averageHeartRateBpm: String? = "N/A",
    totalCaloriesKcal: String? = "N/A"
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
            if (averagePace != null) {
                StatItem(label = "Passo medio", value = averagePace)
            }
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