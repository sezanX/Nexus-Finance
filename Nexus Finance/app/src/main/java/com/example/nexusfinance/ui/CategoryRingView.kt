package com.example.nexusfinance.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.nexusfinance.model.CategoryTotal

class CategoryRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val colors = listOf(
        Color.parseColor("#E53935"), // Red
        Color.parseColor("#F4511E"), // Deep Orange
        Color.parseColor("#FB8C00"), // Orange
        Color.parseColor("#FFB300"), // Amber
        Color.parseColor("#43A047"), // Green
        Color.parseColor("#1E88E5"), // Blue
        Color.parseColor("#3949AB"), // Indigo
        Color.parseColor("#8E24AA")  // Purple
    )

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
        strokeCap = Paint.Cap.ROUND
    }

    private var categories: List<CategoryTotal> = emptyList()

    fun setData(data: List<CategoryTotal>) {
        categories = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (categories.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val size = minOf(width, height)
        val padding = ringPaint.strokeWidth / 2f
        
        // Center the ring
        val left = (width - size) / 2f + padding
        val top = (height - size) / 2f + padding
        val right = left + size - 2 * padding
        val bottom = top + size - 2 * padding
        val rect = RectF(left, top, right, bottom)

        var startAngle = -90f
        
        categories.forEachIndexed { index, cat ->
            ringPaint.color = colors[index % colors.size]
            val sweepAngle = (cat.percentOfTotal / 100f) * 360f
            val gap = if (categories.size > 1 && sweepAngle > 5f) 4f else 0f
            
            canvas.drawArc(rect, startAngle, sweepAngle - gap, false, ringPaint)
            startAngle += sweepAngle
        }
    }
}
