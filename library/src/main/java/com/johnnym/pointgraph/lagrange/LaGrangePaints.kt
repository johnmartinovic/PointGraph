package com.johnnym.pointgraph.lagrange

import android.graphics.Paint
import android.text.TextPaint

data class LaGrangePaints(
        val xAxisRectPaint: Paint,
        val textPaint: TextPaint,
        val selectorPaint: Paint,
        val selectedLinePaint: Paint,
        val graphPaint: Paint,
        val selectedGraphPaint: Paint) {

    companion object {

        fun create(attributes: LaGrangeAttrs): LaGrangePaints =
                LaGrangePaints(
                        Paint().apply {
                            isAntiAlias = true
                            color = attributes.xAxisColor
                            style = Paint.Style.FILL
                        },
                        TextPaint().apply {
                            isAntiAlias = true
                            color = attributes.textColor
                            textAlign = Paint.Align.CENTER
                            textSize = attributes.textSize
                        },
                        Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.FILL
                            color = attributes.selectorColor
                        },
                        Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.FILL
                            color = attributes.selectedLineColor
                        },
                        Paint().apply {
                            isAntiAlias = true
                            color = attributes.graphColor
                            style = Paint.Style.FILL
                        },
                        Paint().apply {
                            isAntiAlias = true
                            color = attributes.selectedGraphColor
                            style = Paint.Style.FILL
                        })
    }
}