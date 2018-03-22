package com.johnnym.pointgraph.lagrange

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.johnnym.pointgraph.R

class LaGrangeAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int) {

    val xAxisColor: Int
    val xAxisThickness: Float
    val selectedLineColor: Int
    val selectedLineThickness: Float
    val selectorColor: Int
    val textColor: Int
    val textSize: Float
    val graphColor: Int
    val selectedGraphColor: Int
    val xAxisNumberOfMiddlePoints: Int
    val animateSelectorChanges: Boolean
    val xAxisIndicatorDrawable: Drawable
    val minViewWidth: Float
    val minViewHeight: Float
    val graphTopFromTop: Float
    val graphBottomFromBottom: Float
    val graphLeftRightPadding: Float
    val numbersYPositionFromBottom: Float
    val selectorDiameter: Float
    val selectorTouchDiameter: Float

    init {
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.pg__LaGrange, defStyleAttr, 0)
        val resources = context.resources

        xAxisColor = styledAttrs.getColor(
                R.styleable.pg__LaGrange_pg__x_axis_color,
                ContextCompat.getColor(context, R.color.pg__default_x_axis_color))
        xAxisThickness = styledAttrs.getDimension(
                R.styleable.pg__LaGrange_pg__x_axis_thickness,
                resources.getDimension(R.dimen.pg__default_line_thickness))
        selectedLineColor = styledAttrs.getColor(
                R.styleable.pg__LaGrange_pg__selected_line_color,
                ContextCompat.getColor(context, R.color.pg__default_selected_line_color))
        selectedLineThickness = styledAttrs.getDimension(
                R.styleable.pg__LaGrange_pg__selected_line_thickness,
                resources.getDimension(R.dimen.pg__default_selected_line_thickness))
        selectorColor = styledAttrs.getColor(
                R.styleable.pg__LaGrange_pg__selector_color,
                ContextCompat.getColor(context, R.color.pg__default_selector_color))
        textColor = styledAttrs.getColor(
                R.styleable.pg__LaGrange_pg__text_color,
                ContextCompat.getColor(context, R.color.pg__default_text_color))
        textSize = styledAttrs.getDimension(
                R.styleable.pg__LaGrange_pg__text_size,
                resources.getDimension(R.dimen.pg__default_text_size))
        graphColor = styledAttrs.getColor(
                R.styleable.pg__LaGrange_pg__graph_color,
                ContextCompat.getColor(context, R.color.pg__default_graph_color))
        selectedGraphColor = styledAttrs.getColor(
                R.styleable.pg__LaGrange_pg__selected_graph_color,
                ContextCompat.getColor(context, R.color.pg__default_selected_graph_color))
        xAxisNumberOfMiddlePoints = styledAttrs.getInteger(
                R.styleable.pg__LaGrange_pg__x_axis_number_of_middle_points,
                resources.getInteger(R.integer.pg__default_line_middle_points_num))
        animateSelectorChanges = styledAttrs.getBoolean(
                R.styleable.pg__LaGrange_pg__animate_selector_changes,
                resources.getBoolean(R.bool.pg__default_animate_selector_changes))
        xAxisIndicatorDrawable = styledAttrs.getDrawable(R.styleable.pg__LaGrange_pg__x_axis_indicator)
                ?: ContextCompat.getDrawable(context, R.drawable.pg__circle_point_indicator)!!
        minViewWidth = resources.getDimension(R.dimen.pg__la_grange_min_view_width)
        minViewHeight = resources.getDimension(R.dimen.pg__la_grange_min_view_height)
        graphTopFromTop = resources.getDimension(R.dimen.pg__la_grange_graph_top_from_top)
        graphBottomFromBottom = resources.getDimension(R.dimen.pg__la_grange_graph_bottom_from_bottom)
        graphLeftRightPadding = resources.getDimension(R.dimen.pg__la_grange_graph_left_right_padding)
        numbersYPositionFromBottom = resources.getDimension(R.dimen.pg__la_grange_numbers_y_position_from_bottom)
        selectorDiameter = resources.getDimension(R.dimen.pg__la_grange_selector_diameter)
        selectorTouchDiameter = resources.getDimension(R.dimen.pg__la_grange_selector_touch_diameter)

        styledAttrs.recycle()
    }
}