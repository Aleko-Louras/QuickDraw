package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Path
import android.graphics.Paint

data class Pen(
    var color: Int = Color.BLUE,
    var size: Float = 10f
)

enum class Shape {
    CIRCLE, LINE
}

class DrawingViewModel : ViewModel(){

    private val drawingWidth = 800
    private val drawingHeight = 800

    // Bitmap to hold pixels, Canvas to host the draw calls, Paths for creating lines
    // and a Paint for styles
    private val _bitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(_bitmap)
    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Keep track of pen properties with the pen
    private var pen = Pen()
    private val _penLiveData = MutableLiveData(pen)
    val penLiveData: LiveData<Pen> = _penLiveData

    // Store all the paths of the drawing as a list
    private var currentPath: Path = Path()
    private val paths: MutableList<Path> = mutableListOf()

    // Start a new path and move to the touch position
    fun startPath(x: Float, y: Float) {
        currentPath = Path()
        currentPath.moveTo(x, y)
        paths.add(currentPath)
    }

    // Add new lines to the path and add draw them on the canvas
    fun addToPath(x: Float, y: Float) {
        currentPath.lineTo(x, y)
        paint.color = pen.color
        paint.strokeWidth = pen.size
        canvas.drawPath(currentPath, paint)
    }

    fun getBitmap(): Bitmap {
        return _bitmap
    }

    fun setPenColor(color: Int) {
        pen.color = color
       _penLiveData.value = pen
    }

    fun setPenSize(size: Float) {
       pen.size = size
        _penLiveData.value = pen
    }

    fun getNumberOfPaths() : Int{
        return paths.size
    }

}