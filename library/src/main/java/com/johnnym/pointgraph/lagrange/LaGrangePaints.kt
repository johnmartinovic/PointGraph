package com.johnnym.pointgraph.lagrange

import android.graphics.Paint
import android.text.TextPaint

class LaGrangePaints(attributes: LaGrangeAttrs) {

    val xAxisRectPaint = Paint().apply {
        isAntiAlias = true
        color = attributes.xAxisColor
        style = Paint.Style.FILL
    }
    val textPaint = TextPaint().apply {
        isAntiAlias = true
        color = attributes.textColor
        textAlign = Paint.Align.CENTER
        textSize = attributes.textSize
    }
    val selectorPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = attributes.selectorColor
    }
    val selectedLinePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = attributes.selectedLineColor
    }
    val graphPaint = Paint().apply {
        isAntiAlias = true
        color = attributes.graphColor
        style = Paint.Style.FILL
    }
    val selectedGraphPaint = Paint().apply {
        isAntiAlias = true
        color = attributes.selectedGraphColor
        style = Paint.Style.FILL
    }
}