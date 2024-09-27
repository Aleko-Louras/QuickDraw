package com.example.quckdraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // 用于存储绘制路径的 Path 对象
    private var path = Path()

    // 创建一个 Paint 对象用于绘制蓝色线条
    private val paint = Paint().apply {
        color = Color.BLUE
        isAntiAlias = true
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制当前的路径
        canvas.drawPath(path, paint)
    }

    // 处理触摸事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 当用户按下时，移动路径的起点到当前触摸点
                path.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                // 当用户移动时，绘制线条到当前触摸点
                path.lineTo(x, y)
                invalidate() // 刷新视图，触发 onDraw() 重新绘制
            }
            MotionEvent.ACTION_UP -> {
                // 当用户抬起时，不需要特别处理
            }
        }

        return true
    }
}
