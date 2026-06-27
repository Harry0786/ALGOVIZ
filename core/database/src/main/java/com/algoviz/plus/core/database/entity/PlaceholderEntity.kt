package com.algoviz.plus.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placeholder_table")
data class PlaceholderEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Placeholder"
)
