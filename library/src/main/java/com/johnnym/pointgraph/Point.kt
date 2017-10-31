package com.johnnym.pointgraph

import android.os.Parcel
import android.os.Parcelable

data class Point(val x: Float, val y: Float) : Parcelable {

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return Point(
                    parcel.readFloat(),
                    parcel.readFloat())
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }
}