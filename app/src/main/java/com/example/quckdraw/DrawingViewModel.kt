package com.example.quckdraw

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.graphics.Path
import android.graphics.Paint
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.pow
import java.io.FileOutputStream
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

// class of pen
data class Pen(
    var color: Int = Color.BLUE,
    var size: Float = 10f,
    var shape: Shape = Shape.LINE
)
// class of shape
enum class Shape {
    LINE, CIRCLE, TRIANGLE, SQUARE
}

// This factory class allows us to define custom constructors for the view model
class DrawingViewModelFactory(private val repository: DrawingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DrawingViewModel(private val repository: DrawingRepository) : ViewModel() {

    private val _drawings = MutableLiveData(
        mutableMapOf<String, Bitmap>())

    var currentDrawingName: String? = null
    private val currentDrawingBitmap = MutableLiveData<Bitmap>()

    val drawingsList: LiveData<List<DrawingData>> = repository.getAllDrawings().asLiveData()

    fun uploadDrawing(drawingData: DrawingData) {
        val bitmap = currentDrawingBitmap.value

        val storageReference = Firebase.storage.reference
        val filePath = "${user?.uid}/${currentDrawingName}.png"

        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 0, baos)
        val imageData = baos.toByteArray()

        viewModelScope.launch {
            try {
                val successs = uploadData(storageReference, filePath, imageData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //returns whether the upload was successful
    suspend fun uploadData(ref: StorageReference, path: String, data: ByteArray): Boolean {
        val fileRef = ref.child(path)
        return suspendCoroutine { continuation ->
            val uploadTask = fileRef.putBytes(data)
            uploadTask
                .addOnFailureListener { e ->
                    Log.e("PICUPLOAD", "Failed !$e")
                    continuation.resume(false)
                }
                .addOnSuccessListener {
                    Log.d("PICUPLOAD", "success")
                    continuation.resume(true)
                }
        }
    }

    // update the drawing if the drawing is already created
    suspend fun updateDrawing(fileName: String, filePath: String) {
        val path = if (filePath.isNotBlank()) filePath else File(repository.filesDir, fileName).absolutePath

        val file = File(path)
        FileOutputStream(file).use { out ->
            _bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val drawingData = DrawingData(filename = fileName, path = path, timestamp = Date())
        repository.updateDrawing(drawingData)
    }

    //ChatGPT helped to debug the loadDrawing, it added  currentDrawingBitmap.value = _bitmap and  to
    //to set the current bit map to be correct one
    // load the selected drawing to display page
    fun loadDrawing(name: String) {
        currentDrawingName = name
        val file = File(repository.filesDir, name)
        if (file.exists()) {
            _bitmap = BitmapFactory.decodeFile(file.absolutePath).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            _bitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888)
        }
        canvas.setBitmap(_bitmap)
        currentDrawingBitmap.value = _bitmap
    }

    // save the file's drawing
    suspend fun saveDrawing(fileName: String) {
        repository.saveDrawing(fileName, _bitmap)
        //_drawings.value = repository.getAllDrawings().asLiveData().value
    }

    // create a new drawing page
    suspend fun createNewDrawing(fileName: String): Boolean {
        if (!repository.isDrawingNameUnique(fileName) ) {
            return false
        }
        _bitmap.eraseColor(Color.WHITE)
        currentPath.reset()
        pen = Pen()
        _penLiveData.value = pen
        currentDrawingName = fileName
        currentDrawingBitmap.value = _bitmap
        _drawings.value = _drawings.value?.toMutableMap()?.apply {
            put(fileName, _bitmap)
        }
        saveDrawing(fileName)
        return true
    }

    // delete the selected drawing
    suspend fun deleteDrawing(drawingData: DrawingData){
        repository.deleteDrawing(drawingData)
    }
    //size of the canvas
    private val drawingWidth = 800
    private val drawingHeight = 800

    //the path coordinate
    private var startX = 0f
    private var startY = 0f

    private var _bitmap = Bitmap.createBitmap(drawingWidth, drawingHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(_bitmap)
    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    //set pen object and make it livedata
    private var pen = Pen()
    private val _penLiveData = MutableLiveData(pen)
    val penLiveData: LiveData<Pen> = _penLiveData

    private var currentPath: Path = Path()

    /**
     * Start a new path and move to the new position
     */
    fun startPath(x: Float, y: Float) {
        currentPath = Path().apply {
            moveTo(x, y)
        }
    }

    /**
     * initialize start values for shapes
     */
    fun startShape(x: Float, y: Float) {
        startX = x
        startY = y
    }

    /**
     * returns the computed distance between to points
     */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return ((x2 - x1).pow(2) + (y2 - y1).pow(2)).pow(0.5f)
    }

    /**
     * Handle logic for pen shape change and drawing
     */
    fun updateShape(x: Float, y: Float) {

        paint.color = pen.color
        paint.strokeWidth = pen.size
        paint.style = Paint.Style.FILL

        when (pen.shape) {

            Shape.LINE -> {}//default shape

            Shape.CIRCLE -> { //draw a circle
                val radius = distance(startX, startY, x, y)
                canvas.drawCircle(startX, startY, radius, paint)
            }

            Shape.TRIANGLE -> {//draw triangle
                val size = distance(startX, startY, x, y)
                val path = Path().apply {
                    moveTo(startX, startY - size)
                    lineTo(startX - size, startY + size)
                    lineTo(startX + size, startY + size)
                    close()
                }
                canvas.drawPath(path, paint)
            }

            Shape.SQUARE -> {//draw square
                val size = distance(startX, startY, x, y)
                canvas.drawRect(startX - size, startY - size, startX + size, startY + size, paint)
            }

        }
    }

    /**
     * draws a path to x and y
     */
    fun addToPath(x: Float, y: Float) {
        paint.color = pen.color
        paint.strokeWidth = pen.size
        paint.style = Paint.Style.STROKE
        currentPath.lineTo(x, y)
        canvas.drawPath(currentPath, paint)
    }

    /**
     * returns the global bitmap
     */
    fun getBitmap(): Bitmap {
        return currentDrawingBitmap.value ?: _bitmap
    }


    /**
     * sets the pen color to passed in color
     */
    fun setPenColor(color: Int) {
        pen.color = color
        _penLiveData.value = pen
    }

    /**
     * sets the pen size to passed in size
     */
    fun setPenSize(size: Float) {
        pen.size = size
        _penLiveData.value = pen
    }

    /**
     * sets the pen shape to passed in shape
     */
    fun setPenShape(shape: Shape) {
        pen.shape = shape
        _penLiveData.value = pen
    }

    /**
     * returns true or false on if pen shape is line
     */
    fun isLine(): Boolean {
        return pen.shape == Shape.LINE
    }

}