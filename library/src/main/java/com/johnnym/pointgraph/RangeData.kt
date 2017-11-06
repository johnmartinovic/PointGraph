package com.johnnym.pointgraph

import android.os.Parcel
import android.os.Parcelable

/**
 * Data class which defines a list of ranges with their uniquely related values.
 * When these range values are connected, graph is defined, whose min and max borders
 * are defined by ranges and their related values.
 * It contains a [PointsData] instance which can be easily set as an argument for PointGraphs.
 *
 * @param rangeList a list of ranges which, with their uniquely related values, represent a graph
 */
data class RangeData(
        private val rangeList: List<Range>
) : Parcelable {

    /**
     * [PointsData] instance calculated out from [rangeList].
     */
    val pointsData: PointsData

    init {
        var x: Float
        var y: Float

        val points = ArrayList<Point>()

        // Calculate and add first point
        // (lets say its value is the half of the first data point)
        x = rangeList[0].from
        y = (rangeList[0].count / 2)

        points.add(Point(x, y))

        // Calculate and add middle points
        val middlePoints = Array(rangeList.size) {
            Point(rangeList[it].middle, rangeList[it].count)
        }
        points.addAll(middlePoints)

        // Calculate and add last point
        // (lets say its value is the half of the last data point)
        x = rangeList[rangeList.size - 1].to
        y = (rangeList[rangeList.size - 1].count / 2)
        points[points.size - 1] = Point(x, y)

        pointsData = PointsData(points)
    }

    /**
     * Get a sum of all [rangeList] [Range.count]s
     *
     * @param minValue start of range
     * @param maxValue end of range
     * @return summed value of all [Range.count]s that are contained in range defined by [minValue] and [maxValue]
     */
    fun getApproxCountInRange(minValue: Float, maxValue: Float): Float {
        return rangeList
                .map { Math.max(0f, Math.min(it.to, maxValue) - Math.max(it.from, minValue)) * it.count / it.range }
                .sum()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(rangeList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RangeData> {
        override fun createFromParcel(parcel: Parcel): RangeData {
            return RangeData(parcel.createTypedArrayList(Range.CREATOR))
        }

        override fun newArray(size: Int): Array<RangeData?> {
            return arrayOfNulls(size)
        }
    }
}