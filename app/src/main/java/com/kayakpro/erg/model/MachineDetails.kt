package com.kayakpro.erg.model

import android.graphics.drawable.Drawable

class MachineDetails(
    val id: Drawable,
    val name: String?,
    var locked:Int=0,
    var isSelected: Boolean = false

)