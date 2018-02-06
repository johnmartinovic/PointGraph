package com.johnnym.pointgraph.utils

import android.graphics.Rect
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

fun Rect.setMiddle(x: Int, y: Int) {
    this.setXMiddle(x)
    this.setYMiddle(y)
}

fun Rect.setXMiddle(x: Int) {
    this.offsetTo(x - this.width() / 2, this.top)
}

fun Rect.setYMiddle(y: Int) {
    this.offsetTo(this.left, y - this.height() / 2)
}

fun Rect.getXPosition(): Int {
    return this.centerX()
}