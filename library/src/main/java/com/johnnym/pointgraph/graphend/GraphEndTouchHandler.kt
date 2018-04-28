package com.johnnym.pointgraph.graphend

import android.view.MotionEvent

class GraphEndTouchHandler(
        private val listener: Listener) {

    interface Listener {

        fun isInSelector(x: Float, y: Float): Boolean

        fun selectorChanged(xPosition: Float)

        fun onSelectorPressed()

        fun onSelectorReleased()
    }

    private var actionDownXValue: Float = 0f
    private var actionDownYValue: Float = 0f
    private var actionMoveXValue: Float = 0f
    private var selectorSelected: Boolean = false

    fun handleTouchEvent(event: MotionEvent) {
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                actionDownXValue = event.x
                actionDownYValue = event.y

                val selectorTouchFieldContainsTouch = listener.isInSelector(
                        actionDownXValue, actionDownYValue)

                if (selectorTouchFieldContainsTouch) {
                    selectorSelected = true
                    listener.onSelectorPressed()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (selectorSelected) {
                    selectorSelected = false
                    listener.onSelectorReleased()
                }
            }
        }

        val newXPosition = event.x
        if (selectorSelected) listener.selectorChanged(newXPosition)
    }

    fun isSelectorSelected() = selectorSelected
}