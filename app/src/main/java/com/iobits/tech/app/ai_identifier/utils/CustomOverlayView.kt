package com.iobits.tech.app.ai_identifier.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import com.iobits.tech.app.ai_identifier.R

class CustomOverlayView(context: Context, private val boundingBox: Rect) : View(context) {

    private val paint: Paint = Paint()

    init {
        paint.color = ContextCompat.getColor(context, R.color.red)
        paint.style = Paint.Style.STROKE // Outline only
        paint.strokeWidth = 12f // Thickness of the line
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Define corner lengths
        val cornerLength = 100f

        // Draw top-left corner
        canvas.drawLine(boundingBox.left.toFloat(), boundingBox.top.toFloat(), boundingBox.left + cornerLength, boundingBox.top.toFloat(), paint)
        canvas.drawLine(boundingBox.left.toFloat(), boundingBox.top.toFloat(), boundingBox.left.toFloat(), boundingBox.top + cornerLength, paint)

        // Draw top-right corner
        canvas.drawLine(boundingBox.right.toFloat(), boundingBox.top.toFloat(), boundingBox.right - cornerLength, boundingBox.top.toFloat(), paint)
        canvas.drawLine(boundingBox.right.toFloat(), boundingBox.top.toFloat(), boundingBox.right.toFloat(), boundingBox.top + cornerLength, paint)

        // Draw bottom-left corner
        canvas.drawLine(boundingBox.left.toFloat(), boundingBox.bottom.toFloat(), boundingBox.left + cornerLength, boundingBox.bottom.toFloat(), paint)
        canvas.drawLine(boundingBox.left.toFloat(), boundingBox.bottom.toFloat(), boundingBox.left.toFloat(), boundingBox.bottom - cornerLength, paint)

        // Draw bottom-right corner
        canvas.drawLine(boundingBox.right.toFloat(), boundingBox.bottom.toFloat(), boundingBox.right - cornerLength, boundingBox.bottom.toFloat(), paint)
        canvas.drawLine(boundingBox.right.toFloat(), boundingBox.bottom.toFloat(), boundingBox.right.toFloat(), boundingBox.bottom - cornerLength, paint)
    }
}