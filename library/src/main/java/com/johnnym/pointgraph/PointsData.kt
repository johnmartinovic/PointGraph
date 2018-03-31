package com.johnnym.pointgraph

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Data class which defines a list of points in rectangular coordinate system.
 * When connected, these points define a graph, whose min and max borders are defined
 * by points themselves.
 *
 * @param points a list of points which represent a graph
 */
@Parcelize
data class PointsData(
        val points: List<Point>
) : Parcelable {

    @IgnoredOnParcel val minX: Float = points[0].x
    @IgnoredOnParcel val maxX: Float = points[points.size - 1].x
    @IgnoredOnParcel val xRange: Float = maxX - minX
    @IgnoredOnParcel val minY: Float = 0f
    @IgnoredOnParcel val maxY: Float

    init {
        var pointsMaxYTemp = 0f
        points.asSequence()
                .filter { pointsMaxYTemp < it.y }
                .forEach { pointsMaxYTemp = it.y }
        maxY = pointsMaxYTemp
    }
}
