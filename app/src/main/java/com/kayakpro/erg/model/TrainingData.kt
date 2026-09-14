package com.kayakpro.erg.model

import android.os.Parcel
import android.os.Parcelable

data class TrainingData(
    val code: String?,
    val message: String?,
    var body: RBody
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(RBody::class.java.classLoader)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeString(message)
        parcel.writeParcelable(body, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrainingData> {
        override fun createFromParcel(parcel: Parcel): TrainingData {
            return TrainingData(parcel)
        }

        override fun newArray(size: Int): Array<TrainingData?> {
            return arrayOfNulls(size)
        }
    }


}