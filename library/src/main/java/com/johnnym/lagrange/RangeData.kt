package com.johnnym.lagrange

import android.os.Parcel
import android.os.Parcelable

data class RangeData(val rangeList: List<Range>) : Parcelable {

    val minX: Long
    val maxX: Long
    val minY: Long
    val maxY: Long
    val xRange: Long

    init {
        minX = rangeList[0].from
        maxX = rangeList[rangeList.size - 1].to
        minY = 0
        xRange = maxX - minX
        var rangeDataMaxYTemp = 0L
        rangeList.asSequence()
                .filter { rangeDataMaxYTemp < it.count }
                .forEach { rangeDataMaxYTemp = it.count }
        maxY = rangeDataMaxYTemp
    }

    fun getApproxCountInRange(minValue: Long, maxValue: Long): Long {
        return rangeList
                .map { Math.max(0, Math.min(it.to, maxValue) - Math.max(it.from, minValue)) * it.count / it.range }
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