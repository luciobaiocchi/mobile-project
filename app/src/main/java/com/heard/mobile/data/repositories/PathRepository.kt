package com.heard.mobile.data.repositories

import android.content.ContentResolver
import android.net.Uri
import com.heard.mobile.data.database.Path
import com.heard.mobile.data.database.PathDAO
import com.heard.mobile.utils.saveImageToStorage
import kotlinx.coroutines.flow.Flow

class PathRepository(
    private val dao: PathDAO,
    private val contentResolver: ContentResolver
) {
    val paths: Flow<List<Path>> = dao.getAll()

    suspend fun upsert(path: Path) {
        if (path.imageUri?.isNotEmpty() == true) {
            val imageUri = saveImageToStorage(
                Uri.parse(path.imageUri),
                contentResolver,
                "TravelDiary_Trip${path.name}"
            )
            dao.upsert(path.copy(imageUri = imageUri.toString()))
        } else {
            dao.upsert(path)
        }
    }

    suspend fun delete(path: Path) = dao.delete(path)
}
