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
    val filesDir: File
) {

    /**
     * Saves a drawing to the database and files directory.
     */
    fun saveDrawing(drawingName: String, bitmap: Bitmap) {
        scope.launch {
            val filename = drawingName

            val exists = drawingDao.doesDrawingExist(filename) > 0
            if (exists) {
                return@launch
            }
            val file = File(filesDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Create the DrawingData object without the 'file' parameter
            val drawingEntity = DrawingData(
                filename = filename,
                path = file.absolutePath,
                timestamp = Date()
            )
            drawingDao.insertDrawing(drawingEntity)
        }
    }

    suspend fun isDrawingNameUnique(drawingName: String): Boolean {
        val filename = drawingName
        return drawingDao.doesDrawingExist(filename) == 0
    }

    /**
     * Updates a drawing in the database.
     */
    fun updateDrawing(drawingData: DrawingData) {
        scope.launch {
            drawingDao.updateDrawing(drawingData)
        }
    }

    fun getLatestDrawing(): Flow<DrawingData> {
        return drawingDao.latestDrawing()
    }

    /**
     * Retrieves all drawings from the database.
     */
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
}
