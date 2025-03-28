package com.example.quckdraw

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//this is a DB, we have 1 entity (so we'll get 1 table in SQLite)
//the version stuff is for managing DB migrations
@Database(entities= [DrawingData::class], version = 1, exportSchema = false)
//This lets use have an entity with a "Date" in it which Room won't natively support
@TypeConverters(Converters::class)
abstract class DrawingDatabase : RoomDatabase(){
    abstract fun drawingDAO(): DrawingDAO

    companion object {
        @Volatile
        private var INSTANCE: DrawingDatabase? = null

        fun getDatabase(context: Context): DrawingDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                //if another thread initialized this before we got the lock
                //return the object they created
                if(INSTANCE != null) return INSTANCE!!
                //otherwise we're the first thread here, so create the DB
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrawingDatabase::class.java,
                    "drawing_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

@Dao
interface DrawingDAO {

    //marked as suspend so the thread can yield in case the DB update is slow
    @Insert
    suspend fun insertDrawing(data: DrawingData)
    @Query("SELECT COUNT(*) FROM drawings WHERE filename = :filename")
    suspend fun doesDrawingExist(filename: String): Int

    @Query("SELECT * from drawings ORDER BY timestamp DESC LIMIT 1")
    fun latestDrawing(): Flow<DrawingData>
    @Query("SELECT * from drawings ORDER BY timestamp DESC")
    fun allDrawings(): Flow<List<DrawingData>>
    @Update
    suspend fun updateDrawing(data: DrawingData)
    @Delete
    suspend fun deleteDrawing(drawing: DrawingData)
}