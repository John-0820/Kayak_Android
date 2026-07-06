package com.kayakpro.erg.model

import android.graphics.drawable.Drawable

data class TrainingModel(
    val resId: Drawable,
    val name: String?,
    var value:String?,
    var unit:String="",
    var shouldShow:Boolean=true
)