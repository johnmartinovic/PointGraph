package com.johnnym.pointgraph

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
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

    private val attributes: Attributes
    private val drawObjects: DrawObjects

    private val minSelector: RectF
    private val maxSelector: RectF
    private val minSelectorTouchField: RectF
    private val maxSelectorTouchField: RectF
    private val xAxisRect: RectF
    private val xAxisIndicatorsRects: List<Rect>
    private val indicatorsXPositions: FloatArray
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
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.pg__LaGrange, defStyleAttr, 0)
        attributes = Attributes(
                styledAttrs.getColor(
                        R.styleable.pg__LaGrange_pg__x_axis_color,
                        ContextCompat.getColor(getContext(), R.color.pg__default_x_axis_color)),
                styledAttrs.getDimension(
                        R.styleable.pg__LaGrange_pg__x_axis_thickness,
                        resources.getDimension(R.dimen.pg__default_line_thickness)),
                styledAttrs.getColor(
                        R.styleable.pg__LaGrange_pg__selected_line_color,
                        ContextCompat.getColor(getContext(), R.color.pg__default_selected_line_color)),
                styledAttrs.getDimension(
                        R.styleable.pg__LaGrange_pg__selected_line_thickness,
                        resources.getDimension(R.dimen.pg__default_selected_line_thickness)),
                styledAttrs.getColor(
                        R.styleable.pg__LaGrange_pg__selector_color,
                        ContextCompat.getColor(getContext(), R.color.pg__default_selector_color)),
                styledAttrs.getColor(
                        R.styleable.pg__LaGrange_pg__text_color,
                        ContextCompat.getColor(getContext(), R.color.pg__default_text_color)),
                styledAttrs.getDimension(
                        R.styleable.pg__LaGrange_pg__text_size,
                        resources.getDimension(R.dimen.pg__default_text_size)),
                styledAttrs.getColor(
                        R.styleable.pg__LaGrange_pg__graph_color,
                        ContextCompat.getColor(getContext(), R.color.pg__default_graph_color)),
                styledAttrs.getColor(
                        R.styleable.pg__LaGrange_pg__selected_graph_color,
                        ContextCompat.getColor(getContext(), R.color.pg__default_selected_graph_color)),
                styledAttrs.getInteger(
                        R.styleable.pg__LaGrange_pg__x_axis_number_of_middle_points,
                        resources.getInteger(R.integer.pg__default_line_middle_points_num)),
                styledAttrs.getBoolean(
                        R.styleable.pg__LaGrange_pg__animate_selector_changes,
                        resources.getBoolean(R.bool.pg__default_animate_selector_changes)),
                styledAttrs.getDrawable(R.styleable.pg__LaGrange_pg__x_axis_indicator)
                        ?: ContextCompat.getDrawable(context, R.drawable.pg__circle_point_indicator)!!,
                resources.getDimension(R.dimen.pg__la_grange_min_view_width),
                resources.getDimension(R.dimen.pg__la_grange_min_view_height),
                resources.getDimension(R.dimen.pg__la_grange_graph_top_from_top),
                resources.getDimension(R.dimen.pg__la_grange_graph_bottom_from_bottom),
                resources.getDimension(R.dimen.pg__la_grange_graph_left_right_padding),
                resources.getDimension(R.dimen.pg__la_grange_numbers_y_position_from_bottom),
                resources.getDimension(R.dimen.pg__la_grange_selector_diameter),
                resources.getDimension(R.dimen.pg__la_grange_selector_touch_diameter))
        styledAttrs.recycle()

        minSelector = RectF(
                0f,
                0f,
                attributes.selectorDiameter,
                attributes.selectorDiameter)
        maxSelector = RectF(minSelector)
        minSelectorTouchField = RectF(
                0f,
                0f,
                attributes.selectorTouchDiameter,
                attributes.selectorTouchDiameter)
        maxSelectorTouchField = RectF(minSelectorTouchField)

        xAxisRect = RectF(0f, 0f, 0f, attributes.xAxisThickness)
        val xAxisNumberOfPoints = attributes.xAxisNumberOfMiddlePoints + 2
        val xAxisIndicatorRect = Rect(
                0,
                0,
                attributes.xAxisIndicatorDrawable.intrinsicWidth,
                attributes.xAxisIndicatorDrawable.intrinsicHeight)
        xAxisIndicatorsRects = List(xAxisNumberOfPoints) { Rect(xAxisIndicatorRect) }
        indicatorsXPositions = FloatArray(xAxisNumberOfPoints)
        numbers = FloatArray(xAxisNumberOfPoints)

        selectedLine = RectF(
                0f,
                0f,
                0f,
                attributes.selectedLineThickness)

        graphPath = GraphPath()
        graphBoundsRect = RectF()
        selectedGraphBoundsRect = RectF()

        minSelectorAnimator = ValueAnimator().apply {
            duration = 150
            addUpdateListener { animation ->
                updateMinSelectorDependantShapes(animation.animatedValue as Float)
                invalidate()
            }
        }
        maxSelectorAnimator = ValueAnimator().apply {
            duration = 150
            addUpdateListener { animation ->
                updateMaxSelectorDependantShapes(animation.animatedValue as Float)
                invalidate()
            }
        }
        graphScaleAnimator = ValueAnimator().apply {
            duration = 300
            setFloatValues(0f, 1f)
            addUpdateListener { animation ->
                graphYAxisScaleFactor = animation.animatedValue as Float
                invalidate()
            }
        }

        drawObjects = DrawObjects(
                Paint().apply {
                    isAntiAlias = true
                    color = attributes.xAxisColor
                    style = Paint.Style.FILL
                },
                TextPaint().apply {
                    isAntiAlias = true
                    color = attributes.textColor
                    textAlign = Paint.Align.CENTER
                    textSize = attributes.textSize
                },
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = attributes.selectorColor
                },
                Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = attributes.selectedLineColor
                },
                Paint().apply {
                    isAntiAlias = true
                    color = attributes.graphColor
                    style = Paint.Style.FILL
                },
                Paint().apply {
                    isAntiAlias = true
                    color = attributes.selectedGraphColor
                    style = Paint.Style.FILL
                }
        )
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

            setMinSelectorXPosition(
                    affineTransformXToY(minSelectorValue, pointsData.minX, pointsData.maxX, graphLeft, graphRight),
                    attributes.animateSelectorChanges)

            setMaxSelectorXPosition(
                    affineTransformXToY(maxSelectorValue, pointsData.minX, pointsData.maxX, graphLeft, graphRight),
                    attributes.animateSelectorChanges)
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
        val width = resolveSizeAndState(attributes.minViewWidth.toInt(), widthMeasureSpec, 0)
        val height = resolveSizeAndState(attributes.minViewHeight.toInt(), heightMeasureSpec, 0)

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        viewStartX = paddingLeft.toFloat()
        viewEndX = (w - paddingRight).toFloat()
        viewStartY = paddingTop.toFloat()
        viewEndY = (h - paddingBottom).toFloat()

        graphTop = viewStartY + attributes.graphTopFromTop
        graphBottom = viewEndY - attributes.graphBottomFromBottom
        graphLeft = viewStartX + attributes.graphLeftRightPadding
        graphRight = viewEndX - attributes.graphLeftRightPadding

        numbersYPosition = viewEndY - attributes.numbersYPositionFromBottom

        xAxisRect.left = graphLeft - attributes.xAxisThickness / 2
        xAxisRect.right = graphRight + attributes.xAxisThickness / 2
        xAxisRect.setYMiddle(graphBottom)

        // Calculate X axis number positions
        val pointsDistance = (xAxisRect.right - xAxisRect.left) / (attributes.xAxisNumberOfMiddlePoints + 1)
        for (i in indicatorsXPositions.indices) {
            val indicatorXPosition = xAxisRect.left + i * pointsDistance
            indicatorsXPositions[i] = indicatorXPosition
            xAxisIndicatorsRects[i].setXMiddle(indicatorXPosition.toInt())
            xAxisIndicatorsRects[i].setYMiddle(graphBottom.toInt())
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

        pointsData
                ?.let { pointsData ->
                    handleTouchEvent(pointsData, event)

                    return true
                }
                ?: return false
    }

    private fun handleTouchEvent(pointsData: PointsData, event: MotionEvent) {
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
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) {
            return
        }

        if (hasData()) {
            // draw graph and selected part of graph
            canvas.save()
            canvas.scale(1f, graphYAxisScaleFactor, 0f, graphBottom)
            canvas.clipRect(graphBoundsRect)
            canvas.drawPath(graphPath, drawObjects.graphPaint)
            canvas.clipRect(selectedGraphBoundsRect)
            canvas.drawPath(graphPath, drawObjects.selectedGraphPaint)
            canvas.restore()
        }

        // draw X axis line and indicators
        canvas.drawRect(xAxisRect, drawObjects.xAxisRectPaint)
        for (xAxisIndicatorsRect in xAxisIndicatorsRects) {
            attributes.xAxisIndicatorDrawable.bounds = xAxisIndicatorsRect
            attributes.xAxisIndicatorDrawable.draw(canvas)
        }

        if (hasData()) {
            // draw selected line and selectors
            canvas.drawRect(selectedLine, drawObjects.selectedLinePaint)
            canvas.drawOval(minSelector, drawObjects.selectorPaint)
            canvas.drawOval(maxSelector, drawObjects.selectorPaint)

            for (i in 0 until attributes.xAxisNumberOfMiddlePoints + 2) {
                canvas.drawText(
                        String.format("%.0f", numbers[i]),
                        indicatorsXPositions[i],
                        numbersYPosition,
                        drawObjects.textPaint)
            }
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
        pointsData = state.pointsData
        this.minSelectorValue = state.minSelectorValue
        this.maxSelectorValue = state.maxSelectorValue
        refreshGraphValues()
        listenersEnabled = true

        super.onRestoreInstanceState(state.superState)
    }

    private fun hasData(): Boolean {
        return pointsData != null
    }

    private fun setMinSelectorXPosition(x: Float, animated: Boolean = false) {
        if (animated) {
            minSelectorAnimator.setFloatValues(minSelector.getXPosition(), x)
            minSelectorAnimator.start()
        } else {
            updateMinSelectorDependantShapes(x)
            invalidate()
        }
    }

    private fun updateMinSelectorDependantShapes(minSelectorX: Float) {
        selectedGraphBoundsRect.left = minSelectorX
        selectedLine.left = minSelectorX
        minSelector.setXMiddle(minSelectorX)
        minSelectorTouchField.setXMiddle(minSelectorX)
    }

    private fun setMaxSelectorXPosition(x: Float, animated: Boolean = false) {
        if (animated) {
            maxSelectorAnimator.setFloatValues(maxSelector.getXPosition(), x)
            maxSelectorAnimator.start()
        } else {
            updateMaxSelectorDependantShapes(x)
            invalidate()
        }
    }

    private fun updateMaxSelectorDependantShapes(maxSelectorX: Float) {
        selectedGraphBoundsRect.right = maxSelectorX
        selectedLine.right = maxSelectorX
        maxSelector.setXMiddle(maxSelectorX)
        maxSelectorTouchField.setXMiddle(maxSelectorX)
    }

    private fun updateSelectorValues(pointsData: PointsData) {
        minSelectorValue = affineTransformXToY(minSelector.getXPosition(), graphLeft, graphRight, pointsData.minX, pointsData.maxX)
        maxSelectorValue = affineTransformXToY(maxSelector.getXPosition(), graphLeft, graphRight, pointsData.minX, pointsData.maxX)
    }

    private fun refreshGraphValues() {
        pointsData?.let {
            for (i in numbers.indices) {
                numbers[i] = i * it.xRange / (numbers.size - 1) + it.minX
            }

            graphPath.generatePath(it, graphLeft, graphBottom, graphRight, graphTop)
            minSelectorValue = it.minX
            maxSelectorValue = it.maxX
            refreshSelectorsPositions(it)

            invalidate()
        }
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

    class Attributes(
            val xAxisColor: Int,
            val xAxisThickness: Float,
            val selectedLineColor: Int,
            val selectedLineThickness: Float,
            val selectorColor: Int,
            val textColor: Int,
            val textSize: Float,
            val graphColor: Int,
            val selectedGraphColor: Int,
            val xAxisNumberOfMiddlePoints: Int,
            val animateSelectorChanges: Boolean,
            val xAxisIndicatorDrawable: Drawable,
            val minViewWidth: Float,
            val minViewHeight: Float,
            val graphTopFromTop: Float,
            val graphBottomFromBottom: Float,
            val graphLeftRightPadding: Float,
            val numbersYPositionFromBottom: Float,
            val selectorDiameter: Float,
            val selectorTouchDiameter: Float)

    class DrawObjects(
            val xAxisRectPaint: Paint,
            val textPaint: TextPaint,
            val selectorPaint: Paint,
            val selectedLinePaint: Paint,
            val graphPaint: Paint,
            val selectedGraphPaint: Paint)
}