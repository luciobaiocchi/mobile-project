package com.heard.mobile.ui.screens.personal.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.heard.mobile.ui.screens.personal.tabs.UserData

@Composable
fun EditProfileDialog(
    currentData: UserData,
    onDismiss: () -> Unit,
    onSave: (UserData) -> Unit
) {
    var nome by remember { mutableStateOf(currentData.nome) }
    var cognome by remember { mutableStateOf(currentData.cognome) }
    var telefono by remember { mutableStateOf(currentData.telefono) }
    var luogoNascita by remember { mutableStateOf(currentData.cittaNascita) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Modifica Profilo")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = cognome,
                        onValueChange = { cognome = it },
                        label = { Text("Cognome") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Telefono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }


                item {
                    OutlinedTextField(
                        value = luogoNascita,
                        onValueChange = { luogoNascita = it },
                        label = { Text("Luogo di Nascita") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },



        confirmButton = {
            TextButton(
                onClick = {

                    val updatedData = UserData(
                        cognome = cognome,
                        cittaNascita = luogoNascita,
                        nome = nome,
                        telefono = telefono,
                    )
                    onSave(updatedData)
                }
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}