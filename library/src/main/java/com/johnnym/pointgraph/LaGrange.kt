package com.johnnym.pointgraph

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.johnnym.pointgraph.utils.affineTransformXToY
import com.johnnym.pointgraph.utils.getXPosition
import com.johnnym.pointgraph.utils.setXMiddle
import com.johnnym.pointgraph.utils.setYMiddle
import kotlin.collections.ArrayList
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

    // Constant graph values
    private val lineColor: Int
    private val lineThickness: Float
    private val selectedLineColor: Int
    private val selectedLineThickness: Float
    private val selectorColor: Int
    private val textColor: Int
    private val textSize: Float
    private val graphColor: Int
    private val selectedGraphColor: Int
    private val lineMiddlePointsNum: Int
    private val animateSelectorChanges: Boolean

    private val minViewWidth: Float
    private val minViewHeight: Float
    private val graphTopFromTop: Float
    private val graphBottomFromBottom: Float
    private val graphLeftRightPadding: Float
    private val numbersYPositionFromBottom: Float
    private val pointIndicatorLength: Float
    private val selectorDiameter: Float
    private val selectorTouchDiameter: Float

    // Graph drawing objects
    private val xAxisRectPaint: Paint
    private val textPaint: TextPaint
    private val selectorPaint: Paint
    private val selectedLinePaint: Paint
    private val graphPaint: Paint
    private val selectedGraphPaint: Paint

    private val minSelector: RectF
    private val maxSelector: RectF
    private val minSelectorTouchField: RectF
    private val maxSelectorTouchField: RectF
    private val xAxisRect: RectF
    private val xAxisFirstPointRect: RectF
    private val xAxisLastPointRect: RectF
    private val xAxisMiddlePointsRects: List<RectF>
    private val graphPath: GraphPath
    private val graphBoundsRect: RectF
    private val selectedGraphBoundsRect: RectF
    private val selectedLine: RectF
    private val numbers: FloatArray
    private val minSelectorAnimator: ValueAnimator
    private val maxSelectorAnimator: ValueAnimator
    private val graphScaleAnimator: ValueAnimator

    // View's dimensions and sizes, positions etc.
    private var graphTop: Float = 0f
    private var graphBottom: Float = 0f
    private var graphLeft: Float = 0f
    private var graphRight: Float = 0f
    private var numbersYPosition: Float = 0f
    private var viewStartX: Float = 0f
    private var viewEndX: Float = 0f
    private var viewStartY: Float = 0f
    private var viewEndY: Float = 0f
    private var pointsDistance: Float = 0f
    private var numbersPositions: FloatArray
    private var graphYAxisScaleFactor: Float = 1f

    // Touch event variables
    private var actionDownXValue: Float = 0f
    private var actionDownYValue: Float = 0f
    private var actionMoveXValue: Float = 0f
    private var minSelectorSelected: Boolean = false
    private var maxSelectorSelected: Boolean = false
    private var bothSelectorsSelected: Boolean = false

    // Data variables
    private var pointsData: PointsData? = null
    private val minSelectorPositionChangeListeners = ArrayList<MinSelectorPositionChangeListener>()
    private val maxSelectorPositionChangeListeners = ArrayList<MaxSelectorPositionChangeListener>()

    private var listenersEnabled = true

    /**
     * True min selector value (set from outside or by touch events)
     */
    var minSelectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            minSelectorPositionChangeListeners.dispatchOnMinSelectorPositionChangeEvent(new)
        }
    }
        private set

    /**
     * True max selector value (set from outside or by touch events)
     */
    var maxSelectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            maxSelectorPositionChangeListeners.dispatchOnMaxSelectorPositionChangeEvent(new)
        }
    }
        private set

    init {
        // Calculate view dimensions from the given attributes
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.pg__LaGrange, defStyleAttr, 0)
        try {
            lineColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__line_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_line_color))
            lineThickness = attributes.getDimension(
                    R.styleable.pg__LaGrange_pg__line_thickness,
                    resources.getDimension(R.dimen.pg__default_line_thickness))
            selectedLineColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__selected_line_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selected_line_color))
            selectedLineThickness = attributes.getDimension(
                    R.styleable.pg__LaGrange_pg__selected_line_thickness,
                    resources.getDimension(R.dimen.pg__default_selected_line_thickness))
            selectorColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__selector_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selector_color))
            textColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__text_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_text_color))
            textSize = attributes.getDimension(
                    R.styleable.pg__LaGrange_pg__text_size,
                    resources.getDimension(R.dimen.pg__default_text_size))
            graphColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__graph_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_graph_color))
            selectedGraphColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__selected_graph_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selected_graph_color))
            lineMiddlePointsNum = attributes.getInteger(
                    R.styleable.pg__LaGrange_pg__line_middle_points_num,
                    resources.getInteger(R.integer.pg__default_line_middle_points_num))
            animateSelectorChanges = attributes.getBoolean(
                    R.styleable.pg__LaGrange_pg__animate_selector_changes,
                    resources.getBoolean(R.bool.pg__default_animate_selector_changes))
        } finally {
            attributes.recycle()
        }

        minViewWidth = resources.getDimension(R.dimen.pg__la_grange_min_view_width)
        minViewHeight = resources.getDimension(R.dimen.pg__la_grange_min_view_height)
        graphTopFromTop = resources.getDimension(R.dimen.pg__la_grange_graph_top_from_top)
        graphBottomFromBottom = resources.getDimension(R.dimen.pg__la_grange_graph_bottom_from_bottom)
        graphLeftRightPadding = resources.getDimension(R.dimen.pg__la_grange_graph_left_right_padding)
        numbersYPositionFromBottom = resources.getDimension(R.dimen.pg__la_grange_numbers_y_position_from_bottom)
        pointIndicatorLength = resources.getDimension(R.dimen.pg__la_grange_point_indicator_length)
        selectorDiameter = resources.getDimension(R.dimen.pg__la_grange_selector_diameter)
        selectorTouchDiameter = resources.getDimension(R.dimen.pg__la_grange_selector_touch_diameter)

        numbersPositions = FloatArray(lineMiddlePointsNum + 2)

        // Init drawing objects
        minSelector = RectF(
                0f,
                0f,
                selectorDiameter,
                selectorDiameter)
        maxSelector = RectF(minSelector)
        minSelectorTouchField = RectF(
                0f,
                0f,
                selectorTouchDiameter,
                selectorTouchDiameter)
        maxSelectorTouchField = RectF(minSelectorTouchField)

        xAxisRect = RectF(0f, 0f, 0f, lineThickness)
        xAxisFirstPointRect = RectF()
        xAxisLastPointRect = RectF()
        xAxisMiddlePointsRects = List(lineMiddlePointsNum) { RectF() }
        numbers = FloatArray(lineMiddlePointsNum + 2)

        graphPath = GraphPath()
        graphBoundsRect = RectF()
        selectedGraphBoundsRect = RectF()

        selectedLine = RectF(
                0f,
                0f,
                0f,
                selectedLineThickness)

        minSelectorAnimator = ValueAnimator()
        minSelectorAnimator.duration = 150
        maxSelectorAnimator = ValueAnimator()
        maxSelectorAnimator.duration = 150
        graphScaleAnimator = ValueAnimator()
        graphScaleAnimator.duration = 300

        // Init draw settings
        xAxisRectPaint = Paint()
        xAxisRectPaint.isAntiAlias = true
        xAxisRectPaint.color = lineColor
        xAxisRectPaint.style = Paint.Style.FILL

        textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize

        selectorPaint = Paint()
        selectorPaint.isAntiAlias = true
        selectorPaint.style = Paint.Style.FILL
        selectorPaint.color = selectorColor

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
     * Set [LaGrange] graph data
     *
     * @param pointsData [Point]s to be shown in form of a graph
     * @param animated true if this set of data should be animated
     */
    fun setPointsData(pointsData: PointsData?, animated: Boolean = true) {
        refreshGraphValues(pointsData)
        if (animated) {
            graphScaleAnimator.setFloatValues(0f, 1f)
            graphScaleAnimator.addUpdateListener { animation ->
                graphYAxisScaleFactor = animation.animatedValue as Float
                invalidate()
            }
            graphScaleAnimator.start()
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
        if (minSelectorSelected || maxSelectorSelected) {
            return
        }

        pointsData?.let { pointsData ->
            var minSelectorValue: Float = minValue ?: pointsData.minX
            var maxSelectorValue: Float = maxValue ?: pointsData.maxX

            minSelectorValue = Math.max(minSelectorValue, pointsData.minX)
            minSelectorValue = Math.min(minSelectorValue, pointsData.maxX)
            maxSelectorValue = Math.max(maxSelectorValue, pointsData.minX)
            maxSelectorValue = Math.min(maxSelectorValue, pointsData.maxX)

            if (minSelectorValue > maxSelectorValue) {
                maxSelectorValue = minSelectorValue
            }

            this.minSelectorValue = minSelectorValue
            this.maxSelectorValue = maxSelectorValue

            setMinSelectorXPosition(
                    affineTransformXToY(minSelectorValue, pointsData.minX, pointsData.maxX, graphLeft, graphRight),
                    animateSelectorChanges)

            setMaxSelectorXPosition(
                    affineTransformXToY(maxSelectorValue, pointsData.minX, pointsData.maxX, graphLeft, graphRight),
                    animateSelectorChanges)
        }
    }

    /**
     * Add a listener which will be informed about the changes of [minSelectorValue].
     *
     * @param minSelectorPositionChangeListener listener to be added
     */
    fun addMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        minSelectorPositionChangeListeners.add(minSelectorPositionChangeListener)
    }

    /**
     * Remove a listener which previously has been informed about the changes of [minSelectorValue].
     *
     * @param minSelectorPositionChangeListener listener to be removed
     */
    fun removeMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        minSelectorPositionChangeListeners.remove(minSelectorPositionChangeListener)
    }

    /**
     * Add a listener which will be informed about the changes of [maxSelectorValue].
     *
     * @param maxSelectorPositionChangeListener listener to be added
     */
    fun addMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        maxSelectorPositionChangeListeners.add(maxSelectorPositionChangeListener)
    }

    /**
     * Remove a listener which previously has been informed about the changes of [maxSelectorValue].
     *
     * @param maxSelectorPositionChangeListener listener to be removed
     */
    fun removeMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        maxSelectorPositionChangeListeners.remove(maxSelectorPositionChangeListener)
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

        graphTop = viewStartY + graphTopFromTop
        graphBottom = viewEndY - graphBottomFromBottom
        graphLeft = viewStartX + graphLeftRightPadding
        graphRight = viewEndX - graphLeftRightPadding

        numbersYPosition = viewEndY - numbersYPositionFromBottom

        xAxisRect.left = graphLeft - lineThickness / 2
        xAxisRect.right = graphRight + lineThickness / 2
        xAxisRect.setYMiddle(graphBottom)
        xAxisFirstPointRect.set(xAxisRect.left, xAxisRect.top, xAxisRect.left + lineThickness, xAxisRect.top + pointIndicatorLength)
        xAxisLastPointRect.set(xAxisRect.right - lineThickness, xAxisRect.bottom - pointIndicatorLength, xAxisRect.right, xAxisRect.bottom)

        // Calculate X axis number positions
        numbersPositions[0] = xAxisFirstPointRect.centerX()
        numbersPositions[numbersPositions.size - 1] = xAxisLastPointRect.centerX()
        pointsDistance = (xAxisRect.right - xAxisRect.left - (2 + lineMiddlePointsNum) * lineThickness) / (lineMiddlePointsNum + 1)
        var middlePointIndicatorX: Float
        for (i in xAxisMiddlePointsRects.indices) {
            middlePointIndicatorX = xAxisFirstPointRect.left + (i + 1) * (pointsDistance + lineThickness)
            xAxisMiddlePointsRects[i].set(
                    middlePointIndicatorX,
                    xAxisRect.top,
                    middlePointIndicatorX + lineThickness,
                    xAxisRect.top + pointIndicatorLength)
            numbersPositions[i + 1] = xAxisMiddlePointsRects[i].centerX()
        }

        graphBoundsRect.set(graphLeft, graphTop, graphRight, graphBottom)
        selectedGraphBoundsRect.set(graphBoundsRect)

        selectedLine.left = selectedGraphBoundsRect.left
        selectedLine.right = selectedGraphBoundsRect.right

        minSelector.setYMiddle(graphBottom)
        minSelectorTouchField.setYMiddle(graphBottom)
        maxSelector.setYMiddle(graphBottom)
        maxSelectorTouchField.setYMiddle(graphBottom)
        selectedLine.setYMiddle(graphBottom)

        pointsData?.let { pointsData ->
            graphPath.generatePath(pointsData, graphLeft, graphBottom, graphRight, graphTop)
            refreshSelectorsPositions(pointsData)
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

                    val minSelectorTouchFieldContainsTouch = minSelectorTouchField.contains(actionDownXValue, actionDownYValue)
                    val maxSelectorTouchFieldContainsTouch = maxSelectorTouchField.contains(actionDownXValue, actionDownYValue)

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
                if (newXPosition < graphLeft) {
                    newXPosition = graphLeft
                } else if (newXPosition > maxSelector.getXPosition()) {
                    newXPosition = maxSelector.getXPosition()
                }
                setMinSelectorXPosition(newXPosition)
                updateSelectorValues(pointsData)
            } else if (maxSelectorSelected) {
                if (newXPosition > graphRight) {
                    newXPosition = graphRight
                } else if (newXPosition < minSelector.getXPosition()) {
                    newXPosition = minSelector.getXPosition()
                }
                setMaxSelectorXPosition(newXPosition)
                updateSelectorValues(pointsData)
            }

            // If any of the selectors is selected, then user must be able to move his finger anywhere
            // on the screen and still have control of the selected selector.
            if (bothSelectorsSelected || minSelectorSelected || maxSelectorSelected) {
                parent.requestDisallowInterceptTouchEvent(true)
            }

            return true
        }
                ?: return false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        // draw X axis line
        canvas.drawRect(xAxisRect, xAxisRectPaint)
        canvas.drawRect(xAxisFirstPointRect, xAxisRectPaint)
        canvas.drawRect(xAxisLastPointRect, xAxisRectPaint)
        for (xAxisMiddlePointsRect in xAxisMiddlePointsRects) {
            canvas.drawRect(xAxisMiddlePointsRect, xAxisRectPaint)
        }

        if (hasData()) {
            drawDataViewPart(canvas)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.pointsData = pointsData
        savedState.minSelectorValue = minSelectorValue
        savedState.maxSelectorValue = maxSelectorValue
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
                    refreshGraphValues(pointsData, state.minSelectorValue, state.maxSelectorValue)
                }
                ?: refreshGraphValues(state.pointsData)
        listenersEnabled = true

        super.onRestoreInstanceState(state.superState)
    }

    private fun drawDataViewPart(canvas: Canvas) {
        canvas.save()
        canvas.scale(1f, graphYAxisScaleFactor, 0f, graphBottom)
        canvas.clipRect(graphBoundsRect)
        canvas.drawPath(graphPath, graphPaint)
        canvas.clipRect(selectedGraphBoundsRect)
        canvas.drawPath(graphPath, selectedGraphPaint)
        canvas.restore()

        // draw selected line and selectors
        canvas.drawRect(selectedLine, selectedLinePaint)
        canvas.drawOval(minSelector, selectorPaint)
        canvas.drawOval(maxSelector, selectorPaint)

        for (i in 0 until lineMiddlePointsNum + 2) {
            canvas.drawText(
                    String.format("%.0f", numbers[i]),
                    numbersPositions[i],
                    numbersYPosition,
                    textPaint)
        }
    }

    private fun hasData(): Boolean {
        return pointsData != null
    }

    private fun setMinSelectorXPosition(x: Float, animated: Boolean = false) {

        fun setMinSelectorShapes(x: Float) {
            selectedGraphBoundsRect.left = x
            selectedLine.left = x
            minSelector.setXMiddle(x)
            minSelectorTouchField.setXMiddle(x)
        }

        if (animated) {
            minSelectorAnimator.setFloatValues(minSelector.getXPosition(), x)
            minSelectorAnimator.addUpdateListener { animation ->
                setMinSelectorShapes(animation.animatedValue as Float)
                invalidate()
            }
            minSelectorAnimator.start()
        } else {
            setMinSelectorShapes(x)
            invalidate()
        }
    }

    private fun setMaxSelectorXPosition(x: Float, animated: Boolean = false) {

        fun setMaxSelectorShapes(x: Float) {
            selectedGraphBoundsRect.right = x
            selectedLine.right = x
            maxSelector.setXMiddle(x)
            maxSelectorTouchField.setXMiddle(x)
        }

        if (animated) {
            maxSelectorAnimator.setFloatValues(maxSelector.getXPosition(), x)
            maxSelectorAnimator.addUpdateListener { animation ->
                setMaxSelectorShapes(animation.animatedValue as Float)
                invalidate()
            }
            maxSelectorAnimator.start()
        } else {
            setMaxSelectorShapes(x)
            invalidate()
        }
    }

    private fun updateSelectorValues(pointsData: PointsData) {
        minSelectorValue = affineTransformXToY(minSelector.getXPosition(), graphLeft, graphRight, pointsData.minX, pointsData.maxX)
        maxSelectorValue = affineTransformXToY(maxSelector.getXPosition(), graphLeft, graphRight, pointsData.minX, pointsData.maxX)
    }

    private fun refreshGraphValues(newPointsData: PointsData?) {
        pointsData = newPointsData

        pointsData?.let { pointsData ->
            refreshGraphValues(pointsData, pointsData.minX, pointsData.maxX)
        }
    }

    private fun refreshGraphValues(pointsData: PointsData, minSelectorValue: Float, maxSelectorValue: Float) {
        this.pointsData = pointsData

        for (i in numbers.indices) {
            numbers[i] = i * pointsData.xRange / (numbers.size - 1) + pointsData.minX
        }

        this.minSelectorValue = minSelectorValue
        this.maxSelectorValue = maxSelectorValue

        graphPath.generatePath(pointsData, graphLeft, graphBottom, graphRight, graphTop)
        refreshSelectorsPositions(pointsData)
    }

    private fun refreshSelectorsPositions(pointsData: PointsData) {
        setMinSelectorXPosition(affineTransformXToY(minSelectorValue, pointsData.minX, pointsData.maxX, graphLeft, graphRight))
        setMaxSelectorXPosition(affineTransformXToY(maxSelectorValue, pointsData.minX, pointsData.maxX, graphLeft, graphRight))
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

    private fun ArrayList<MinSelectorPositionChangeListener>.dispatchOnMinSelectorPositionChangeEvent(newMinValue: Float) {
        this.forEach { it.onMinValueChanged(newMinValue) }
    }

    private fun ArrayList<MaxSelectorPositionChangeListener>.dispatchOnMaxSelectorPositionChangeEvent(newMaxValue: Float) {
        this.forEach { it.onMaxValueChanged(newMaxValue) }
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