package com.johnnym.lagrange

import android.os.Parcel
import android.os.Parcelable

data class Range(val from: Long, val to: Long, val count: Long) : Parcelable {

    val middle: Long
        get() = (from + to) / 2

    val range: Long
        get() = to - from

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(from)
        parcel.writeLong(to)
        parcel.writeLong(count)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Range> {
        override fun createFromParcel(parcel: Parcel): Range {
            return Range(
                    parcel.readLong(),
                    parcel.readLong(),
                    parcel.readLong())
        }

        override fun newArray(size: Int): Array<Range?> {
            return arrayOfNulls(size)
        }
    }
}