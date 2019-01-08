package com.johnnym.pointgraph.graphend

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import com.johnnym.pointgraph.R

data class GraphEndAttrs(
        val xAxisColor: Int,
        val xAxisThickness: Float,
        val selectedLineColor: Int,
        val selectedLineThickness: Float,
        val selector: Drawable,
        val graphColor: Int,
        val selectedGraphColor: Int,
        val minViewWidth: Float,
        val minViewHeight: Float,
        val graphTopFromTop: Float,
        val graphBottomFromBottom: Float,
        val graphLeftRightPadding: Float) {

    companion object {

        fun create(
                context: Context,
                attrs: AttributeSet?,
                defStyleAttr: Int
        ): GraphEndAttrs {
            val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.GraphEnd, defStyleAttr, 0)
            val resources = context.resources

            val graphEndAttrs = GraphEndAttrs(
                    styledAttrs.getColor(
                            R.styleable.GraphEnd_pg__x_axis_color,
                            ContextCompat.getColor(context, R.color.pg__default_x_axis_color)),
                    styledAttrs.getDimension(
                            R.styleable.GraphEnd_pg__x_axis_thickness,
                            resources.getDimension(R.dimen.pg__default_line_thickness)),
                    styledAttrs.getColor(
                            R.styleable.GraphEnd_pg__selected_line_color,
                            ContextCompat.getColor(context, R.color.pg__default_selected_line_color)),
                    styledAttrs.getDimension(
                            R.styleable.GraphEnd_pg__selected_line_thickness,
                            resources.getDimension(R.dimen.pg__default_selected_line_thickness)),
                    styledAttrs.getDrawable(R.styleable.GraphEnd_pg__selector)
                            ?: ContextCompat.getDrawable(context, R.drawable.pg__ring_selector)!!,
                    styledAttrs.getColor(
                            R.styleable.GraphEnd_pg__graph_color,
                            ContextCompat.getColor(context, R.color.pg__default_graph_color)),
                    styledAttrs.getColor(
                            R.styleable.GraphEnd_pg__selected_graph_color,
                            ContextCompat.getColor(context, R.color.pg__default_selected_graph_color)),
                    resources.getDimension(R.dimen.pg__graph_end_min_view_width),
                    resources.getDimension(R.dimen.pg__graph_end_min_view_height),
                    resources.getDimension(R.dimen.pg__graph_end_graph_top_from_top),
                    resources.getDimension(R.dimen.pg__graph_end_graph_bottom_from_bottom),
                    resources.getDimension(R.dimen.pg__graph_end_graph_left_right_padding))

            styledAttrs.recycle()

            return graphEndAttrs
        }
    }
}