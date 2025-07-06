package com.heard.mobile.ui.screens.pathDetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

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
    onSaveComplete: (Boolean, Bitmap?, String) -> Unit
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
        val file = File(context.filesDir, filePath)
        if (file.exists()) {
            file.delete()
        }

        db.collection("Percorsi").document(travelId)
            .update("file", "")
            .await()

        onRemoveComplete(true)
    } catch (e: Exception) {
        onRemoveComplete(false)
    }
}