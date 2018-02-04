package com.johnnym.pointgraph.utils

fun affineTransformXToY(
        x: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
): Float = (maxY - minY) / (maxX - minX) * (x - maxX) + maxY