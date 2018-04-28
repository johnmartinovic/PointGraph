package com.johnnym.pointgraph.utils

import kotlin.math.max
import kotlin.math.min

const val S_TO_MS_FACTOR: Long = 1000

fun affineTransformXToY(
        x: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
): Float = (maxY - minY) / (maxX - minX) * (x - maxX) + maxY

fun constrainToRange(value: Float, minValue: Float, maxValue: Float): Float {
    assert(minValue <= maxValue)
    return min(max(value, minValue), maxValue)
}