package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Path
import android.graphics.Paint
import kotlin.math.pow

data class Pen(
    var color: Int = Color.BLUE,
    var size: Float = 10f,
    var shape: Shape = Shape.LINE
)

enum class Shape {
    LINE, CIRCLE, TRIANGLE, SQUARE
}

class DrawingViewModel : ViewModel(){

    private val drawingWidth = 800
    private val drawingHeight = 800

    private var startX = 0f
    private var startY = 0f

    private val _bitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(_bitmap)
    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private var pen = Pen()
    private val _penLiveData = MutableLiveData(pen)
    val penLiveData: LiveData<Pen> = _penLiveData

    private var currentPath: Path = Path()
    private val paths: MutableList<Path> = mutableListOf()

    // Start a new path and move to the touch position
    fun startPath(x: Float, y: Float) {
        currentPath = Path().apply {
            moveTo(x, y)
        }
        paths.add(currentPath)
    }
    fun startShape(x: Float, y: Float) {
        startX = x
        startY = y
    }


    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return ((x2 - x1).pow(2) + (y2 - y1).pow(2)).pow(0.5f)
    }
    fun updateShape(x: Float, y: Float) {
        paint.color = pen.color
        paint.strokeWidth = pen.size
        paint.style = Paint.Style.FILL

        when (pen.shape) {
            Shape.LINE -> {}
            Shape.CIRCLE -> {
                val radius = distance(startX, startY, x, y)
                canvas.drawCircle(startX, startY, radius, paint)
            }
            Shape.TRIANGLE -> {
                val size = distance(startX, startY, x, y)
                val path = Path().apply {
                    moveTo(startX, startY - size)
                    lineTo(startX - size, startY + size)
                    lineTo(startX + size, startY + size)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            Shape.SQUARE -> {
                val size = distance(startX, startY, x, y)
                canvas.drawRect(startX - size, startY - size, startX + size, startY + size, paint)
            }

        }
    }

    fun addToPath(x: Float, y: Float) {
        paint.color = pen.color
        paint.strokeWidth = pen.size
        paint.style = Paint.Style.STROKE
        currentPath.lineTo(x, y)
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

    fun setPenShape(shape: Shape) {
        pen.shape = shape
        _penLiveData.value = pen
    }

    fun getNumberOfPaths() : Int{
        return paths.size
    }

    fun isLine(): Boolean {
      return pen.shape == Shape.LINE
    }

}