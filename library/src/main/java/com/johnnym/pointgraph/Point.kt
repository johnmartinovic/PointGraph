package com.johnnym.pointgraph

import android.os.Parcel
import android.os.Parcelable

/**
 * Simple data class which defines a point in rectangular coordinate system.
 * These points do NOT define their positions in PointGraph on their own.
 * See [PointsData] for further information.
 *
 * @param x point x-axis value
 * @param y point y-axis value
 */
data class Point(
        val x: Float,
        val y: Float
) : Parcelable {

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