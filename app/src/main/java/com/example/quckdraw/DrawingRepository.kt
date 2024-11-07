package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DrawingRepository(
    private val scope: CoroutineScope,
    private val drawingDao: DrawingDAO,
    private val filesDir: File
) {

    // TODO: Function to save a bitmap to file and insert metadata in the Room database
    fun saveDrawing(drawingID: String, bitmap: Bitmap) {
        scope.launch {
            val filename = "$drawingID.png"
            val file = File(filesDir, filename)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Create the DrawingData object without the 'file' parameter
            val drawingEntity = DrawingData(filename = filename)
            drawingDao.insertDrawing(drawingEntity)
        }
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

    // TODO: Function to load all the names of drawings for the list of drawings

}
