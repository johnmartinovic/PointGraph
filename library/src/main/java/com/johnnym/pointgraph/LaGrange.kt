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
import com.johnnym.pointgraph.utils.convertDpToPixel
import com.johnnym.pointgraph.utils.getXPosition
import com.johnnym.pointgraph.utils.setXMiddle
import com.johnnym.pointgraph.utils.setYMiddle
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

/**
 * Draws a Graph of Points in form of a spline.
 */
class LaGrange @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    companion object {
        // LaGrange view settings
        private val MIN_VIEW_WIDTH: Int = 200
        private val MIN_VIEW_HEIGHT: Int = 150
        private val GRAPH_TOP_DRAW_POSITION_FROM_TOP: Int = 0
        private val GRAPH_TOP_POSITION_FROM_TOP: Int = 20
        private val LINE_Y_POSITION_FROM_BOTTOM: Int = 50
        private val NUMBERS_Y_POSITION_FROM_BOTTOM: Int = 20
        private val X_AXIS_LEFT_RIGHT_PADDING: Int = 16
        private val POINT_INDICATOR_LENGTH: Int = 11
        private val SELECTOR_DIAMETER: Int = 30
        private val SELECTOR_TOUCH_DIAMETER: Int = 60
    }

    // Constant graph values
    private val lineColor: Int
    private val lineThickness: Float
    private val selectedLineColor: Int
    private val selectedLineThickness: Float
    private val selectorColor: Int
    private val selectorBorderColor: Int
    private val textColor: Int
    private val graphColor: Int
    private val selectedGraphColor: Int
    private val lineMiddlePointsNum: Int
    private val animateSelectorChanges: Boolean

    private val minViewWidth: Float
    private val minViewHeight: Float
    private val graphTopDrawPositionFromTop: Float
    private val graphTopPositionFromTop: Float
    private val lineYPositionFromBottom: Float
    private val numbersYPositionFromBottom: Float
    private val xAxisLeftRightPadding: Float
    private val pointIndicatorLength: Float
    private val selectorDiameter: Float
    private val selectorTouchDiameter: Float

    // Graph drawing objects
    private val xAxisRectPaint: Paint
    private val textPaint: TextPaint
    private val selectorPaint: Paint
    private val selectorBorderPaint: Paint
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
    private val graphBoundsRect: RectF
    private val selectedGraphBoundsRect: RectF
    private val selectedLine: RectF
    private val splineGraphPath: Path
    private val numbers: FloatArray
    private val minSelectorAnimator: ValueAnimator
    private val maxSelectorAnimator: ValueAnimator
    private val graphScaleAnimator: ValueAnimator
    private var graphYAxisScaleFactor: Float = 1f

    // View's dimensions and sizes, positions etc.
    private var graphTopDrawYPosition: Float = 0f
    private var graphTopYPosition: Float = 0f
    private var lineYPosition: Float = 0f
    private var numbersYPosition: Float = 0f
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
    private var pointsData: PointsData? = null
    private val minSelectorPositionChangeListeners = ArrayList<MinSelectorPositionChangeListener>()
    private val maxSelectorPositionChangeListeners = ArrayList<MaxSelectorPositionChangeListener>()

    private var listenersEnabled = true

    // True selectors values (set from outside by setters or by touch events)
    var minSelectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            for (minSelectorPositionChangeListener in minSelectorPositionChangeListeners) {
                minSelectorPositionChangeListener.onMinValueChanged(new)
            }
        }
    }
        private set

    var maxSelectorValue: Float by Delegates.observable(0f) { _, _, new: Float ->
        if (listenersEnabled) {
            for (maxSelectorPositionChangeListener in maxSelectorPositionChangeListeners) {
                maxSelectorPositionChangeListener.onMaxValueChanged(new)
            }
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
            selectorBorderColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__selector_border_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_selector_border_color))
            textColor = attributes.getColor(
                    R.styleable.pg__LaGrange_pg__text_color,
                    ContextCompat.getColor(getContext(), R.color.pg__default_text_color))
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

        minViewWidth = convertDpToPixel(MIN_VIEW_WIDTH.toFloat(), context)
        minViewHeight = convertDpToPixel(MIN_VIEW_HEIGHT.toFloat(), context)
        graphTopDrawPositionFromTop = convertDpToPixel(GRAPH_TOP_DRAW_POSITION_FROM_TOP.toFloat(), context)
        graphTopPositionFromTop = convertDpToPixel(GRAPH_TOP_POSITION_FROM_TOP.toFloat(), context)
        lineYPositionFromBottom = convertDpToPixel(LINE_Y_POSITION_FROM_BOTTOM.toFloat(), context)
        numbersYPositionFromBottom = convertDpToPixel(NUMBERS_Y_POSITION_FROM_BOTTOM.toFloat(), context)
        xAxisLeftRightPadding = convertDpToPixel(X_AXIS_LEFT_RIGHT_PADDING.toFloat(), context)
        pointIndicatorLength = convertDpToPixel(POINT_INDICATOR_LENGTH.toFloat(), context)
        selectorDiameter = convertDpToPixel(SELECTOR_DIAMETER.toFloat(), context)
        selectorTouchDiameter = convertDpToPixel(SELECTOR_TOUCH_DIAMETER.toFloat(), context)

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

        graphBoundsRect = RectF()
        selectedGraphBoundsRect = RectF()
        selectedLine = RectF(
                0f,
                0f,
                0f,
                selectedLineThickness)

        splineGraphPath = Path()

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
                    getXGraphPositionFromXValue(pointsData, minSelectorValue),
                    animateSelectorChanges)

            setMaxSelectorXPosition(
                    getXGraphPositionFromXValue(pointsData, maxSelectorValue),
                    animateSelectorChanges)
        }
    }

    fun addMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        minSelectorPositionChangeListeners.add(minSelectorPositionChangeListener)
    }

    fun removeMinSelectorChangeListener(minSelectorPositionChangeListener: MinSelectorPositionChangeListener) {
        minSelectorPositionChangeListeners.remove(minSelectorPositionChangeListener)
    }

    fun addMaxSelectorChangeListener(maxSelectorPositionChangeListener: MaxSelectorPositionChangeListener) {
        maxSelectorPositionChangeListeners.add(maxSelectorPositionChangeListener)
    }

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

        graphTopDrawYPosition = viewStartY + graphTopDrawPositionFromTop
        graphTopYPosition = viewStartY + graphTopPositionFromTop
        lineYPosition = viewEndY - lineYPositionFromBottom
        numbersYPosition = viewEndY - numbersYPositionFromBottom

        xAxisRect.left = viewStartX + xAxisLeftRightPadding
        xAxisRect.right = viewEndX - xAxisLeftRightPadding
        xAxisRect.setYMiddle(lineYPosition)
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

        graphMinXPosition = xAxisRect.left + lineThickness / 2
        graphMaxXPosition = xAxisRect.right - lineThickness / 2

        graphTopDrawYPosition = viewStartY

        graphBoundsRect.set(graphMinXPosition, graphTopDrawYPosition, graphMaxXPosition, lineYPosition - lineThickness / 2)

        selectedGraphBoundsRect.set(graphBoundsRect)
        selectedLine.left = selectedGraphBoundsRect.left
        selectedLine.right = selectedGraphBoundsRect.right

        minSelector.setYMiddle(lineYPosition)
        minSelectorTouchField.setYMiddle(lineYPosition)
        maxSelector.setYMiddle(lineYPosition)
        maxSelectorTouchField.setYMiddle(lineYPosition)
        selectedLine.setYMiddle(lineYPosition)

        pointsData?.let { pointsData ->
            generateSplineGraphPath(pointsData)
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
                setMinSelectorXPosition(newXPosition)
                dispatchOnMinSelectorPositionChanged(pointsData)
            } else if (maxSelectorSelected) {
                if (newXPosition > graphMaxXPosition) {
                    newXPosition = graphMaxXPosition
                } else if (newXPosition < minSelector.getXPosition()) {
                    newXPosition = minSelector.getXPosition()
                }
                setMaxSelectorXPosition(newXPosition)
                dispatchOnMaxSelectorPositionChanged(pointsData)
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
        canvas.scale(1f, graphYAxisScaleFactor, 0f, lineYPosition)
        canvas.clipRect(graphBoundsRect)
        canvas.drawPath(splineGraphPath, graphPaint)
        canvas.clipRect(selectedGraphBoundsRect)
        canvas.drawPath(splineGraphPath, selectedGraphPaint)
        canvas.restore()

        // draw selected line and selectors
        canvas.drawRect(selectedLine, selectedLinePaint)
        canvas.drawOval(minSelector, selectorPaint)
        canvas.drawOval(minSelector, selectorBorderPaint)
        canvas.drawOval(maxSelector, selectorPaint)
        canvas.drawOval(maxSelector, selectorBorderPaint)

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

    private fun dispatchOnMinSelectorPositionChanged(pointsData: PointsData) {
        updateSelectorValues(pointsData)
    }

    private fun dispatchOnMaxSelectorPositionChanged(pointsData: PointsData) {
        updateSelectorValues(pointsData)
    }

    private fun updateSelectorValues(pointsData: PointsData) {
        minSelectorValue = getXValueFromXGraphPosition(pointsData, minSelector.getXPosition())
        maxSelectorValue = getXValueFromXGraphPosition(pointsData, maxSelector.getXPosition())
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

        generateSplineGraphPath(pointsData)
        refreshSelectorsPositions(pointsData)
    }

    private fun generateSplineGraphPath(pointsData: PointsData) {
        val knotsArr = getGraphPointsFromPointsData(pointsData)
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

    private fun refreshSelectorsPositions(pointsData: PointsData) {
        setMinSelectorXPosition(getXGraphPositionFromXValue(pointsData, minSelectorValue))
        setMaxSelectorXPosition(getXGraphPositionFromXValue(pointsData, maxSelectorValue))
    }

    private fun getGraphPointsFromPointsData(pointsData: PointsData): List<Point> {
        val points = pointsData.points

        return List(points.size) {
            Point(
                    getXGraphPositionFromXValue(pointsData, points[it].x),
                    getYGraphPositionFromYValue(pointsData, points[it].y))
        }
    }

    interface MinSelectorPositionChangeListener {
        fun onMinValueChanged(newMinValue: Float)
    }

    interface MaxSelectorPositionChangeListener {
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