package com.johnnym.pointgraph

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Simple data class which defines a point in rectangular coordinate system.
 * These points do NOT define their positions in PointGraph on their own.
 * See [PointsData] for further information.
 *
 * @param x point x-axis value
 * @param y point y-axis value
 */
@Parcelize
data class Point(
        val x: Float,
        val y: Float
) : Parcelable