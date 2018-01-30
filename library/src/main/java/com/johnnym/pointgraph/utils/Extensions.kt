package com.johnnym.pointgraph.utils

import android.graphics.RectF

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