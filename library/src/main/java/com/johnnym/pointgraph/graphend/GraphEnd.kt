package com.johnnym.pointgraph.graphend

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.johnnym.pointgraph.PointsData
import com.johnnym.pointgraph.utils.*
import kotlin.properties.Delegates

/**
 * View that enables the user to select a value in a range defined by the
 * [PointsData]'s minimum and maximum values, while having a graph presentation
 * of [Point]s defined in the same [PointsData] object.
 */
class GraphEnd @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val selectorAnimator = ValueAnimator().apply {
        duration = 150
        addUpdateListener { animation ->
            drawObjects.updateSelectorDependantShapes(animation.animatedValue as Float)
            invalidate()
        }
    }
    private val graphScaleAnimator = ValueAnimator().apply {
        duration = 300
        setFloatValues(GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE, GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE)
        addUpdateListener { animation ->
            graphYAxisScaleFactor = animation.animatedValue as Float
            invalidate()
        }
    }

    private var graphYAxisScaleFactor = GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE

    private val touchHandlerListener = object : GraphEndTouchHandler.Listener {

        override fun isInSelectorTouchField(x: Float, y: Float): Boolean =
                drawObjects.isInSelectorTouchField(x, y)

        override fun selectorChanged(xPosition: Float) {
            val newXPosition: Float = when {
                xPosition < dimensions.graphLeft -> dimensions.graphLeft
                xPosition > dimensions.graphRight -> dimensions.graphRight
                else -> xPosition
            }

            pointsData?.let {
                selectorValue = transformSelectorXPositionToValue(it, newXPosition)
            }

            drawObjects.updateSelectorDependantShapes(newXPosition)
            invalidate()
        }

        override fun onSelectorPressed() {
            selectorListeners.dispatchOnSelectorPressedEvent()
        }

        override fun onSelectorReleased() {
            selectorListeners.dispatchOnSelectorReleasedEvent()
        }
    }

    private val attributes: GraphEndAttrs = GraphEndAttrs.create(context, attrs, defStyleAttr)
    private val dimensions: GraphEndDimensions = GraphEndDimensions(attributes)
    private val drawObjects: GraphEndDraw = GraphEndDraw(attributes, dimensions)
    private val touchHandler: GraphEndTouchHandler = GraphEndTouchHandler(touchHandlerListener)

    private val selectorListeners = ArrayList<GraphEnd.SelectorListener>()
    private var pointsData: PointsData? = null
    private var listenersEnabled = true
    private var graphIsShown = true

    /**
     * True selector value (set from outside or by touch events)
     */
    var selectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            selectorListeners.dispatchOnValueChangedEvent(new)
        }
    }
        private set

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, drawObjects.paints.selectorPaint)
    }

    /**
     * Set [GraphEnd] graph data
     *
     * @param pointsData [Point]s to be shown in form of a graph
     * @param animated true if this set of data should be animated
     */
    fun setPointsData(pointsData: PointsData?, animated: Boolean = true) {
        this.pointsData = pointsData
        if (pointsData != null) {
            this.selectorValue = pointsData.minX
        }

        resetDataAndSelectorDrawObjects()

        if (animated) {
            showGraph()
        } else {
            invalidate()
        }
    }

    /**
     * Set [selectorValue] value
     * Value will be normalized to fit the current [PointsData] value in [pointsData].
     *
     * @param value wanted [selectorValue]
     */
    fun setSelectorValue(value: Float?, animated: Boolean) {
        // if user is interacting with the view, do not set values from outside
        if (touchHandler.isSelectorSelected()) {
            return
        }

        this.pointsData?.let { pointsData ->
            normalizeAndSetSelectorValue(pointsData, value)

            moveSelectorXPosition(
                    transformSelectorValueToXPosition(pointsData, selectorValue),
                    animated)
        }
    }

    private fun normalizeAndSetSelectorValue(pointsData: PointsData, value: Float?) {
        var selectorValue = value ?: pointsData.minX

        selectorValue = Math.max(selectorValue, pointsData.minX)
        selectorValue = Math.min(selectorValue, pointsData.maxX)

        this.selectorValue = selectorValue
    }

    /**
     * Add a listener which will be informed about
     *
     * @param selectorListener listener to be added
     */
    fun addSelectorListener(selectorListener: SelectorListener) {
        this.selectorListeners.add(selectorListener)
    }

    /**
     * Remove a listener which previously has been informed about selector interaction and
     * [selectorValue] changes.
     *
     * @param selectorListener listener to be removed
     */
    fun removeSelectorListener(selectorListener: SelectorListener) {
        this.selectorListeners.remove(selectorListener)
    }

    /**
     * Toggle the [GraphEnd] graph visibility from visible to invisible
     * and vice versa.
     */
    fun toggleGraphVisibility() {
        if (graphIsShown) {
            hideGraph()
        } else {
            showGraph()
        }
    }

    /**
     * Show [GraphEnd] graph.
     */
    fun showGraph() {
        graphScaleAnimator.cancel()
        graphScaleAnimator.setFloatValues(graphYAxisScaleFactor, GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE)
        graphScaleAnimator.start()
        graphIsShown = true
    }

    /**
     * Hide [GraphEnd] graph.
     */
    fun hideGraph() {
        graphScaleAnimator.cancel()
        graphScaleAnimator.setFloatValues(graphYAxisScaleFactor, GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE)
        graphScaleAnimator.start()
        graphIsShown = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSizeAndState(attributes.minViewWidth.toInt(), widthMeasureSpec, 0)
        val height = resolveSizeAndState(attributes.minViewHeight.toInt(), heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        dimensions.updateDimensions(
                paddingLeft,
                w - paddingRight,
                paddingTop,
                h - paddingBottom)
        drawObjects.refreshStaticObjects()
        resetDataAndSelectorDrawObjects()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.pointsData
                ?.let {
                    touchHandler.handleTouchEvent(event)

                    // If selector is selected, then user must be able to move his finger anywhere
                    // on the screen and still have control of the selected selector.
                    if (touchHandler.isSelectorSelected()) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    return true
                }
                ?: return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        this.pointsData
                ?.let { drawObjects.drawWithData(canvas, graphYAxisScaleFactor) }
                ?: drawObjects.drawWithoutData(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.pointsData = this.pointsData
        savedState.selectorValue = this.selectorValue
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        this.listenersEnabled = false
        this.pointsData = state.pointsData
        this.selectorValue = state.selectorValue
        resetDataAndSelectorDrawObjects()
        this.listenersEnabled = true

        super.onRestoreInstanceState(state.superState)
    }

    private fun moveSelectorXPosition(x: Float, animated: Boolean = false) {
        if (animated) {
            selectorAnimator.setFloatValues(drawObjects.getSelectorXPosition(), x)
            selectorAnimator.start()
        } else {
            drawObjects.updateSelectorDependantShapes(x)
            invalidate()
        }
    }

    private fun resetDataAndSelectorDrawObjects() {
        pointsData?.let {
            drawObjects.refreshDataObjects(it)
            drawObjects.updateSelectorDependantShapes(transformSelectorValueToXPosition(it, this.selectorValue))
        }
    }

    private fun transformSelectorXPositionToValue(pointsData: PointsData, selectorXPosition: Float): Float =
            affineTransformXToY(selectorXPosition, dimensions.graphLeft, dimensions.graphRight, pointsData.minX, pointsData.maxX)

    private fun transformSelectorValueToXPosition(pointsData: PointsData, selectorValue: Float): Float =
            affineTransformXToY(selectorValue, pointsData.minX, pointsData.maxX, dimensions.graphLeft, dimensions.graphRight)

    /**
     * Listener interface whose methods are called as a consequence of selector's interaction
     * and [selectorValue] value change events.
     */
    interface SelectorListener {

        /**
         * Called when selector is pressed.
         */
        fun onSelectorPressed()

        /**
         * Called when [selectorValue] is changed.
         *
         * @param newValue [selectorValue] new value
         */
        fun onValueChanged(newValue: Float)

        /**
         * Called when selector is released.
         */
        fun onSelectorReleased()
    }
}