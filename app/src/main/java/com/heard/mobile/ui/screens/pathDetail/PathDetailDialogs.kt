package com.heard.mobile.ui.screens.pathDetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PermissionRationaleDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Permesso necessario") },
            text = {
                Text("Per aggiungere un'immagine al percorso, l'app ha bisogno di accedere alla galleria del dispositivo. Questo permesso viene utilizzato esclusivamente per selezionare le immagini che desideri condividere.")
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Concedi permesso")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun PermissionDeniedDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Permesso negato") },
            text = {
                Text("Per aggiungere immagini ai percorsi, è necessario concedere il permesso di accesso alla galleria. Puoi abilitarlo dalle impostazioni dell'app.")
            },
            confirmButton = {
                TextButton(onClick = onOpenSettings) {
                    Text("Apri impostazioni")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun RemoveImageDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Rimuovi immagine") },
            text = {
                Text("Sei sicuro di voler rimuovere l'immagine dal percorso? Questa azione non può essere annullata.")
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Rimuovi")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Annulla")
                }
            }
        )
    }
}

fun openAppSettings(context: Context) {
    try {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossibile aprire le impostazioni", Toast.LENGTH_SHORT).show()
        }
    }
}