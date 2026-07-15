package com.example.nexusfinance.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.nexusfinance.model.CashflowPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CashflowTrendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val expenseLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val incomeLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50") // Green
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun setLineColor(color: Int) {
        expenseLinePaint.color = color
        invalidate()
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EAEAEA")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAAAA")
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935")
        style = Paint.Style.FILL
    }
    
    private val dotShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55E53935")
        style = Paint.Style.FILL
    }

    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E53935")
        style = Paint.Style.FILL
    }

    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
    
    private val incomeDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }
    
    private val incomeDotShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#554CAF50")
        style = Paint.Style.FILL
    }
    
    private val incomeTooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private var points: List<CashflowPoint> = emptyList()

    fun setData(data: List<CashflowPoint>) {
        points = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val paddingHorizontal = 40f
        val paddingVerticalTop = 80f // space for tooltip
        val paddingVerticalBottom = 80f // space for labels
        
        val drawHeight = height - paddingVerticalTop - paddingVerticalBottom

        // Draw horizontal grid lines (4 lines)
        val gridCount = 4
        for (i in 0 until gridCount) {
            val y = paddingVerticalTop + (i * drawHeight / (gridCount - 1))
            canvas.drawLine(paddingHorizontal, y, width - paddingHorizontal, y, gridPaint)
        }

        val maxExpense = points.maxOfOrNull { it.expense } ?: 0.0
        val maxIncome = points.maxOfOrNull { it.income } ?: 0.0
        val maxAmount = maxOf(maxExpense, maxIncome, 1.0)
        val maxScale = maxAmount

        val stepX = (width - 2 * paddingHorizontal) / (if (points.size > 1) points.size - 1 else 1)
        val expensePath = Path()
        val incomePath = Path()

        var prevExpenseX = paddingHorizontal
        var prevExpenseY = paddingVerticalTop + drawHeight - ((points.firstOrNull()?.expense ?: 0.0) / maxScale * drawHeight).toFloat()
        
        var prevIncomeX = paddingHorizontal
        var prevIncomeY = paddingVerticalTop + drawHeight - ((points.firstOrNull()?.income ?: 0.0) / maxScale * drawHeight).toFloat()
        
        expensePath.moveTo(prevExpenseX, prevExpenseY)
        incomePath.moveTo(prevIncomeX, prevIncomeY)

        for (i in 0 until points.size) {
            val point = points[i]
            val x = paddingHorizontal + i * stepX
            val expenseY = paddingVerticalTop + drawHeight - ((point.expense / maxScale) * drawHeight).toFloat()
            val incomeY = paddingVerticalTop + drawHeight - ((point.income / maxScale) * drawHeight).toFloat()

            if (i > 0) {
                val cp1X = prevExpenseX + (x - prevExpenseX) / 2
                val cp2X = cp1X
                
                expensePath.cubicTo(cp1X, prevExpenseY, cp2X, expenseY, x, expenseY)
                incomePath.cubicTo(cp1X, prevIncomeY, cp2X, incomeY, x, incomeY)
            }
            
            // Draw x-axis labels
            val dayStr = point.monthLabel
            canvas.drawText(dayStr, x, height - 20f, textPaint)
            
            prevExpenseX = x
            prevExpenseY = expenseY
            prevIncomeX = x
            prevIncomeY = incomeY
        }
        
        canvas.drawPath(expensePath, expenseLinePaint)
        canvas.drawPath(incomePath, incomeLinePaint)

        // Draw dot and tooltip on last point
        val lastPoint = points.last()
        val lastX = paddingHorizontal + (points.size - 1) * stepX
        val lastExpenseY = paddingVerticalTop + drawHeight - ((lastPoint.expense / maxScale) * drawHeight).toFloat()
        val lastIncomeY = paddingVerticalTop + drawHeight - ((lastPoint.income / maxScale) * drawHeight).toFloat()
        
        // Draw income dot and tooltip
        canvas.drawCircle(lastX, lastIncomeY, 24f, incomeDotShadowPaint)
        canvas.drawCircle(lastX, lastIncomeY, 12f, incomeDotPaint)
        
        val incomeTooltipText = "৳${lastPoint.income.toInt()}"
        val incomeTextBounds = Rect()
        tooltipTextPaint.getTextBounds(incomeTooltipText, 0, incomeTooltipText.length, incomeTextBounds)
        
        val incomeTooltipWidth = incomeTextBounds.width() + 40f
        val incomeTooltipHeight = incomeTextBounds.height() + 20f
        val incomeTooltipRect = RectF(
            lastX - incomeTooltipWidth - 20f, // offset to left
            lastIncomeY - incomeTooltipHeight / 2 - 24f,
            lastX - 20f,
            lastIncomeY + incomeTooltipHeight / 2 - 24f
        )
        
        canvas.drawRoundRect(incomeTooltipRect, 16f, 16f, incomeTooltipPaint)
        canvas.drawText(incomeTooltipText, incomeTooltipRect.centerX(), incomeTooltipRect.centerY() - (tooltipTextPaint.ascent() + tooltipTextPaint.descent()) / 2, tooltipTextPaint)

        // Draw expense dot and tooltip
        canvas.drawCircle(lastX, lastExpenseY, 24f, dotShadowPaint)
        canvas.drawCircle(lastX, lastExpenseY, 12f, dotPaint)
        
        val expenseTooltipText = "৳${lastPoint.expense.toInt()}"
        val textBounds = Rect()
        tooltipTextPaint.getTextBounds(expenseTooltipText, 0, expenseTooltipText.length, textBounds)
        
        val tooltipWidth = textBounds.width() + 40f
        val tooltipHeight = textBounds.height() + 20f
        val tooltipRect = RectF(
            lastX - tooltipWidth / 2,
            lastExpenseY - 24f - tooltipHeight,
            lastX + tooltipWidth / 2,
            lastExpenseY - 24f
        )
        
        canvas.drawRoundRect(tooltipRect, 16f, 16f, tooltipPaint)
        canvas.drawText(expenseTooltipText, lastX, lastExpenseY - 24f - (tooltipHeight - textBounds.height())/2f + 4f, tooltipTextPaint)
    }
}
