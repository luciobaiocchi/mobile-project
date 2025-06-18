package com.heard.mobile.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Path (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo
    var name: String,

    @ColumnInfo
    var pathURI: String,

    @ColumnInfo
    var favourite: Boolean,

    @ColumnInfo
    var date: String,

    @ColumnInfo
    var imageUri: String?,

    @ColumnInfo
    val lengthKM: Int = 0,

    @ColumnInfo
    val durationMIN: Int = 0,
)
