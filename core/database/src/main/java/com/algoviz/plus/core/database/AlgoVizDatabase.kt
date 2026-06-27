package com.algoviz.plus.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.algoviz.plus.core.database.converter.DateConverter
import com.algoviz.plus.core.database.entity.PlaceholderEntity

@Database(
    entities = [
        PlaceholderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AlgoVizDatabase : RoomDatabase() {
    // DAOs will be added here
}
