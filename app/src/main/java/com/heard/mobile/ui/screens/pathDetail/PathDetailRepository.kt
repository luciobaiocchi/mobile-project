package com.heard.mobile.ui.screens.pathDetail

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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