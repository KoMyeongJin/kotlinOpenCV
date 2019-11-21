package com.example.opencvkotlin

import android.content.Context
import android.util.AttributeSet
import androidx.drawerlayout.widget.DrawerLayout

class DrawView : DrawerLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle : Int) : super(context, attrs, defStyle)

    override fun onMeasure( widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasure = MeasureSpec.makeMeasureSpec(
            MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY)
        val heightMeasure = MeasureSpec.makeMeasureSpec(
            MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasure, heightMeasure)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}