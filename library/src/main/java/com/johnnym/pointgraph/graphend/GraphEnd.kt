package com.johnnym.pointgraph.graphend

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.johnnym.pointgraph.Point
import com.johnnym.pointgraph.PointsData
import com.johnnym.pointgraph.R
import com.johnnym.pointgraph.utils.getXPosition
import com.johnnym.pointgraph.utils.setXMiddle
import com.johnnym.pointgraph.utils.setYMiddle
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

    companion object {

        // Graph Y Axis Scale Factor
        private val GRAPH_Y_AXIS_SCALE_FACTOR_MIN_VALUE: Float = 0f
        private val GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE: Float = 1f
    }

    // Constant graph values
    private val lineColor: Int
    private val lineThickness: Float
    private val selectedLineColor: Int
    private val selectedLineThickness: Float
    private val selectorColor: Int
    private val graphColor: Int
    private val selectedGraphColor: Int

    private val minViewWidth: Float
    private val minViewHeight: Float
    private val graphTopDrawPositionFromTop: Float
    private val graphTopPositionFromTop: Float
    private val lineYPositionFromBottom: Float
    private val xAxisLeftRightPadding: Float
    private val selectorDiameter: Float
    private val selectionFieldHeight: Float

    // Graph drawing objects
    private val xAxisRectPaint: Paint
    private val selectorPaint: Paint
    private val selectedLinePaint: Paint
    private val graphPaint: Paint
    private val selectedGraphPaint: Paint

    private val selector: RectF
    private val selectionField: RectF
    private val xAxisRect: RectF
    private val graphBoundsRect: RectF
    private val selectedGraphBoundsRect: RectF
    private val selectedLine: RectF
    private val splineGraphPath: Path
    private val selectorAnimator: ValueAnimator
    private val graphScaleAnimator: ValueAnimator

    private var graphYAxisScaleFactor: Float = GRAPH_Y_AXIS_SCALE_FACTOR_MAX_VALUE
    private var graphIsShown: Boolean = true

    // View's dimensions and sizes, positions etc.
    private var graphTopDrawYPosition: Float = 0f
    private var graphTopYPosition: Float = 0f
    private var lineYPosition: Float = 0f
    private var viewStartX: Float = 0f
    private var viewEndX: Float = 0f
    private var viewStartY: Float = 0f
    private var viewEndY: Float = 0f
    private var graphMinXPosition: Float = 0f
    private var graphMaxXPosition: Float = 0f

    // Touch event variables
    private var actionDownXValue: Float = 0f
    private var actionDownYValue: Float = 0f
    private var selectorSelected: Boolean = false

    // Data variables
    private var pointsData: PointsData? = null
    private val selectorListeners = ArrayList<SelectorListener>()

    private var listenersEnabled = true

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
        // Calculate view dimensions from the given attributes
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.pg__GraphEnd, defStyleAttr, 0)
        try {
            lineColor = attributes.getColor(
                    R.styleable.pg__GraphEnd_pg__x_axis_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_x_axis_color))
            lineThickness = attributes.getDimension(
                    R.styleable.pg__GraphEnd_pg__x_axis_thickness,
                    resources.getDimension(R.dimen.pg__default_line_thickness))
            selectedLineColor = attributes.getColor(
                    R.styleable.pg__GraphEnd_pg__selected_line_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selected_line_color))
            selectedLineThickness = attributes.getDimension(
                    R.styleable.pg__GraphEnd_pg__selected_line_thickness,
                    resources.getDimension(R.dimen.pg__default_selected_line_thickness))
            selectorColor = attributes.getColor(
                    R.styleable.pg__GraphEnd_pg__selector_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selector_color))
            graphColor = attributes.getColor(
                    R.styleable.pg__GraphEnd_pg__graph_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_graph_color))
            selectedGraphColor = attributes.getColor(
                    R.styleable.pg__GraphEnd_pg__selected_graph_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selected_graph_color))
        } finally {
            attributes.recycle()
        }

        minViewWidth = resources.getDimension(R.dimen.pg__graph_end_min_view_width)
        minViewHeight = resources.getDimension(R.dimen.pg__graph_end_min_view_height)
        graphTopDrawPositionFromTop = resources.getDimension(R.dimen.pg__graph_end_graph_top_draw_position_from_top)
        graphTopPositionFromTop = resources.getDimension(R.dimen.pg__graph_end_graph_top_position_from_top)
        lineYPositionFromBottom = resources.getDimension(R.dimen.pg__graph_end_line_y_position_from_bottom)
        xAxisLeftRightPadding = resources.getDimension(R.dimen.pg__graph_end_x_axis_left_right_padding)
        selectorDiameter = resources.getDimension(R.dimen.pg__graph_end_selector_diameter)
        selectionFieldHeight = resources.getDimension(R.dimen.pg__graph_end_selection_field_height)

        // Init drawing objects
        selector = RectF(
                0f,
                0f,
                selectorDiameter,
                selectorDiameter)

        xAxisRect = RectF(0f, 0f, 0f, lineThickness)
        selectionField = RectF(
                0f,
                0f,
                0f,
                selectionFieldHeight)

        graphBoundsRect = RectF()
        selectedGraphBoundsRect = RectF()
        selectedLine = RectF(
                0f,
                0f,
                0f,
                selectedLineThickness)

        splineGraphPath = Path()

        selectorAnimator = ValueAnimator()
        selectorAnimator.duration = 150

        graphScaleAnimator = ValueAnimator()
        graphScaleAnimator.duration = 300
        graphScaleAnimator.addUpdateListener { animation ->
            graphYAxisScaleFactor = animation.animatedValue as Float
            invalidate()
        }

        // Init draw settings
        xAxisRectPaint = Paint()
        xAxisRectPaint.isAntiAlias = true
        xAxisRectPaint.color = lineColor
        xAxisRectPaint.style = Paint.Style.FILL

        selectorPaint = Paint()
        selectorPaint.isAntiAlias = true
        selectorPaint.style = Paint.Style.FILL
        selectorPaint.color = selectorColor
        selectorPaint.setShadowLayer(12f, 0f, 0f, Color.YELLOW)
        setLayerType(LAYER_TYPE_SOFTWARE, selectorPaint)

        selectedLinePaint = Paint()
        selectedLinePaint.isAntiAlias = true
        selectedLinePaint.style = Paint.Style.FILL
        selectedLinePaint.color = selectedLineColor

        graphPaint = Paint()
        graphPaint.isAntiAlias = true
        graphPaint.color = graphColor
        graphPaint.style = Paint.Style.FILL

        selectedGraphPaint = Paint()
        selectedGraphPaint.isAntiAlias = true
        selectedGraphPaint.color = selectedGraphColor
        selectedGraphPaint.style = Paint.Style.FILL
    }

    /**
     * Set [GraphEnd] graph data
     *
     * @param pointsData [Point]s to be shown in form of a graph
     * @param animated true if this set of data should be animated
     */
    fun setPointsData(pointsData: PointsData?, animated: Boolean = true) {
        refreshGraphValues(pointsData)
        if (animated) {
            showGraph()
        } else {
            invalidate()
        }
    }

    /**
     * Set [selectorValue] value
     *
     * @param value wanted [selectorValue]
     * @param animateSelectorChanges true if the selector change should be animated
     */
    fun setSelectorValue(value: Float?, animateSelectorChanges: Boolean = false) {
        // if user is interacting with the view, do not set values from outside
        if (selectorSelected) {
            return
        }

        pointsData?.let { pointsData ->
            var selectorValue: Float = value ?: pointsData.minX

            selectorValue = Math.max(selectorValue, pointsData.minX)
            selectorValue = Math.min(selectorValue, pointsData.maxX)

            this.selectorValue = selectorValue

            setSelectorXPosition(
                    getXGraphPositionFromXValue(pointsData, selectorValue),
                    animateSelectorChanges)
        }
    }

    /**
     * Add a listener which will be informed about [selector] interaction
     * and [selectorValue] value change events.
     *
     * @param selectorListener listener to be added
     */
    fun addSelectorListener(selectorListener: SelectorListener) {
        selectorListeners.add(selectorListener)
    }

    /**
     * Remove a listener which previously has been informed about
     * [selector] interaction and [selectorValue] value change events.
     *
     * @param selectorListener listener to be removed
     */
    fun removeSelectorListener(selectorListener: SelectorListener) {
        selectorListeners.remove(selectorListener)
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
        val width = resolveSizeAndState(minViewWidth.toInt(), widthMeasureSpec, 0)
        val height = resolveSizeAndState(minViewHeight.toInt(), heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewStartX = paddingLeft.toFloat()
        viewEndX = (w - paddingRight).toFloat()
        viewStartY = paddingTop.toFloat()
        viewEndY = (h - paddingBottom).toFloat()

        graphTopDrawYPosition = viewStartY + graphTopDrawPositionFromTop
        graphTopYPosition = viewStartY + graphTopPositionFromTop
        lineYPosition = viewEndY - lineYPositionFromBottom

        xAxisRect.left = viewStartX + xAxisLeftRightPadding
        xAxisRect.right = viewEndX - xAxisLeftRightPadding
        xAxisRect.setYMiddle(lineYPosition)

        selectionField.left = xAxisRect.left - selectionFieldHeight / 2
        selectionField.right = xAxisRect.right + selectionFieldHeight / 2
        selectionField.setYMiddle(lineYPosition)

        graphMinXPosition = xAxisRect.left
        graphMaxXPosition = xAxisRect.right

        graphTopDrawYPosition = viewStartY

        graphBoundsRect.set(graphMinXPosition, graphTopDrawYPosition, graphMaxXPosition, lineYPosition)

        selectedGraphBoundsRect.set(graphBoundsRect)
        selectedLine.left = selectedGraphBoundsRect.left
        selectedLine.right = selectedGraphBoundsRect.right

        selector.setYMiddle(lineYPosition)
        selectedLine.setYMiddle(lineYPosition)

        pointsData?.let { pointsData ->
            refreshSelectorPosition(pointsData)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        pointsData?.let { pointsData ->
            val action = event.actionMasked

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    actionDownXValue = event.x
                    actionDownYValue = event.y
                    if (selectionField.contains(actionDownXValue, actionDownYValue)) {
                        selectorSelected = true
                        selectorListeners.dispatchOnSelectorPressedEvent()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (selectorSelected) {
                        selectorSelected = false
                        selectorListeners.dispatchOnSelectorReleasedEvent()
                    }
                }
            }

            if (selectorSelected) {
                var newXPosition = event.x
                newXPosition = Math.max(newXPosition, graphMinXPosition)
                newXPosition = Math.min(newXPosition, graphMaxXPosition)
                setSelectorXPosition(newXPosition)
                updateSelectorValue(pointsData)

                // If selector is selected, then user must be able to move his finger anywhere
                // on the screen and still have control of the selected selector.
                parent.requestDisallowInterceptTouchEvent(true)

                return true
            } else {
                return false
            }
        }
                ?: return false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        // draw X axis line
        canvas.drawRoundRect(
                xAxisRect,
                lineThickness / 2,
                lineThickness / 2,
                xAxisRectPaint)

        if (hasData()) {
            drawDataViewPart(canvas)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.pointsData = pointsData
        savedState.selectorValue = selectorValue
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        listenersEnabled = false
        state.pointsData
                ?.let { pointsData ->
                    refreshGraphValues(pointsData, state.selectorValue)
                }
                ?: refreshGraphValues(state.pointsData)
        listenersEnabled = true

        super.onRestoreInstanceState(state.superState)
    }

    private fun drawDataViewPart(canvas: Canvas) {
        canvas.save()
        canvas.scale(1f, graphYAxisScaleFactor, 0f, lineYPosition)
        canvas.clipRect(graphBoundsRect)
        canvas.drawPath(splineGraphPath, graphPaint)
        canvas.clipRect(selectedGraphBoundsRect)
        canvas.drawPath(splineGraphPath, selectedGraphPaint)
        canvas.restore()

        // draw selected line and selector
        canvas.drawRoundRect(
                selectedLine,
                selectedLineThickness / 2,
                selectedLineThickness / 2,
                selectedLinePaint)
        canvas.drawOval(selector, selectorPaint)
    }

    private fun hasData(): Boolean {
        return pointsData != null
    }

    private fun setSelectorXPosition(x: Float, animated: Boolean = false) {

        fun setSelectorShapes(x: Float) {
            selectedGraphBoundsRect.right = x
            selectedLine.right = x
            selector.setXMiddle(x)
        }

        if (animated) {
            selectorAnimator.setFloatValues(selector.getXPosition(), x)
            selectorAnimator.addUpdateListener { animation ->
                setSelectorShapes(animation.animatedValue as Float)
                invalidate()
            }
            selectorAnimator.start()
        } else {
            setSelectorShapes(x)
            invalidate()
        }
    }

    private fun updateSelectorValue(pointsData: PointsData) {
        selectorValue = getXValueFromXGraphPosition(pointsData, selector.getXPosition())
    }

    private fun getXValueFromXGraphPosition(pointsData: PointsData, x: Float): Float {
        return (x - graphMinXPosition) * pointsData.xRange / (graphMaxXPosition - graphMinXPosition) + pointsData.minX
    }

    private fun getXGraphPositionFromXValue(pointsData: PointsData, x: Float): Float {
        return (graphMaxXPosition - graphMinXPosition) / pointsData.xRange * (x - pointsData.minX) + graphMinXPosition
    }

    private fun getYGraphPositionFromYValue(pointsData: PointsData, y: Float): Float {
        return (lineYPosition - graphTopYPosition) / (pointsData.maxY - pointsData.minY) * (pointsData.maxY - y) + graphTopYPosition
    }

    private fun refreshGraphValues(newPointsData: PointsData?) {
        pointsData = newPointsData

        pointsData?.let { pointsData ->
            refreshGraphValues(pointsData, pointsData.minX)
        }
    }

    private fun refreshGraphValues(pointsData: PointsData, selectorValue: Float) {
        this.pointsData = pointsData

        this.selectorValue = selectorValue

        refreshSelectorPosition(pointsData)
    }

    private fun refreshSelectorPosition(pointsData: PointsData) {
        setSelectorXPosition(getXGraphPositionFromXValue(pointsData, selectorValue))
    }

    private fun getGraphPointsFromPointsData(pointsData: PointsData): List<Point> {
        val points = pointsData.points

        return List(points.size) {
            Point(
                    getXGraphPositionFromXValue(pointsData, points[it].x),
                    getYGraphPositionFromYValue(pointsData, points[it].y))
        }
    }

    /**
     * Listener interface whose methods are called as a consequence of [selector] interaction
     * and [selectorValue] value change events.
     */
    interface SelectorListener {

        /**
         * Called when [selectionField] is pressed.
         */
        fun onSelectorPressed()

        /**
         * Called when [selectorValue] is changed.
         *
         * @param newValue [selectorValue] new value
         */
        fun onValueChanged(newValue: Float)

        /**
         * Called when [selectionField] is released.
         */
        fun onSelectorReleased()
    }

    private fun ArrayList<SelectorListener>.dispatchOnSelectorPressedEvent() {
        this.forEach { it.onSelectorPressed() }
    }

    private fun ArrayList<SelectorListener>.dispatchOnValueChangedEvent(newValue: Float) {
        this.forEach { it.onValueChanged(newValue) }
    }

    private fun ArrayList<SelectorListener>.dispatchOnSelectorReleasedEvent() {
        this.forEach { it.onSelectorReleased() }
    }

    class SavedState : BaseSavedState {

        var pointsData: PointsData? = null
        var selectorValue: Float = 0f

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(pointsData, flags)
            out.writeFloat(selectorValue)
        }

        private constructor(parcel: Parcel) : super(parcel) {
            pointsData = parcel.readParcelable(PointsData::class.java.classLoader)
            selectorValue = parcel.readFloat()
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