package com.johnnym.pointgraph

import android.os.Parcel
import android.os.Parcelable

/**
 * Data class which defines a list of points in rectangular coordinate system.
 * When connected, these points define a graph, whose min and max borders are defined
 * by points themselves.
 *
 * @param points a list of points which represent a graph
 */
data class PointsData(
        val points: List<Point>
) : Parcelable {

    val minX: Float
    val maxX: Float
    val minY: Float
    val maxY: Float
    val xRange: Float

    init {
        minX = points[0].x
        maxX = points[points.size - 1].x
        minY = 0f
        xRange = maxX - minX
        var pointsMaxYTemp = 0f
        points.asSequence()
                .filter { pointsMaxYTemp < it.y }
                .forEach { pointsMaxYTemp = it.y }
        maxY = pointsMaxYTemp
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(points)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PointsData> {
        override fun createFromParcel(parcel: Parcel): PointsData {
            return PointsData(parcel.createTypedArrayList(Point))
        }

        override fun newArray(size: Int): Array<PointsData?> {
            return arrayOfNulls(size)
        }
    }
}
