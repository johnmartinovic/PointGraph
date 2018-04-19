package com.johnnym.pointgraph.utils

const val S_TO_MS_FACTOR: Long = 1000

const val GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE: Float = 0f
const val GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE: Float = 1f

fun affineTransformXToY(
        x: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
): Float = (maxY - minY) / (maxX - minX) * (x - maxX) + maxY