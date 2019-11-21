package com.example.opencvkotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class DrawLineView(context: Context, var fromX: Float, var fromY: Float, var duX: Float, var duY: Float) :
    View(context) {

    var paint: Paint = Paint()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 20f
        paint.color = Color.RED
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas!!.drawLine(fromX, fromY, duX, duY, paint)
    }


}