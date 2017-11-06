package com.johnnym.pointgraph

import android.os.Parcel
import android.os.Parcelable

/**
 * Helping data class which defines a range (with its start and end values) and some value
 * which is uniquely related to this range.
 *
 * E.g. in a height [Range] from 180cm and 190cm there is a total of 7 students in some class.
 *
 * @param from range start value
 * @param to range end value
 * @param count some value uniquely related to  the range between [from] and [to]
 */
data class Range(
        val from: Float,
        val to: Float,
        val count: Float
) : Parcelable {

    val middle: Float
        get() = (from + to) / 2

    val range: Float
        get() = to - from

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(from)
        parcel.writeFloat(to)
        parcel.writeFloat(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Range> {
        override fun createFromParcel(parcel: Parcel): Range {
            return Range(
                    parcel.readFloat(),
                    parcel.readFloat(),
                    parcel.readFloat())
        }

        override fun newArray(size: Int): Array<Range?> {
            return arrayOfNulls(size)
        }
    }
}