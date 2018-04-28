package com.johnnym.pointgraph.lagrange

import android.graphics.Canvas
import android.graphics.RectF
import com.johnnym.pointgraph.GraphPath
import com.johnnym.pointgraph.PointsData
import com.johnnym.pointgraph.utils.*

data class LaGrangeDraw(
        private val attributes: LaGrangeAttrs,
        private val dimensions: LaGrangeDimensions) {

    private val paints: LaGrangePaints = LaGrangePaints.create(attributes)

    private val minSelector = RectF(
            0f,
            0f,
            attributes.selector.intrinsicWidth.toFloat(),
            attributes.selector.intrinsicHeight.toFloat())
    private val maxSelector = RectF(minSelector)
    private val xAxisRect = RectF(0f, 0f, 0f, attributes.xAxisThickness)
    private val xAxisIndicators = AxisIndicators(
            attributes.xAxisNumberOfMiddlePoints,
            attributes.xAxisIndicatorDrawable)
    private val xAxisIndicatorLabels = AxisIndicatorLabels(
            attributes.xAxisNumberOfMiddlePoints,
            paints.textPaint)
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

        // Calculate X axis number positions
        xAxisIndicators.setLimitValues(
                dimensions.graphLeft,
                dimensions.graphRight,
                dimensions.graphBottom)
        xAxisIndicatorLabels.setLimitValues(
                dimensions.graphLeft,
                dimensions.graphRight,
                dimensions.numbersYPosition)

        graphBoundsRect.set(
                dimensions.graphLeft,
                dimensions.graphTop,
                dimensions.graphRight,
                dimensions.graphBottom)
        selectedGraphBoundsRect.set(graphBoundsRect)

        selectedLine.left = selectedGraphBoundsRect.left
        selectedLine.right = selectedGraphBoundsRect.right

        minSelector.setYMiddle(dimensions.graphBottom)
        maxSelector.setYMiddle(dimensions.graphBottom)
        selectedLine.setYMiddle(dimensions.graphBottom)
    }

    fun refreshDataObjects(pointsData: PointsData) {
        xAxisIndicatorLabels.updateNumbers(pointsData.minX, pointsData.maxX)
        graphPath.generatePath(pointsData, dimensions.graphLeft, dimensions.graphBottom, dimensions.graphRight, dimensions.graphTop)
    }

    fun updateMinSelectorDependantShapes(minSelectorX: Float) {
        selectedGraphBoundsRect.left = minSelectorX
        selectedLine.left = minSelectorX
        minSelector.setXMiddle(minSelectorX)
    }

    fun updateMaxSelectorDependantShapes(maxSelectorX: Float) {
        selectedGraphBoundsRect.right = maxSelectorX
        selectedLine.right = maxSelectorX
        maxSelector.setXMiddle(maxSelectorX)
    }

    fun getMinSelectorXPosition(): Float = minSelector.getXPosition()

    fun getMaxSelectorXPosition(): Float = maxSelector.getXPosition()

    fun isInMinSelector(x: Float, y: Float): Boolean =
            minSelector.contains(x, y)

    fun isInMaxSelector(x: Float, y: Float): Boolean =
            maxSelector.contains(x, y)

    fun drawWithData(canvas: Canvas, graphYAxisScaleFactor: Float) {
        drawGraph(canvas, graphYAxisScaleFactor)
        drawXAxisAndIndicators(canvas)
        drawSelectedLineAndSelectors(canvas)
    }

    fun drawWithoutData(canvas: Canvas) {
        drawXAxisAndIndicators(canvas)
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

    private fun drawXAxisAndIndicators(canvas: Canvas) {
        canvas.drawRoundRect(xAxisRect, attributes.xAxisThickness, attributes.xAxisThickness, paints.xAxisRectPaint)
        xAxisIndicators.draw(canvas)
    }

    private fun drawSelectedLineAndSelectors(canvas: Canvas) {
        canvas.drawRect(selectedLine, paints.selectedLinePaint)
        attributes.selector.bounds = minSelector.toRect()
        attributes.selector.draw(canvas)
        attributes.selector.bounds = maxSelector.toRect()
        attributes.selector.draw(canvas)
        xAxisIndicatorLabels.draw(canvas)
    }
}