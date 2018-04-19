package com.johnnym.pointgraph.graphend

import android.graphics.Paint

data class GraphEndPaints(
        val xAxisRectPaint: Paint,
        val selectorPaint: Paint,
        val selectedLinePaint: Paint,
        val graphPaint: Paint,
        val selectedGraphPaint: Paint) {

    companion object {

        fun create(attributes: GraphEndAttrs): GraphEndPaints =
                GraphEndPaints(
                        Paint().apply {
                            isAntiAlias = true
                            color = attributes.xAxisColor
                            style = Paint.Style.FILL
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