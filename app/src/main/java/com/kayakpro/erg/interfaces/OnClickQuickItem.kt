package com.kayakpro.erg.interfaces

import com.kayakpro.erg.model.TrainingModel


interface OnClickQuickItem {
        fun clickItem(id: Int, name:String, isFirst:Boolean, model:TrainingModel)
    }