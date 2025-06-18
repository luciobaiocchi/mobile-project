package com.heard.mobile.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PathDAO {
    @Query("SELECT * FROM path ORDER BY name ASC")
    fun getAll(): Flow<List<Path>>

    @Upsert
    suspend fun upsert(path: Path)

    @Delete
    suspend fun delete(item: Path)
}
