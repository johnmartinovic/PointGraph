package com.johnnym.pointgraph.utils

const val S_TO_MS_FACTOR: Long = 1000

fun affineTransformXToY(
        x: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
): Float = (maxY - minY) / (maxX - minX) * (x - maxX) + maxY