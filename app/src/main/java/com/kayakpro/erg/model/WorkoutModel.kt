package com.kayakpro.erg.model

data class WorkoutModel(
    var id: String?,
    var worokoutName: String?,
    var time: String?,
    var distance: String?,
    var is_rest: Int = 0,
    var isEdit: Boolean?,
    var is_time_or_distance: Boolean =true,
    var value: String ="",
    var shouldEdit: Boolean =true
)
