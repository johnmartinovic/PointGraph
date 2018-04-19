package com.johnnym.pointgraph.graphend

data class GraphEndDimensions(
        private val attributes: GraphEndAttrs) {

    var graphTop: Float = 0f
        private set
    var graphBottom: Float = 0f
        private set
    var graphLeft: Float = 0f
        private set
    var graphRight: Float = 0f
        private set

    fun updateDimensions(viewStartX: Int, viewEndX: Int, viewStartY: Int, viewEndY: Int) {
        graphTop = viewStartY + attributes.graphTopFromTop
        graphBottom = viewEndY - attributes.graphBottomFromBottom
        graphLeft = viewStartX + attributes.graphLeftRightPadding
        graphRight = viewEndX - attributes.graphLeftRightPadding
    }
}