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
        paint.color = pen.color
        paint.strokeWidth = pen.size
        currentPath.lineTo(x, y)
        canvas.drawPath(currentPath, paint)
    }

    // Draw shapes on the canvas as the tapped position
    fun drawShapeAt(x: Float, y: Float) {
        paint.color = pen.color
        paint.strokeWidth = pen.size
        paint.style = Paint.Style.FILL

        when(pen.shape) {
            Shape.CIRCLE -> {
                val radius = pen.size
                canvas.drawCircle(x, y, radius, paint)
            }
            Shape.TRIANGLE -> {
                val size = pen.size
                val path = Path().apply {
                    moveTo(x, y - size)
                    lineTo(x - size, y + size)
                    lineTo(x + size, y + size)
                    close()
                }
                canvas.drawPath(path, paint)
            }
            Shape.SQUARE -> {
                val size = pen.size
                canvas.drawRect(x - size, y - size, x + size, y + size,  paint)
            }
            Shape.LINE -> {}
        }
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