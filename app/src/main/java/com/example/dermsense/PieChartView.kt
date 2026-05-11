package com.example.dermsense

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    data class Slice(val value: Float, val color: Int, val label: String)

    private var slices: List<Slice> = emptyList()
    private var total: Float = 0f

    fun setData(low: Int, mid: Int, high: Int) {
        val l = low.toFloat(); val m = mid.toFloat(); val h = high.toFloat()
        total = l + m + h
        slices = if (total == 0f) {
            listOf(Slice(1f, Color.parseColor("#2A2A4A"), "Veri Yok"))
        } else {
            buildList {
                if (h > 0) add(Slice(h, Color.parseColor("#FF4444"), "Yüksek"))
                if (m > 0) add(Slice(m, Color.parseColor("#FFA500"), "Orta"))
                if (l > 0) add(Slice(l, Color.parseColor("#44BB44"), "Düşük"))
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        val radius = (minOf(w, h) / 2f) * 0.75f
        val cx = w / 2f; val cy = h / 2f
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        var startAngle = -90f
        for (slice in slices) {
            val sweep = if (total == 0f) 360f else (slice.value / total) * 360f
            paint.color = slice.color
            paint.style = Paint.Style.FILL
            canvas.drawArc(oval, startAngle, sweep, true, paint)

            // Kenarlık
            paint.color = Color.parseColor("#12122A")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawArc(oval, startAngle, sweep, true, paint)

            startAngle += sweep
        }

        // Ortada delik (donut efekti)
        paint.color = Color.parseColor("#12122A")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius * 0.5f, paint)

        // Ortada toplam sayı
        if (total > 0f) {
            textPaint.textSize = radius * 0.35f
            canvas.drawText(total.toInt().toString(), cx, cy + textPaint.textSize * 0.35f, textPaint)
            textPaint.textSize = radius * 0.18f
            textPaint.setColor(Color.parseColor("#7B7B9A"))
            canvas.drawText("tarama", cx, cy + textPaint.textSize * 3.5f, textPaint)
            textPaint.setColor(Color.WHITE)
        }
    }
}