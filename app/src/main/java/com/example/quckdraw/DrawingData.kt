package com.example.quckdraw

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.io.File
import java.util.Date

//apparently Room can't handle Date objects directly...
//Room will use these converters when going from DB <-> Kotlin
class Converters {

    @TypeConverter
    fun fromFile(file: File?): String? {
        return file?.absolutePath
    }

    @TypeConverter
    fun toFile(path: String?): File? {
        return path?.let { File(it) }
    }
}


//Defines a SQLITE table, basically
@Entity(tableName="drawings")
data class DrawingData(
    var filename: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

