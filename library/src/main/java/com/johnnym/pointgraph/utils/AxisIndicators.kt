package com.johnnym.pointgraph.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable

class AxisIndicators(
        numberOfMiddleIndicators: Int,
        private val axisIndicatorDrawable: Drawable) {

    private val numberOfIndicators = numberOfMiddleIndicators + 2
    private val xAxisIndicatorsRects: List<Rect>
    private val indicatorsXPositions: FloatArray

    init {
        val xAxisIndicatorRect = Rect(
                0,
                0,
                axisIndicatorDrawable.intrinsicWidth,
                axisIndicatorDrawable.intrinsicHeight)
        xAxisIndicatorsRects = List(numberOfIndicators) { Rect(xAxisIndicatorRect) }
        indicatorsXPositions = FloatArray(numberOfIndicators)
    }

    fun setLimitValues(axisXStartPosition: Float, axisXEndPosition: Float, axisYPosition: Float) {
        val pointsDistance = (axisXEndPosition - axisXStartPosition) / (numberOfIndicators - 1)

        var indicatorXPosition: Float
        for (i in 0 until indicatorsXPositions.size) {
            indicatorXPosition = axisXStartPosition + i * pointsDistance
            indicatorsXPositions[i] = indicatorXPosition
            xAxisIndicatorsRects[i].setXMiddle(indicatorXPosition.toInt())
            xAxisIndicatorsRects[i].setYMiddle(axisYPosition.toInt())
        }
    }

    fun draw(canvas: Canvas) {
        for (xAxisIndicatorsRect in xAxisIndicatorsRects) {
            axisIndicatorDrawable.bounds = xAxisIndicatorsRect
            axisIndicatorDrawable.draw(canvas)
        }
    }
}