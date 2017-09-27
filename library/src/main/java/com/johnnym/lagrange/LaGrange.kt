package com.johnnym.lagrange

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
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

        // Default settings
        private val LINE_COLOR : Int = 0xFFE0E0E0.toInt()
        private val SELECTORS_CONNECT_LINE_COLOR : Int = 0xFF1787AE.toInt()
        private val SELECTOR_COLOR : Int = 0xFFFFFFFF.toInt()
        private val SELECTOR_BORDER_COLOR : Int = 0xFF1787AE.toInt()
        private val TEXT_COLOR : Int = 0xFF1787AE.toInt()
        private val GRAPH_COLOR : Int = 0xFFE0E0E0.toInt()
        private val SELECTED_GRAPH_COLOR : Int = 0xFFC4E1EB.toInt()
        private val LINE_MIDDLE_POINTS_NUM : Int = 4
        private val BAR_GRAPH_COLOR : Int = Color.RED

        // LaGrange view settings
        private val VIEW_HEIGHT : Int = 150
        private val LINE_THICKNESS : Int = 3
        private val SELECTED_LINE_THICKNESS : Int = 5
        private val GRAPH_TOP_DRAW_POSITION : Int = 0
        private val GRAPH_TOP_POSITION : Int = 20
        private val LINE_Y_POSITION : Int = 100
        private val NUMBERS_Y_POSITION : Int = 130
        private val X_AXIS_LEFT_RIGHT_PADDING : Int = 16
        private val POINT_INDICATOR_LENGTH : Int = 11
        private val SELECTOR_DIAMETER : Int = 30
        private val SELECTOR_TOUCH_DIAMETER : Int = 60
        private val BAR_GRAPH_SHOWN : Boolean = false
        private val ANIMATE_SELECTOR_CHANGES : Boolean = true
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
    private var splineGraphPath: Path
    private var barGraphPath: Path
    private var numbers: LongArray
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
    private var rangeDataMinX: Long = 0
    private var rangeDataMaxX: Long = 0
    private var rangeDataMinY: Long = 0
    private var rangeDataMaxY: Long = 0
    private var rangeDataXRange: Long = 0

    // True selectors values (set from outside by setters or by touch events)
    private var minSelectorValue : Long = 0
    private var maxSelectorValue : Long = 0

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.LaGrange, defStyleAttr, 0)
        try {
            lineColor = attributes.getColor(R.styleable.LaGrange_line_color, LINE_COLOR)
            selectorsConnectLineColor = attributes.getColor(R.styleable.LaGrange_selectors_connect_line_color, SELECTORS_CONNECT_LINE_COLOR)
            selectorColor = attributes.getColor(R.styleable.LaGrange_selector_color, SELECTOR_COLOR)
            selectorBorderColor = attributes.getColor(R.styleable.LaGrange_selector_border_color, SELECTOR_BORDER_COLOR)
            textColor = attributes.getColor(R.styleable.LaGrange_text_color, TEXT_COLOR)
            graphColor = attributes.getColor(R.styleable.LaGrange_graph_color, GRAPH_COLOR)
            selectedGraphColor = attributes.getColor(R.styleable.LaGrange_selected_graph_color, SELECTED_GRAPH_COLOR)
            lineMiddlePointsNum = attributes.getInteger(R.styleable.LaGrange_line_middle_points_num, LINE_MIDDLE_POINTS_NUM)
            barGraphColor = attributes.getColor(R.styleable.LaGrange_bar_graph_color, BAR_GRAPH_COLOR)
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
        xAxisMiddlePointsRects = List(lineMiddlePointsNum) {RectF()}
        numbers = LongArray(lineMiddlePointsNum + 2)

        graphBoundsRect = RectF()
        selectedGraphBoundsRect = RectF()
        selectorConnectLine = RectF()
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

        val selectorsYPosition = (xAxisRect.top + xAxisRect.bottom) / 2
        minSelector.setXMiddle(graphMinXPosition)
        minSelector.setYMiddle(selectorsYPosition)
        minSelectorTouchField.setXMiddle(graphMinXPosition)
        minSelectorTouchField.setYMiddle(selectorsYPosition)
        maxSelector.setXMiddle(graphMaxXPosition)
        maxSelector.setYMiddle(selectorsYPosition)
        maxSelectorTouchField.setXMiddle(graphMaxXPosition)
        maxSelectorTouchField.setYMiddle(selectorsYPosition)

        selectorConnectLine.set(xAxisRect)
        val linesThicknessDifference = (selectedLineThickness - lineThickness) / 2
        selectorConnectLine.top -= linesThicknessDifference
        selectorConnectLine.bottom += linesThicknessDifference
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || this.rangeData == null) {
            return false
        }

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
            dispatchOnMinSelectorPositionChanged()
        } else if (maxSelectorSelected) {
            if (newXPosition > graphMaxXPosition) {
                newXPosition = graphMaxXPosition
            } else if (newXPosition < minSelector.getXPosition()) {
                newXPosition = minSelector.getXPosition()
            }
            setMaxSelectorXPosition(newXPosition, false)
            dispatchOnMaxSelectorPositionChanged()
        }

        if (bothSelectorsSelected || minSelectorSelected || maxSelectorSelected) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        return true
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

    private fun dispatchOnMinSelectorPositionChanged() {
        updateSelectorValues()
        for (minSelectorPositionChangeListener in minSelectorPositionChangeListeners) {
            minSelectorPositionChangeListener.onMinValueChanged(minSelectorValue)
        }
    }

    private fun dispatchOnMaxSelectorPositionChanged() {
        updateSelectorValues()
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

    private fun updateSelectorValues() {
        minSelectorValue = getXValueFromXGraphPosition(minSelector.getXPosition()).toLong()
        maxSelectorValue = getXValueFromXGraphPosition(maxSelector.getXPosition()).toLong()
    }

    private fun getXValueFromXGraphPosition(x: Float): Float {
        return (x - graphMinXPosition) * rangeDataXRange / (graphMaxXPosition - graphMinXPosition) + rangeDataMinX
    }

    private fun getXGraphPositionFromXValue(x: Float): Float {
        return (graphMaxXPosition - graphMinXPosition) / rangeDataXRange * (x - rangeDataMinX) + graphMinXPosition
    }

    private fun getYGraphPositionFromYValue(y: Float): Float {
        return (lineYPosition - graphTopPosition) / (rangeDataMaxY - rangeDataMinY) * (rangeDataMaxY - y) + graphTopPosition
    }

    fun setRangeData(rangeData: RangeData?) {
        this.rangeData = rangeData

        if (this.rangeData == null) {
            return
        } else {
            setGraphValues()
        }
    }

    private fun setGraphValues() {
        rangeDataMinX = this.rangeData!!.rangeList[0].from
        rangeDataMaxX = this.rangeData!!.rangeList[this.rangeData!!.rangeList.size - 1].to
        rangeDataXRange = rangeDataMaxX - rangeDataMinX
        rangeDataMinY = 0
        rangeDataMaxY = 0
        this.rangeData!!.rangeList
                .asSequence()
                .filter { rangeDataMaxY < it.count }
                .forEach { rangeDataMaxY = it.count }

        numbers = LongArray(lineMiddlePointsNum + 2)
        for (i in numbers.indices) {
            numbers[i] = i * rangeDataXRange / (numbers.size - 1) + rangeDataMinX
        }

        generateBarGraphPath()

        val knotsArr = getGraphPointsFromRangeList(this.rangeData!!.rangeList)
        val (firstCP, secondCP) = BezierSplineUtil.getCurveControlPoints(knotsArr)
        splineGraphPath = Path()
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

        minSelectorValue = rangeDataMinX
        maxSelectorValue = rangeDataMaxX

        invalidate()
    }

    private fun generateBarGraphPath() {
        barGraphPath.reset()
        barGraphPath.moveTo(
                getXGraphPositionFromXValue(rangeDataMinX.toFloat()),
                getYGraphPositionFromYValue(0f))

        for ((from, to, count) in rangeData!!.rangeList) {
            barGraphPath.lineTo(
                    getXGraphPositionFromXValue(from.toFloat()),
                    getYGraphPositionFromYValue(count.toFloat()))
            barGraphPath.lineTo(
                    getXGraphPositionFromXValue(to.toFloat()),
                    getYGraphPositionFromYValue(count.toFloat()))
        }

        barGraphPath.lineTo(
                getXGraphPositionFromXValue(rangeDataMaxX.toFloat()),
                getYGraphPositionFromYValue(0f))
    }

    private fun getGraphPointsFromRangeList(rangeList: ArrayList<Range>): ArrayList<Point> {
        var rangeDataX: Float
        var rangeDataY: Float
        var x: Float
        var y: Float

        val points = ArrayList<Point>()

        // Calculate and add first point
        // (lets say its value is the half of the first data point)
        rangeDataX = rangeDataMinX.toFloat()
        rangeDataY = (rangeList[0].count / 2).toFloat()
        x = getXGraphPositionFromXValue(rangeDataX)
        y = getYGraphPositionFromYValue(rangeDataY)

        points.add(Point(x, y))

        // Calculate and add middle points
        val middlePoints = Array(rangeList.size) {
            val rangeDataXValue = rangeList[it].middle.toFloat()
            val rangeDataYValue = rangeList[it].count.toFloat()
            x = getXGraphPositionFromXValue(rangeDataXValue)
            y = getYGraphPositionFromYValue(rangeDataYValue)
            Point(x, y)
        }
        points.addAll(middlePoints)

        // Calculate and add last point
        // (lets say its value is the half of the last data point)
        rangeDataX = rangeDataMaxX.toFloat()
        rangeDataY = (rangeList[rangeList.size - 1].count / 2).toFloat()
        x = getXGraphPositionFromXValue(rangeDataX)
        y = getYGraphPositionFromYValue(rangeDataY)
        points[points.size - 1] = Point(x, y)

        return points
    }

    fun addMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        minSelectorPositionChangeListeners.add(minSelectorPositionChangeListener)
    }

    fun addMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        maxSelectorPositionChangeListeners.add(maxSelectorPositionChangeListener)
    }
}