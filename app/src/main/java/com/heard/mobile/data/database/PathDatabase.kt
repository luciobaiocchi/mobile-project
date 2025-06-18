package com.heard.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Path::class], version = 2)
abstract class PathDatabase : RoomDatabase() {
    abstract fun pathDAO(): PathDAO
}
