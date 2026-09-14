package com.kayakpro.erg.interfaces

import com.kayakpro.erg.model.WorkoutModel

interface OnDeleteItem {
    fun clickItem(id: Int, model:WorkoutModel)
    fun clickSave(id: Int, model: WorkoutModel)
    fun clickEdit(id:Int,model: WorkoutModel)
}