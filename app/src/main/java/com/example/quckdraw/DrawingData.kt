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
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}

//Defines a SQLITE table, basically
@Entity(tableName="drawings")
data class DrawingData(var filename: String,
                        var file: File
                       ){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0 // integer primary key for the DB
}
