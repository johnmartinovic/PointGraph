package com.johnnym.pointgraph.utils

import android.graphics.Rect
import android.graphics.RectF
import com.johnnym.pointgraph.graphend.GraphEnd
import com.johnnym.pointgraph.lagrange.LaGrange

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

fun RectF.toRect(): Rect = Rect(
        this.left.toInt(),
        this.top.toInt(),
        this.right.toInt(),
        this.bottom.toInt())

fun ArrayList<GraphEnd.SelectorListener>.dispatchOnSelectorPressedEvent() {
    this.forEach { it.onSelectorPressed() }
}

fun ArrayList<GraphEnd.SelectorListener>.dispatchOnValueChangedEvent(newValue: Float) {
    this.forEach { it.onValueChanged(newValue) }
}

fun ArrayList<GraphEnd.SelectorListener>.dispatchOnSelectorReleasedEvent() {
    this.forEach { it.onSelectorReleased() }
}

fun ArrayList<LaGrange.SelectorsListener>.dispatchOnMinSelectorValueChangeEvent(newMinValue: Float) {
    this.forEach { it.onMinValueChanged(newMinValue) }
}

fun ArrayList<LaGrange.SelectorsListener>.dispatchOnMaxSelectorValueChangeEvent(newMaxValue: Float) {
    this.forEach { it.onMaxValueChanged(newMaxValue) }
}