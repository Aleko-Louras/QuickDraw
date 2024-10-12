package com.example.quckdraw

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private lateinit var drawingViewModel: DrawingViewModel

    fun setViewModel(viewModel: DrawingViewModel) {
        this.drawingViewModel = viewModel
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(drawingViewModel.getBitmap(), 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (drawingViewModel.isLine()) {
                    drawingViewModel.startPath(x, y)
                } else {
                    drawingViewModel.startShape(x, y)
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (drawingViewModel.isLine()) {
                    drawingViewModel.addToPath(x, y)
                } else {
                    drawingViewModel.updateShape(x, y)
                }
                invalidate()
            }
        }
        return true
    }

}
