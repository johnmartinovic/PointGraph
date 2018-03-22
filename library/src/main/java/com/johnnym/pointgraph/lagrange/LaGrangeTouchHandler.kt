package com.johnnym.pointgraph.lagrange

import android.view.MotionEvent
import android.view.View
import com.johnnym.pointgraph.utils.getXPosition

class LaGrangeTouchHandler(
        private val view: View,
        private val dimensions: LaGrangeDimensions,
        private val drawObjects: LaGrangeDraw,
        private val listener: Listener) {

    interface Listener {

        fun minSelectorChanged()

        fun maxSelectorChanged()
    }

    private var actionDownXValue: Float = 0f
    private var actionDownYValue: Float = 0f
    private var actionMoveXValue: Float = 0f
    private var minSelectorSelected: Boolean = false
    private var maxSelectorSelected: Boolean = false
    private var bothSelectorsSelected: Boolean = false

    fun handleTouchEvent(event: MotionEvent) {
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownXValue = event.x
                actionDownYValue = event.y

                val minSelectorTouchFieldContainsTouch = drawObjects.isInMinSelectorTouchField(actionDownXValue, actionDownYValue)
                val maxSelectorTouchFieldContainsTouch = drawObjects.isInMaxSelectorTouchField(actionDownXValue, actionDownYValue)

                if (minSelectorTouchFieldContainsTouch && maxSelectorTouchFieldContainsTouch) {
                    bothSelectorsSelected = true
                } else if (minSelectorTouchFieldContainsTouch) {
                    minSelectorSelected = true
                } else if (maxSelectorTouchFieldContainsTouch) {
                    maxSelectorSelected = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                actionMoveXValue = event.x
                if (bothSelectorsSelected) {
                    if (actionMoveXValue < actionDownXValue) {
                        bothSelectorsSelected = false
                        minSelectorSelected = true
                    } else if (actionMoveXValue > actionDownXValue) {
                        bothSelectorsSelected = false
                        maxSelectorSelected = true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                bothSelectorsSelected = false
                minSelectorSelected = false
                maxSelectorSelected = false
            }
        }

        var newXPosition = event.x
        if (minSelectorSelected) {
            if (newXPosition < dimensions.graphLeft) {
                newXPosition = dimensions.graphLeft
            } else if (newXPosition > drawObjects.getMaxSelectorXPosition()) {
                newXPosition = drawObjects.getMaxSelectorXPosition()
            }
            drawObjects.setMinSelectorXPosition(newXPosition)
            listener.minSelectorChanged()
        } else if (maxSelectorSelected) {
            if (newXPosition > dimensions.graphRight) {
                newXPosition = dimensions.graphRight
            } else if (newXPosition < drawObjects.getMinSelectorXPosition()) {
                newXPosition = drawObjects.getMinSelectorXPosition()
            }
            drawObjects.setMaxSelectorXPosition(newXPosition)
            listener.maxSelectorChanged()
        }

        // If any of the selectors is selected, then user must be able to move his finger anywhere
        // on the screen and still have control of the selected selector.
        if (bothSelectorsSelected || minSelectorSelected || maxSelectorSelected) {
            view.parent.requestDisallowInterceptTouchEvent(true)
        }
    }

    fun isAnySelectorSelected() = minSelectorSelected || maxSelectorSelected
}