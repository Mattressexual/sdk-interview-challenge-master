package com.pison.pisonsdkandroidskeleton

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

private const val STROKE_WIDTH = 12f

class CanvasView(context: Context) : View(context) {

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val backgroundColor = this.context.getColor(R.color.black)
    private val drawColor = this.context.getColor(R.color.white)

    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = STROKE_WIDTH
    }


    // Sets the color being drawn with
    fun setPaintColor(color: Int) {
        paint.color = color
    }

    fun drawPoint(x: Float, y: Float) {
        extraCanvas.drawPoint(x, y, paint)
    }

    fun drawLine(oldX: Float, oldY: Float, x: Float, y: Float) {
        extraCanvas.drawLine(oldX, oldY, x, y, paint)
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }
}