package com.kayakpro.erg.model

import android.os.Parcel
import android.os.Parcelable

data class RBody(
    val id: String?,
    val name: String?,
    val details: String?,
    val workouts: ArrayList<Workout>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Workout.CREATOR) ?: ArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(details)
        parcel.writeTypedList(workouts)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RBody> {
        override fun createFromParcel(parcel: Parcel): RBody {
            return RBody(parcel)
        }

        override fun newArray(size: Int): Array<RBody?> {
            return arrayOfNulls(size)
        }
    }

    data class Workout(
        val id: Int = 0,
        val name: String = "",
        val is_time_or_distance: Boolean = false,
        val value: String = "",
        val is_rest: Int = 0,
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "",
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeValue(id)
            parcel.writeString(name)
            parcel.writeByte(if (is_time_or_distance) 1 else 0)
            parcel.writeString(value)
            parcel.writeValue(is_rest)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Workout> {
            override fun createFromParcel(parcel: Parcel): Workout {
                return Workout(parcel)
            }

            override fun newArray(size: Int): Array<Workout?> {
                return arrayOfNulls(size)
            }
        }
    }
}