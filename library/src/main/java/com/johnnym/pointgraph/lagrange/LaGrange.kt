package com.johnnym.pointgraph.lagrange

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.johnnym.pointgraph.PointsData
import com.johnnym.pointgraph.utils.*
import kotlin.properties.Delegates

/**
 * View that enables the user to select a subrange of a range defined by the
 * [PointsData]'s minimum and maximum values, while having a graph presentation
 * of [Point]s defined in the same [PointsData] object.
 */
class LaGrange @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val attributes: LaGrangeAttrs = LaGrangeAttrs(context, attrs, defStyleAttr)
    private val dimensions: LaGrangeDimensions = LaGrangeDimensions(attributes)
    private val paints: LaGrangePaints = LaGrangePaints(attributes)
    private val drawObjects: LaGrangeDraw = LaGrangeDraw(attributes, dimensions, paints, object : LaGrangeDraw.Listener {

        override fun minSelectorDependantShapesChanged() {
            invalidate()
        }

        override fun maxSelectorDependantShapesChanged() {
            invalidate()
        }

        override fun graphYAxisScaleFactorChanged() {
            invalidate()
        }

    })
    private val touchHandler: LaGrangeTouchHandler = LaGrangeTouchHandler(this, dimensions, drawObjects, object : LaGrangeTouchHandler.Listener {

        override fun minSelectorChanged() {
            updateSelectorValues()
        }

        override fun maxSelectorChanged() {
            updateSelectorValues()
        }
    })

    private var pointsData: PointsData? = null
    private val minSelectorPositionChangeListeners = ArrayList<LaGrange.MinSelectorPositionChangeListener>()
    private val maxSelectorPositionChangeListeners = ArrayList<LaGrange.MaxSelectorPositionChangeListener>()
    private var listenersEnabled = true

    /**
     * True min selector value (set from outside or by touch events)
     */
    var minSelectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            minSelectorPositionChangeListeners.dispatchOnMinSelectorPositionChangeEvent(new)
        }
    }

    /**
     * True max selector value (set from outside or by touch events)
     */
    var maxSelectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            maxSelectorPositionChangeListeners.dispatchOnMaxSelectorPositionChangeEvent(new)
        }
    }

    private fun ArrayList<LaGrange.MinSelectorPositionChangeListener>.dispatchOnMinSelectorPositionChangeEvent(newMinValue: Float) {
        this.forEach { it.onMinValueChanged(newMinValue) }
    }

    private fun ArrayList<LaGrange.MaxSelectorPositionChangeListener>.dispatchOnMaxSelectorPositionChangeEvent(newMaxValue: Float) {
        this.forEach { it.onMaxValueChanged(newMaxValue) }
    }

    /**
     * Set [LaGrange] graph data
     *
     * @param pointsData [Point]s to be shown in form of a graph
     * @param animated true if this set of data should be animated
     */
    fun setPointsData(pointsData: PointsData?, animated: Boolean = true) {
        this.pointsData = pointsData

        refreshGraphValues()

        if (animated) {
            drawObjects.graphScaleAnimator.start()
        } else {
            invalidate()
        }
    }

    /**
     * Set [minSelectorValue] and [maxSelectorValue] values
     *
     * Values will be normalized to fit the current [PointsData] value in [pointsData].
     * Also, if [maxValue] is smaller than [minValue], its value will be changed to [minValue].
     *
     * @param minValue wanted [minSelectorValue]
     * @param maxValue wanted [maxSelectorValue]
     */
    fun setSelectorsValues(minValue: Float?, maxValue: Float?) {
        // if user is interacting with the view, do not set values from outside
        if (touchHandler.isAnySelectorSelected()) {
            return
        }

        this.pointsData?.let { pointsData ->
            var minSelectorValue = minValue ?: pointsData.minX
            var maxSelectorValue = maxValue ?: pointsData.maxX

            minSelectorValue = Math.max(minSelectorValue, pointsData.minX)
            minSelectorValue = Math.min(minSelectorValue, pointsData.maxX)
            maxSelectorValue = Math.max(maxSelectorValue, pointsData.minX)
            maxSelectorValue = Math.min(maxSelectorValue, pointsData.maxX)

            if (minSelectorValue > maxSelectorValue) {
                maxSelectorValue = minSelectorValue
            }

            this.minSelectorValue = minSelectorValue
            this.maxSelectorValue = maxSelectorValue

            drawObjects.setMinSelectorXPosition(
                    affineTransformXToY(minSelectorValue, pointsData.minX, pointsData.maxX, dimensions.graphLeft, dimensions.graphRight),
                    attributes.animateSelectorChanges)

            drawObjects.setMaxSelectorXPosition(
                    affineTransformXToY(maxSelectorValue, pointsData.minX, pointsData.maxX, dimensions.graphLeft, dimensions.graphRight),
                    attributes.animateSelectorChanges)
        }
    }

    /**
     * Add a listener which will be informed about the changes of [minSelectorValue].
     *
     * @param minSelectorPositionChangeListener listener to be added
     */
    fun addMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        this.minSelectorPositionChangeListeners.add(minSelectorPositionChangeListener)
    }

    /**
     * Remove a listener which previously has been informed about the changes of [minSelectorValue].
     *
     * @param minSelectorPositionChangeListener listener to be removed
     */
    fun removeMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        this.minSelectorPositionChangeListeners.remove(minSelectorPositionChangeListener)
    }

    /**
     * Add a listener which will be informed about the changes of [maxSelectorValue].
     *
     * @param maxSelectorPositionChangeListener listener to be added
     */
    fun addMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        this.maxSelectorPositionChangeListeners.add(maxSelectorPositionChangeListener)
    }

    /**
     * Remove a listener which previously has been informed about the changes of [maxSelectorValue].
     *
     * @param maxSelectorPositionChangeListener listener to be removed
     */
    fun removeMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        this.maxSelectorPositionChangeListeners.remove(maxSelectorPositionChangeListener)
    }

    private fun updateSelectorValues() {
        pointsData?.let {
            this.minSelectorValue = affineTransformXToY(drawObjects.getMinSelectorXPosition(), dimensions.graphLeft, dimensions.graphRight, it.minX, it.maxX)
            this.maxSelectorValue = affineTransformXToY(drawObjects.getMaxSelectorXPosition(), dimensions.graphLeft, dimensions.graphRight, it.minX, it.maxX)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSizeAndState(attributes.minViewWidth.toInt(), widthMeasureSpec, 0)
        val height = resolveSizeAndState(attributes.minViewHeight.toInt(), heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        dimensions.onSizeChanged(
                paddingLeft,
                w - paddingRight,
                paddingTop,
                h - paddingBottom)
        drawObjects.updateObjects()

        this.pointsData?.let { pointsData ->
            drawObjects.refreshGraph(pointsData)
            drawObjects.refreshSelectors(pointsData, minSelectorValue, maxSelectorValue)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        this.pointsData
                ?.let {
                    touchHandler.handleTouchEvent(event)
                    return true
                }
                ?: return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        this.pointsData
                ?.let { drawObjects.draw(canvas, true) }
                ?: drawObjects.draw(canvas, false)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.pointsData = this.pointsData
        savedState.minSelectorValue = this.minSelectorValue
        savedState.maxSelectorValue = this.maxSelectorValue
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        this.listenersEnabled = false
        this.pointsData = state.pointsData
        this.minSelectorValue = state.minSelectorValue
        this.maxSelectorValue = state.maxSelectorValue
        refreshGraphValues()
        this.listenersEnabled = true

        super.onRestoreInstanceState(state.superState)
    }

    private fun refreshGraphValues() {
        this.pointsData?.let {
            this.minSelectorValue = it.minX
            this.maxSelectorValue = it.maxX

            drawObjects.updateNumbers(it.minX, it.maxX)
            drawObjects.refreshGraph(it)
            drawObjects.refreshSelectors(it, this.minSelectorValue, this.maxSelectorValue)

            invalidate()
        }
    }

    /**
     * Listener interface whose methods are called as a consequence of
     * [minSelectorValue] value change events.
     */
    interface MinSelectorPositionChangeListener {

        /**
         * Called when [minSelectorValue] is changed.
         *
         * @param newMinValue [minSelectorValue] new value
         */
        fun onMinValueChanged(newMinValue: Float)
    }

    /**
     * Listener interface whose methods are called as a consequence of
     * [minSelectorValue] value change events.
     */
    interface MaxSelectorPositionChangeListener {

        /**
         * Called when [maxSelectorValue] is changed.
         *
         * @param newMaxValue [maxSelectorValue] new value
         */
        fun onMaxValueChanged(newMaxValue: Float)
    }

    class SavedState : BaseSavedState {

        var pointsData: PointsData? = null
        var minSelectorValue: Float = 0f
        var maxSelectorValue: Float = 0f

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(pointsData, flags)
            out.writeFloat(minSelectorValue)
            out.writeFloat(maxSelectorValue)
        }

        private constructor(parcel: Parcel) : super(parcel) {
            pointsData = parcel.readParcelable(PointsData::class.java.classLoader)
            minSelectorValue = parcel.readFloat()
            maxSelectorValue = parcel.readFloat()
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}