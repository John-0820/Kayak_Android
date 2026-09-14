package com.kayakpro.erg.model
import android.os.Parcel
import android.os.Parcelable

data class TrainingResponseModel(
    val code: String?,
    val message: String?,
    var body: ArrayList<RBody>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(RBody.CREATOR) ?: ArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeString(message)
        parcel.writeTypedList(body)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrainingResponseModel> {
        override fun createFromParcel(parcel: Parcel): TrainingResponseModel {
            return TrainingResponseModel(parcel)
        }

        override fun newArray(size: Int): Array<TrainingResponseModel?> {
            return arrayOfNulls(size)
        }
    }


}