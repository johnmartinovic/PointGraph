package com.johnnym.lagrange.utils

import android.content.Context
import android.graphics.RectF
import android.util.TypedValue

fun convertDpToPixel(dp: Float, context: Context): Float {
    val resources = context.resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}

fun RectF.setMiddle(x: Float, y: Float) {
    this.setXMiddle(x)
    this.setYMiddle(y)
}

fun RectF.setXMiddle(x: Float) {
    this.offsetTo(x - this.width() / 2, this.top)
}

fun RectF.setYMiddle(y: Float) {
    this.offsetTo(this.left, y - this.height() / 2)
}

fun RectF.getXPosition(): Float {
    return this.centerX()
}