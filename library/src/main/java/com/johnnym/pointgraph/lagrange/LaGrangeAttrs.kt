package com.johnnym.pointgraph.lagrange

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.johnnym.pointgraph.R

data class LaGrangeAttrs(
        val xAxisColor: Int,
        val xAxisThickness: Float,
        val selectedLineColor: Int,
        val selectedLineThickness: Float,
        val selector: Drawable,
        val textColor: Int,
        val textSize: Float,
        val graphColor: Int,
        val selectedGraphColor: Int,
        val xAxisNumberOfMiddlePoints: Int,
        val xAxisIndicatorDrawable: Drawable,
        val minViewWidth: Float,
        val minViewHeight: Float,
        val graphTopFromTop: Float,
        val graphBottomFromBottom: Float,
        val graphLeftRightPadding: Float,
        val numbersYPositionFromBottom: Float) {

    companion object {

        fun create(context: Context,
                   attrs: AttributeSet?,
                   defStyleAttr: Int): LaGrangeAttrs {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.pg__LaGrange, defStyleAttr, 0)
            val resources = context.resources

            val laGrangeAttrs = LaGrangeAttrs(
                    styledAttrs.getColor(
                            R.styleable.pg__LaGrange_pg__x_axis_color,
                            ContextCompat.getColor(context, R.color.pg__default_x_axis_color)),
                    styledAttrs.getDimension(
                            R.styleable.pg__LaGrange_pg__x_axis_thickness,
                            resources.getDimension(R.dimen.pg__default_line_thickness)),
                    styledAttrs.getColor(
                            R.styleable.pg__LaGrange_pg__selected_line_color,
                            ContextCompat.getColor(context, R.color.pg__default_selected_line_color)),
                    styledAttrs.getDimension(
                            R.styleable.pg__LaGrange_pg__selected_line_thickness,
                            resources.getDimension(R.dimen.pg__default_selected_line_thickness)),
                    styledAttrs.getDrawable(R.styleable.pg__LaGrange_pg__selector)
                            ?: ContextCompat.getDrawable(context, R.drawable.pg__ring_selector)!!,
                    styledAttrs.getColor(
                            R.styleable.pg__LaGrange_pg__text_color,
                            ContextCompat.getColor(context, R.color.pg__default_text_color)),
                    styledAttrs.getDimension(
                            R.styleable.pg__LaGrange_pg__text_size,
                            resources.getDimension(R.dimen.pg__default_text_size)),
                    styledAttrs.getColor(
                            R.styleable.pg__LaGrange_pg__graph_color,
                            ContextCompat.getColor(context, R.color.pg__default_graph_color)),
                    styledAttrs.getColor(
                            R.styleable.pg__LaGrange_pg__selected_graph_color,
                            ContextCompat.getColor(context, R.color.pg__default_selected_graph_color)),
                    styledAttrs.getInteger(
                            R.styleable.pg__LaGrange_pg__x_axis_number_of_middle_points,
                            resources.getInteger(R.integer.pg__default_line_middle_points_num)),
                    styledAttrs.getDrawable(R.styleable.pg__LaGrange_pg__x_axis_indicator)
                            ?: ContextCompat.getDrawable(context, R.drawable.pg__circle_point_indicator)!!,
                    resources.getDimension(R.dimen.pg__la_grange_min_view_width),
                    resources.getDimension(R.dimen.pg__la_grange_min_view_height),
                    resources.getDimension(R.dimen.pg__la_grange_graph_top_from_top),
                    resources.getDimension(R.dimen.pg__la_grange_graph_bottom_from_bottom),
                    resources.getDimension(R.dimen.pg__la_grange_graph_left_right_padding),
                    resources.getDimension(R.dimen.pg__la_grange_numbers_y_position_from_bottom))

            styledAttrs.recycle()

            return laGrangeAttrs
        }
    }
}