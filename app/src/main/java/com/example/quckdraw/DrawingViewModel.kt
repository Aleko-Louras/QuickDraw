package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Path
import android.graphics.Paint

class DrawingViewModel : ViewModel(){

    private val drawingWidth = 800
    private val drawingHeight = 800

    // Bitmap to hold pixels, Canvas to host the draw calls, Paths for creating lines
    // and a Paint for styles
    private val _bitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(_bitmap)
    private val paint: Paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
    }
    private var currentPath: Path = Path()

    // Store all the paths of the drawing as a list 
    private val paths: MutableList<Path> = mutableListOf()

    private val _penColor : MutableLiveData<Color> = MutableLiveData(Color.valueOf(0f, 0f, 1f))
    val penColor = _penColor as LiveData<Color>
    private val _penSize = MutableLiveData(10f)
    val penSize = _penSize as LiveData<Float>


    // Start a new path and move to the touch position
    fun startPath(x: Float, y: Float) {
        currentPath = Path()
        currentPath.moveTo(x, y)
        paths.add(currentPath)
    }

    // Add new lines to the path and add draw them on the canvas
    fun addToPath(x: Float, y: Float) {
        currentPath.lineTo(x, y)
        canvas.drawPath(currentPath, paint)
    }

    // Draw all paths on the canvas
    private fun drawAllPaths() {
        paths.forEach { path ->
            canvas.drawPath(path, paint)
        }
    }

    fun getBitmap(): Bitmap {
        return _bitmap
    }

    fun setPenColor(color: Color) {
        _penColor.value = color
        paint.color = color.toArgb()
    }

    fun setPenSize(size: Float) {
        _penSize.value = size
        paint.strokeWidth = size
    }
}