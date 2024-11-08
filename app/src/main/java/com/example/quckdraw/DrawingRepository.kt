package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class DrawingRepository(
    private val scope: CoroutineScope,
    private val drawingDao: DrawingDAO,
    private val filesDir: File
) {

    // TODO: Function to save a bitmap to file and insert metadata in the Room database
    fun saveDrawing(drawingName: String, bitmap: Bitmap, path: String) {
        scope.launch {
            val filename = "$drawingName.png"
            val file = File(filesDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Create the DrawingData object without the 'file' parameter
            val drawingEntity = DrawingData(
                filename = filename,
                path = path,
                timestamp = Date()
            )
            drawingDao.insertDrawing(drawingEntity)
        }
    }

    fun getLatestDrawing(): Flow<DrawingData> {
        return drawingDao.latestDrawing()
    }
    fun getAllDrawings(): Flow<List<DrawingData>> {
        return drawingDao.allDrawings()
    }



    // TODO: Function to load a bitmap from file
    fun loadDrawing(drawingData: DrawingData): Bitmap? {
        val file = File(filesDir, drawingData.filename)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    // TODO: Function to delete a drawing
    fun deleteDrawing(drawingData: DrawingData) {
        scope.launch {
            drawingDao.deleteDrawing(drawingData)
            val file = File(filesDir, drawingData.filename)
            file.delete()
        }
    }

    // TODO: Function to load all the names of drawings for the list of drawings

}
