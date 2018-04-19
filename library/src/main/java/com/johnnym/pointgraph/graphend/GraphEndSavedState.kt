package com.johnnym.pointgraph.graphend

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import com.johnnym.pointgraph.PointsData

class SavedState : View.BaseSavedState {

    var pointsData: PointsData? = null
    var selectorValue: Float = 0f

    constructor(superState: Parcelable) : super(superState)

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeParcelable(pointsData, flags)
        out.writeFloat(selectorValue)
    }

    private constructor(parcel: Parcel) : super(parcel) {
        pointsData = parcel.readParcelable(PointsData::class.java.classLoader)
        selectorValue = parcel.readFloat()
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState {
            return SavedState(parcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}