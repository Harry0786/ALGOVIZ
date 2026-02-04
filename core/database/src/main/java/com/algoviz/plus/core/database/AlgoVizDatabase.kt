package com.algoviz.plus.core.database

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.algoviz.plus.core.database.converter.DateConverter

// TODO: Add @Database annotation when entities are defined
// @Database(
//     entities = [
//         // Add entities here
//     ],
//     version = 1,
//     exportSchema = false
// )
@TypeConverters(DateConverter::class)
abstract class AlgoVizDatabase : RoomDatabase() {
    // DAOs will be added here
    // abstract fun userDao(): UserDao
}
