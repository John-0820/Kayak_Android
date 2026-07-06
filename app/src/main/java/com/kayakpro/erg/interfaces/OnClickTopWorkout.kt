package com.kayakpro.erg.interfaces

import com.kayakpro.erg.model.RBody


interface OnClickTopWorkout {
        fun clickItem(model: RBody.Workout, position:Int)
    }