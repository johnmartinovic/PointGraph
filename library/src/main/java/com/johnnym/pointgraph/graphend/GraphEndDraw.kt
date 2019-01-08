package com.johnnym.pointgraph.graphend

import android.graphics.Canvas
import android.graphics.RectF
import com.johnnym.pointgraph.GraphPath
import com.johnnym.pointgraph.PointsData
import com.johnnym.pointgraph.utils.*

data class GraphEndDraw(
        private val attributes: GraphEndAttrs,
        private val dimensions: GraphEndDimensions) {

    val paints: GraphEndPaints = GraphEndPaints.create(attributes)

    private val selector = RectF(
            0f,
            0f,
            attributes.selector.intrinsicWidth.toFloat(),
            attributes.selector.intrinsicHeight.toFloat())
    private val xAxisRect = RectF(0f, 0f, 0f, attributes.xAxisThickness)
    private val selectedLine = RectF(
            0f,
            0f,
            0f,
            attributes.selectedLineThickness)
    private val graphPath = GraphPath()
    private val graphBoundsRect = RectF()
    private val selectedGraphBoundsRect = RectF()

    fun refreshStaticObjects() {
        xAxisRect.left = dimensions.graphLeft - attributes.xAxisThickness / 2
        xAxisRect.right = dimensions.graphRight + attributes.xAxisThickness / 2
        xAxisRect.setYMiddle(dimensions.graphBottom)

        graphBoundsRect.set(dimensions.graphLeft, dimensions.graphTop, dimensions.graphRight, dimensions.graphBottom)
        selectedGraphBoundsRect.apply {
            left = dimensions.graphLeft
            top = dimensions.graphTop
            bottom = dimensions.graphBottom
        }

        selectedLine.left = dimensions.graphLeft
        selectedLine.setYMiddle(dimensions.graphBottom)

        selector.setYMiddle(dimensions.graphBottom)
    }

    fun refreshDataObjects(pointsData: PointsData) {
        graphPath.generatePath(pointsData, dimensions.graphLeft, dimensions.graphBottom, dimensions.graphRight, dimensions.graphTop)
    }

    fun updateSelectorDependantShapes(selectorX: Float) {
        selectedGraphBoundsRect.right = selectorX
        selectedLine.right = selectorX
        selector.setXMiddle(selectorX)
    }

    fun getSelectorXPosition(): Float = selector.getXPosition()

    fun isInSelector(x: Float, y: Float): Boolean =
            selector.contains(x, y)

    fun drawWithData(canvas: Canvas, graphYAxisScaleFactor: Float) {
        drawGraph(canvas, graphYAxisScaleFactor)
        drawXAxis(canvas)
        drawSelectedLineAndSelector(canvas)
    }

    fun drawWithoutData(canvas: Canvas) {
        drawXAxis(canvas)
    }

    private fun drawGraph(canvas: Canvas, graphYAxisScaleFactor: Float) {
        // draw graph and selected part of graph
        canvas.save()
        canvas.scale(1f, graphYAxisScaleFactor, 0f, dimensions.graphBottom)
        canvas.clipRect(graphBoundsRect)
        canvas.drawPath(graphPath, paints.graphPaint)
        canvas.clipRect(selectedGraphBoundsRect)
        canvas.drawPath(graphPath, paints.selectedGraphPaint)
        canvas.restore()
    }

    private fun drawXAxis(canvas: Canvas) {
        canvas.drawRoundRect(xAxisRect, attributes.xAxisThickness, attributes.xAxisThickness, paints.xAxisRectPaint)
    }

    private fun drawSelectedLineAndSelector(canvas: Canvas) {
        canvas.drawRect(selectedLine, paints.selectedLinePaint)
        attributes.selector.bounds = selector.toRect()
        attributes.selector.draw(canvas)
    }
}