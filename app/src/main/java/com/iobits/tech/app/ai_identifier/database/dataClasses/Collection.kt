package com.iobits.tech.app.ai_identifier.database.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val stringToSplit: String,
    val image: String ,// You can store an image path or URL here
    val detect: String
)
