package com.heard.mobile.ui.screens.addPath

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.heard.mobile.data.database.Path
import com.heard.mobile.ui.HeardRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AddPathState(
    val nome: String = "",
    val descrizione: String = "",
    val lunghezza: String = "",
    val battitiMedi: String = "",
    val calorie: String = "",
    val durata: String = "",
    val tipo: DocumentReference ?= null,
    val passoMedio: String = "",
    val imageUri: Uri = Uri.EMPTY,
    val file: String? = null
    /*
    * Creatore e Data da prendere in automatico
    *
    */
) {
    val canSubmit get() = nome.isNotBlank()
            && lunghezza.isNotBlank()
            && battitiMedi.isNotBlank()
            && calorie.isNotBlank()
            && durata.isNotBlank()
            && passoMedio.isNotBlank()
            && descrizione.isNotBlank()
           // && imageUri != Uri.EMPTY

    fun submit(
        navController: NavController,
        travelId: String,
        onFailure: (Exception) -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure(Exception("Utente non autenticato"))
            return
        }

        val pathData = hashMapOf(
            "Nome" to nome,
            "Descrizione" to descrizione,
            "Lunghezza" to lunghezza,
            "BattitiMedi" to battitiMedi,
            "Calorie" to calorie,
            "Durata" to durata,
            "PassoMedio" to passoMedio,
            "file" to null,
            "Tipo" to tipo,
            "Creatore" to db.collection("Utenti").document(userId),
            "Data" to Timestamp.now(),
            "Mappa" to file
        )

        db.collection("Percorsi")
            .document(travelId)
            .set(pathData)
            .addOnSuccessListener {
                navController.navigate(HeardRoute.Home)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }


}

interface AddPathActions {
    fun setNome(nome: String)
    fun setLunghezza(lunghezza: String)
    fun setBattitiMedi(battitiMedi: String)
    fun setCalorie(calorie: String)
    fun setDurata(durata: String)
    fun setTipo(tipo: DocumentReference?)
    fun setPassoMedio(passoMedio: String)
    fun setImageUri(imageUri: Uri)
    fun setDescrizione(descrizione: String)
    fun setFile(file: String);

    fun resetState()

}

class AddPathViewModel : ViewModel() {

    init {
        println("AddPathViewModel creato!")
    }

    private val _state = MutableStateFlow(AddPathState())
    val state = _state.asStateFlow()

    val actions = object : AddPathActions {
        override fun setNome(nome: String) {
            _state.update { it.copy(nome = nome) }
        }

        override fun setLunghezza(lunghezza: String) {
            _state.update { it.copy(lunghezza = lunghezza) }
        }

        override fun setBattitiMedi(battitiMedi: String) {
            _state.update { it.copy(battitiMedi = battitiMedi) }
        }

        override fun setCalorie(calorie: String) {
            _state.update { it.copy(calorie = calorie) }
        }

        override fun setDurata(durata: String) {
            _state.update { it.copy(durata = durata) }
        }

        override fun setTipo(tipo: DocumentReference?) {
            _state.update { it.copy(tipo = tipo) }
        }

        override fun setPassoMedio(passoMedio: String) {
            _state.update { it.copy(passoMedio = passoMedio) }
        }

        override fun setImageUri(imageUri: Uri) {
            _state.update { it.copy(imageUri = imageUri) }
        }

        override fun setDescrizione(descrizione: String) {
            _state.update { it.copy(descrizione = descrizione) }
        }

        override fun setFile(file: String) {
            _state.update { it.copy(file = file) }
        }

        override fun resetState() {
            _state.update { AddPathState() }
        }

    }
}
