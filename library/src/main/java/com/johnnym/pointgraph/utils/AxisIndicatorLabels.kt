package com.johnnym.pointgraph.utils

import android.graphics.Canvas
import android.text.TextPaint

class AxisIndicatorLabels(
        numberOfMiddleLabels: Int,
        private val textPaint: TextPaint) {

    private val numbers: FloatArray
    private val numberOfLabels = numberOfMiddleLabels + 2
    private val labelsXPositions: FloatArray

    private var labelsYPosition: Float = 0f

    init {
        labelsXPositions = FloatArray(numberOfLabels)
        numbers = FloatArray(numberOfLabels)
    }

    fun updateNumbers(minValue: Float, maxValue: Float) {
        val range = maxValue - minValue
        for (i in numbers.indices) {
            numbers[i] = i * range / (numbers.size - 1) + minValue
        }
    }

    fun setLimitValues(labelsXStartPosition: Float, labelsXEndPosition: Float, labelsYPosition: Float) {
        val labelsDistance = (labelsXEndPosition - labelsXStartPosition) / (numberOfLabels - 1)
        for (i in 0 until numberOfLabels) {
            labelsXPositions[i] = labelsXStartPosition + i * labelsDistance
        }

        this.labelsYPosition = labelsYPosition
    }

    fun draw(canvas: Canvas) {
        labelsXPositions.forEachIndexed { index, labelXPosition ->
            canvas.drawText(
                    String.format("%.0f", numbers[index]),
                    labelXPosition,
                    labelsYPosition,
                    textPaint)
        }
    }
}