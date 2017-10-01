package com.johnnym.lagrange

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.collections.ArrayList

/**
 * Draws a La Graph of Range count values in form of a spline.
 */
class LaGrange @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {
        // LaGrange view settings
        private val VIEW_HEIGHT: Int = 150
        private val LINE_THICKNESS: Int = 3
        private val SELECTED_LINE_THICKNESS: Int = 5
        private val GRAPH_TOP_DRAW_POSITION: Int = 0
        private val GRAPH_TOP_POSITION: Int = 20
        private val LINE_Y_POSITION: Int = 100
        private val NUMBERS_Y_POSITION: Int = 130
        private val X_AXIS_LEFT_RIGHT_PADDING: Int = 16
        private val POINT_INDICATOR_LENGTH: Int = 11
        private val SELECTOR_DIAMETER: Int = 30
        private val SELECTOR_TOUCH_DIAMETER: Int = 60
        private val BAR_GRAPH_SHOWN: Boolean = true
        private val ANIMATE_SELECTOR_CHANGES: Boolean = true
    }

    // Constant graph values
    private val lineColor: Int
    private val selectorsConnectLineColor: Int
    private val selectorColor: Int
    private val selectorBorderColor: Int
    private val textColor: Int
    private val graphColor: Int
    private val selectedGraphColor: Int
    private val lineMiddlePointsNum: Int
    private val barGraphColor: Int

    private val viewHeight: Float
    private val lineThickness: Float
    private val selectedLineThickness: Float
    private val graphTopDrawPosition: Float
    private val graphTopPosition: Float
    private val lineYPosition: Float
    private val numbersYPosition: Float
    private val xAxisLeftRightPadding: Float
    private val pointIndicatorLength: Float
    private val selectorDiameter: Float
    private val selectorTouchDiameter: Float
    private val showBarGraph: Boolean
    private val animateSelectorChanges: Boolean

    // Graph drawing objects
    private val xAxisRectPaint: Paint
    private val textPaint: TextPaint
    private val selectorPaint: Paint
    private val selectorBorderPaint: Paint
    private val selectorsConnectLinePaint: Paint
    private val graphPaint: Paint
    private val selectedGraphPaint: Paint
    private val barGraphPaint: Paint

    private val minSelector: RectF
    private val maxSelector: RectF
    private val minSelectorTouchField: RectF
    private val maxSelectorTouchField: RectF
    private val xAxisRect: RectF
    private val xAxisFirstPointRect: RectF
    private val xAxisLastPointRect: RectF
    private val xAxisMiddlePointsRects: List<RectF>
    private val graphBoundsRect: RectF
    private val selectedGraphBoundsRect: RectF
    private val selectorConnectLine: RectF
    private val splineGraphPath: Path
    private val barGraphPath: Path
    private val numbers: LongArray
    private val minSelectorAnimator: ValueAnimator
    private val maxSelectorAnimator: ValueAnimator

    // View's dimensions and sizes, positions etc.
    private var viewStartX: Float = 0f
    private var viewEndX: Float = 0f
    private var viewStartY: Float = 0f
    private var viewEndY: Float = 0f
    private var pointsDistance: Float = 0f
    private var numbersPositions: FloatArray
    private var graphMinXPosition: Float = 0f
    private var graphMaxXPosition: Float = 0f
    private var selectorsYPosition: Float = 0f

    // Touch event variables
    private var actionDownXValue: Float = 0f
    private var actionDownYValue: Float = 0f
    private var actionMoveXValue: Float = 0f
    private var minSelectorSelected: Boolean = false
    private var maxSelectorSelected: Boolean = false
    private var bothSelectorsSelected: Boolean = false

    // Data variables
    private var rangeData: RangeData? = null
    private val minSelectorPositionChangeListeners = ArrayList<MinSelectorPositionChangeListener>()
    private val maxSelectorPositionChangeListeners = ArrayList<MaxSelectorPositionChangeListener>()

    // True selectors values (set from outside by setters or by touch events)
    private var minSelectorValue: Long = 0
    private var maxSelectorValue: Long = 0

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.sgrs__LaGrange, defStyleAttr, 0)
        try {
            lineColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__line_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_line_color))
            selectorsConnectLineColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__selectors_connect_line_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_selectors_connect_line_color))
            selectorColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__selector_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_selector_color))
            selectorBorderColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__selector_border_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_selector_border_color))
            textColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__text_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_text_color))
            graphColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__graph_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_graph_color))
            selectedGraphColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__selected_graph_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_selected_graph_color))
            lineMiddlePointsNum = attributes.getInteger(
                    R.styleable.sgrs__LaGrange_sgrs__line_middle_points_num,
                    resources.getInteger(R.integer.sgrs__default_line_middle_points_num))
            barGraphColor = attributes.getColor(
                    R.styleable.sgrs__LaGrange_sgrs__bar_graph_color,
                    ContextCompat.getColor(getContext(), R.color.sgrs__default_bar_graph_color))
        } finally {
            attributes.recycle()
        }

        // Calculate view dimensions from the given attributes
        viewHeight = convertDpToPixel(VIEW_HEIGHT.toFloat(), context)
        lineThickness = convertDpToPixel(LINE_THICKNESS.toFloat(), context)
        selectedLineThickness = convertDpToPixel(SELECTED_LINE_THICKNESS.toFloat(), context)
        graphTopDrawPosition = convertDpToPixel(GRAPH_TOP_DRAW_POSITION.toFloat(), context)
        graphTopPosition = convertDpToPixel(GRAPH_TOP_POSITION.toFloat(), context)
        lineYPosition = convertDpToPixel(LINE_Y_POSITION.toFloat(), context)
        numbersYPosition = convertDpToPixel(NUMBERS_Y_POSITION.toFloat(), context)
        xAxisLeftRightPadding = convertDpToPixel(X_AXIS_LEFT_RIGHT_PADDING.toFloat(), context)
        pointIndicatorLength = convertDpToPixel(POINT_INDICATOR_LENGTH.toFloat(), context)
        selectorDiameter = convertDpToPixel(SELECTOR_DIAMETER.toFloat(), context)
        selectorTouchDiameter = convertDpToPixel(SELECTOR_TOUCH_DIAMETER.toFloat(), context)
        showBarGraph = BAR_GRAPH_SHOWN
        animateSelectorChanges = ANIMATE_SELECTOR_CHANGES

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

        xAxisRect = RectF()
        xAxisFirstPointRect = RectF()
        xAxisLastPointRect = RectF()
        xAxisMiddlePointsRects = List(lineMiddlePointsNum) { RectF() }
        numbers = LongArray(lineMiddlePointsNum + 2)

        graphBoundsRect = RectF()
        selectedGraphBoundsRect = RectF()
        selectorConnectLine = RectF(
                0f,
                0f,
                0f,
                selectedLineThickness)

        splineGraphPath = Path()
        barGraphPath = Path()

        minSelectorAnimator = ValueAnimator()
        minSelectorAnimator.duration = 150
        maxSelectorAnimator = ValueAnimator()
        maxSelectorAnimator.duration = 150

        // Init draw settings
        xAxisRectPaint = Paint()
        xAxisRectPaint.isAntiAlias = true
        xAxisRectPaint.color = lineColor
        xAxisRectPaint.style = Paint.Style.FILL

        textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = convertDpToPixel(12f, context)

        selectorPaint = Paint()
        selectorPaint.isAntiAlias = true
        selectorPaint.style = Paint.Style.FILL
        selectorPaint.color = selectorColor

        selectorBorderPaint = Paint()
        selectorBorderPaint.isAntiAlias = true
        selectorBorderPaint.style = Paint.Style.STROKE
        selectorBorderPaint.color = selectorBorderColor
        selectorBorderPaint.strokeWidth = convertDpToPixel(2f, context)

        selectorsConnectLinePaint = Paint()
        selectorsConnectLinePaint.isAntiAlias = true
        selectorsConnectLinePaint.style = Paint.Style.FILL
        selectorsConnectLinePaint.color = selectorsConnectLineColor

        graphPaint = Paint()
        graphPaint.isAntiAlias = true
        graphPaint.color = graphColor
        graphPaint.style = Paint.Style.FILL

        selectedGraphPaint = Paint()
        selectedGraphPaint.isAntiAlias = true
        selectedGraphPaint.color = selectedGraphColor
        selectedGraphPaint.style = Paint.Style.FILL

        barGraphPaint = Paint()
        barGraphPaint.isAntiAlias = true
        barGraphPaint.color = barGraphColor
        barGraphPaint.style = Paint.Style.STROKE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.resolveSizeAndState(viewHeight.toInt() + paddingTop + paddingBottom, heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewStartX = paddingLeft.toFloat()
        viewEndX = (w - paddingRight).toFloat()
        viewStartY = paddingTop.toFloat()
        viewEndY = (h - paddingBottom).toFloat()

        xAxisRect.set(viewStartX + xAxisLeftRightPadding, lineYPosition, viewEndX - xAxisLeftRightPadding, lineYPosition + lineThickness)
        xAxisFirstPointRect.set(xAxisRect.left, xAxisRect.top, xAxisRect.left + lineThickness, xAxisRect.top + pointIndicatorLength)
        xAxisLastPointRect.set(xAxisRect.right - lineThickness, xAxisRect.bottom - pointIndicatorLength, xAxisRect.right, xAxisRect.bottom)

        // Caculate X axis number positions
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

        graphMinXPosition = xAxisRect.left + lineThickness / 2
        graphMaxXPosition = xAxisRect.right - lineThickness / 2

        graphBoundsRect.set(graphMinXPosition, graphTopDrawPosition, graphMaxXPosition, lineYPosition)
        selectedGraphBoundsRect.set(graphBoundsRect)

        selectorsYPosition = (xAxisRect.top + xAxisRect.bottom) / 2
        minSelector.setYMiddle(selectorsYPosition)
        minSelectorTouchField.setYMiddle(selectorsYPosition)
        maxSelector.setYMiddle(selectorsYPosition)
        maxSelectorTouchField.setYMiddle(selectorsYPosition)
        selectorConnectLine.setYMiddle(selectorsYPosition)

        rangeData?.let { rangeData ->
            generateSplineGraphPath(rangeData)
            generateBarGraphPath(rangeData)
            refreshSelectorsPositions(rangeData)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }

        rangeData?.let { rangeData ->
            val action = event.actionMasked

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    actionDownXValue = event.x
                    actionDownYValue = event.y
                    if (minSelectorTouchField.contains(actionDownXValue, actionDownYValue)
                            && maxSelectorTouchField.contains(actionDownXValue, actionDownYValue)) {
                        // variables that mark whether the touch is close to min/max selector center
                        var minCenterIsClose = false
                        var maxCenterIsClose = false
                        if (Math.abs(minSelectorTouchField.centerX() - actionDownXValue) < 0.2 * minSelectorTouchField.width()) {
                            minCenterIsClose = true
                        }
                        if (Math.abs(maxSelectorTouchField.centerX() - actionDownXValue) < 0.2 * maxSelectorTouchField.width()) {
                            maxCenterIsClose = true
                        }
                        if (minCenterIsClose && !maxCenterIsClose) {
                            minSelectorSelected = true
                        } else if (!minCenterIsClose && maxCenterIsClose) {
                            maxSelectorSelected = true
                        } else {
                            bothSelectorsSelected = true
                        }
                    } else if (minSelectorTouchField.contains(actionDownXValue, actionDownYValue)) {
                        minSelectorSelected = true
                    } else if (maxSelectorTouchField.contains(actionDownXValue, actionDownYValue)) {
                        maxSelectorSelected = true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    actionMoveXValue = event.x
                    if (bothSelectorsSelected) {
                        if (Math.abs(actionMoveXValue - actionDownXValue) > convertDpToPixel(1f, context)) {
                            if (actionMoveXValue < actionDownXValue) {
                                bothSelectorsSelected = false
                                minSelectorSelected = true
                            } else if (actionMoveXValue > actionDownXValue) {
                                bothSelectorsSelected = false
                                maxSelectorSelected = true
                            }
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
                if (newXPosition < graphMinXPosition) {
                    newXPosition = graphMinXPosition
                } else if (newXPosition > maxSelector.getXPosition()) {
                    newXPosition = maxSelector.getXPosition()
                }
                setMinSelectorXPosition(newXPosition, false)
                dispatchOnMinSelectorPositionChanged(rangeData)
            } else if (maxSelectorSelected) {
                if (newXPosition > graphMaxXPosition) {
                    newXPosition = graphMaxXPosition
                } else if (newXPosition < minSelector.getXPosition()) {
                    newXPosition = minSelector.getXPosition()
                }
                setMaxSelectorXPosition(newXPosition, false)
                dispatchOnMaxSelectorPositionChanged(rangeData)
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
        savedState.rangeData = rangeData
        savedState.minSelectorValue = minSelectorValue
        savedState.maxSelectorValue = maxSelectorValue
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        state.rangeData
                ?.let { rangeData ->
                    refreshGraphValues(rangeData, state.minSelectorValue, state.maxSelectorValue)
                }
                ?: refreshGraphValues(state.rangeData)

        super.onRestoreInstanceState(state.superState)
    }

    private fun drawDataViewPart(canvas: Canvas) {
        canvas.save()
        canvas.clipRect(graphBoundsRect)
        canvas.drawPath(splineGraphPath, graphPaint)
        canvas.clipRect(selectedGraphBoundsRect)
        canvas.drawPath(splineGraphPath, selectedGraphPaint)
        canvas.restore()

        if (showBarGraph) {
            canvas.save()
            canvas.clipRect(graphBoundsRect)
            canvas.drawPath(barGraphPath, barGraphPaint)
            canvas.restore()
        }

        // draw selectors connect line and selectors
        canvas.drawRect(selectorConnectLine, selectorsConnectLinePaint)
        canvas.drawOval(minSelector, selectorPaint)
        canvas.drawOval(minSelector, selectorBorderPaint)
        canvas.drawOval(maxSelector, selectorPaint)
        canvas.drawOval(maxSelector, selectorBorderPaint)

        for (i in 0 until lineMiddlePointsNum + 2) {
            canvas.drawText(numbers[i].toString(),
                    numbersPositions[i],
                    numbersYPosition,
                    textPaint)
        }
    }

    private fun hasData(): Boolean {
        return rangeData != null
    }

    private fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    private fun RectF.setMiddle(x: Float, y: Float) {
        this.setXMiddle(x)
        this.setYMiddle(y)
    }

    private fun RectF.setXMiddle(x: Float) {
        this.offsetTo(x - this.width() / 2, this.top)
    }

    private fun RectF.setYMiddle(y: Float) {
        this.offsetTo(this.left, y - this.width() / 2)
    }

    private fun RectF.getXPosition(): Float {
        return this.centerX()
    }

    private fun setMinSelectorXPosition(x: Float, animated: Boolean) {

        fun setMinSelectorShapes(x: Float) {
            selectedGraphBoundsRect.left = x
            selectorConnectLine.left = x
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

    private fun setMaxSelectorXPosition(x: Float, animated: Boolean) {

        fun setMaxSelectorShapes(x: Float) {
            selectedGraphBoundsRect.right = x
            selectorConnectLine.right = x
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

    private fun dispatchOnMinSelectorPositionChanged(rangeData: RangeData) {
        updateSelectorValues(rangeData)
        for (minSelectorPositionChangeListener in minSelectorPositionChangeListeners) {
            minSelectorPositionChangeListener.onMinValueChanged(minSelectorValue)
        }
    }

    private fun dispatchOnMaxSelectorPositionChanged(rangeData: RangeData) {
        updateSelectorValues(rangeData)
        for (maxSelectorPositionChangeListener in maxSelectorPositionChangeListeners) {
            maxSelectorPositionChangeListener.onMaxValueChanged(maxSelectorValue)
        }
    }

    interface MinSelectorPositionChangeListener {
        fun onMinValueChanged(newMinValue: Long)
    }

    interface MaxSelectorPositionChangeListener {
        fun onMaxValueChanged(newMaxValue: Long)
    }

    private fun updateSelectorValues(rangeData: RangeData) {
        minSelectorValue = getXValueFromXGraphPosition(rangeData, minSelector.getXPosition()).toLong()
        maxSelectorValue = getXValueFromXGraphPosition(rangeData, maxSelector.getXPosition()).toLong()
    }

    private fun getXValueFromXGraphPosition(rangeData: RangeData, x: Float): Float {
        return (x - graphMinXPosition) * rangeData.xRange / (graphMaxXPosition - graphMinXPosition) + rangeData.minX
    }

    private fun getXGraphPositionFromXValue(rangeData: RangeData, x: Float): Float {
        return (graphMaxXPosition - graphMinXPosition) / rangeData.xRange * (x - rangeData.minX) + graphMinXPosition
    }

    private fun getYGraphPositionFromYValue(rangeData: RangeData, y: Float): Float {
        return (lineYPosition - graphTopPosition) / (rangeData.maxY - rangeData.minY) * (rangeData.maxY - y) + graphTopPosition
    }

    fun setRangeData(rangeData: RangeData?) {
        refreshGraphValues(rangeData)
        invalidate()
    }

    private fun refreshGraphValues(rangeData: RangeData?) {
        this.rangeData = rangeData

        // calculate RangeData data
        rangeData?.let { rangeDataNotNull ->
            refreshGraphValues(rangeDataNotNull, rangeDataNotNull.minX, rangeDataNotNull.maxX)
        }
    }

    private fun refreshGraphValues(rangeData: RangeData, minSelectorValue: Long, maxSelectorValue: Long) {
        this.rangeData = rangeData

        for (i in numbers.indices) {
            numbers[i] = i * rangeData.xRange / (numbers.size - 1) + rangeData.minX
        }

        this.minSelectorValue = minSelectorValue
        this.maxSelectorValue = maxSelectorValue

        generateSplineGraphPath(rangeData)
        generateBarGraphPath(rangeData)
        refreshSelectorsPositions(rangeData)
    }

    private fun generateSplineGraphPath(rangeData: RangeData) {
        val knotsArr = getGraphPointsFromRangeData(rangeData)
        val (firstCP, secondCP) = BezierSplineUtil.getCurveControlPoints(knotsArr)
        splineGraphPath.reset()
        // move to the start of the graph
        splineGraphPath.moveTo(graphMinXPosition, lineYPosition)
        splineGraphPath.lineTo(knotsArr[0].x, knotsArr[0].y)
        for (i in firstCP.indices) {
            splineGraphPath.cubicTo(firstCP[i].x, firstCP[i].y,
                    secondCP[i].x, secondCP[i].y,
                    knotsArr[i + 1].x, knotsArr[i + 1].y)
        }
        // move to the end of the graph
        splineGraphPath.lineTo(graphMaxXPosition, lineYPosition)
    }

    private fun generateBarGraphPath(rangeData: RangeData) {
        barGraphPath.reset()
        barGraphPath.moveTo(
                getXGraphPositionFromXValue(rangeData, rangeData.minX.toFloat()),
                getYGraphPositionFromYValue(rangeData, 0f))

        for ((from, to, count) in rangeData.rangeList) {
            barGraphPath.lineTo(
                    getXGraphPositionFromXValue(rangeData, from.toFloat()),
                    getYGraphPositionFromYValue(rangeData, count.toFloat()))
            barGraphPath.lineTo(
                    getXGraphPositionFromXValue(rangeData, to.toFloat()),
                    getYGraphPositionFromYValue(rangeData, count.toFloat()))
        }

        barGraphPath.lineTo(
                getXGraphPositionFromXValue(rangeData, rangeData.maxX.toFloat()),
                getYGraphPositionFromYValue(rangeData, 0f))
    }

    private fun refreshSelectorsPositions(rangeData: RangeData) {
        setMinSelectorXPosition(getXGraphPositionFromXValue(rangeData, minSelectorValue.toFloat()), false)
        setMaxSelectorXPosition(getXGraphPositionFromXValue(rangeData, maxSelectorValue.toFloat()), false)
    }

    private fun getGraphPointsFromRangeData(rangeData: RangeData): List<Point> {
        var rangeDataX: Float
        var rangeDataY: Float
        var x: Float
        var y: Float

        val rangeList = rangeData.rangeList

        val points = ArrayList<Point>()

        // Calculate and add first point
        // (lets say its value is the half of the first data point)
        rangeDataX = rangeList[0].from.toFloat()
        rangeDataY = (rangeList[0].count / 2).toFloat()
        x = getXGraphPositionFromXValue(rangeData, rangeDataX)
        y = getYGraphPositionFromYValue(rangeData, rangeDataY)

        points.add(Point(x, y))

        // Calculate and add middle points
        val middlePoints = Array(rangeList.size) {
            val rangeDataXValue = rangeList[it].middle.toFloat()
            val rangeDataYValue = rangeList[it].count.toFloat()
            x = getXGraphPositionFromXValue(rangeData, rangeDataXValue)
            y = getYGraphPositionFromYValue(rangeData, rangeDataYValue)
            Point(x, y)
        }
        points.addAll(middlePoints)

        // Calculate and add last point
        // (lets say its value is the half of the last data point)
        rangeDataX = rangeList[rangeList.size - 1].to.toFloat()
        rangeDataY = (rangeList[rangeList.size - 1].count / 2).toFloat()
        x = getXGraphPositionFromXValue(rangeData, rangeDataX)
        y = getYGraphPositionFromYValue(rangeData, rangeDataY)
        points[points.size - 1] = Point(x, y)

        return points
    }

    fun addMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        minSelectorPositionChangeListeners.add(minSelectorPositionChangeListener)
    }

    fun addMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        maxSelectorPositionChangeListeners.add(maxSelectorPositionChangeListener)
    }

    class SavedState : BaseSavedState {

        var rangeData: RangeData? = null
        var minSelectorValue: Long = 0
        var maxSelectorValue: Long = 0

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeParcelable(rangeData, flags)
            out.writeLong(minSelectorValue)
            out.writeLong(maxSelectorValue)
        }

        private constructor(parcel: Parcel) : super(parcel) {
            rangeData = parcel.readParcelable(RangeData.javaClass.classLoader)
            minSelectorValue = parcel.readLong()
            maxSelectorValue = parcel.readLong()
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