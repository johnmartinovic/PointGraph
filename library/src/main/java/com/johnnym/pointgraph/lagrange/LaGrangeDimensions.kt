package com.johnnym.pointgraph.lagrange

data class LaGrangeDimensions(
        private val attributes: LaGrangeAttrs) {

    var graphTop: Float = 0f
        private set
    var graphBottom: Float = 0f
        private set
    var graphLeft: Float = 0f
        private set
    var graphRight: Float = 0f
        private set
    var numbersYPosition: Float = 0f
        private set

    fun update(viewStartX: Int, viewEndX: Int, viewStartY: Int, viewEndY: Int) {
        graphTop = viewStartY + attributes.graphTopFromTop
        graphBottom = viewEndY - attributes.graphBottomFromBottom
        graphLeft = viewStartX + attributes.graphLeftRightPadding
        graphRight = viewEndX - attributes.graphLeftRightPadding

        numbersYPosition = viewEndY - attributes.numbersYPositionFromBottom
    }
}