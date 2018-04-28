package com.johnnym.pointgraph.utils

import android.animation.ValueAnimator

class GraphOps(listener: Listener) {

    interface Listener {

        fun onGraphYAxisScaleFactor()
    }

    companion object {

        const val GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE: Float = 0f
        const val GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE: Float = 1f
    }

    private val graphScaleAnimator = ValueAnimator().apply {
        duration = 300
        addUpdateListener { animation ->
            graphYAxisScaleFactor = animation.animatedValue as Float
            listener.onGraphYAxisScaleFactor()
        }
    }

    private var graphIsShown = false

    var graphYAxisScaleFactor = GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE
        private set

    fun toggleGraphVisibility(animated: Boolean) {
        if (graphIsShown) {
            hideGraph(animated)
        } else {
            showGraph(animated)
        }
    }

    fun showGraph(animated: Boolean) {
        if (animated) {
            graphScaleAnimator.cancel()
            graphScaleAnimator.setFloatValues(graphYAxisScaleFactor, GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE)
            graphScaleAnimator.start()
        } else {
            graphYAxisScaleFactor = GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE
        }

        graphIsShown = true
    }

    fun hideGraph(animated: Boolean) {
        if (animated) {
            graphScaleAnimator.cancel()
            graphScaleAnimator.setFloatValues(graphYAxisScaleFactor, GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE)
            graphScaleAnimator.start()
        } else {
            graphYAxisScaleFactor = GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE
        }

        graphIsShown = false
    }
}