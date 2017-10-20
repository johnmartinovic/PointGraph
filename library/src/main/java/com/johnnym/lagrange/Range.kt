package com.johnnym.lagrange

import android.os.Parcel
import android.os.Parcelable

data class Range(val from: Float, val to: Float, val count: Float) : Parcelable {

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