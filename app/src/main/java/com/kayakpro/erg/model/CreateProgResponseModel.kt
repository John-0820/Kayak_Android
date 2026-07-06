package com.kayakpro.erg.model

data class CreateProgResponseModel(
    val code: String?,
    val message: String?,
    var body:RBody
) {
    data class RBody(
        val id: String?,
        val name: String?,
        val details: String?,
        val workouts: ArrayList<Workout>
    ) {
        data class Workout(
            val id: Int?,
            val name: String,
            val is_time_or_distance: Boolean,
            val value: String,
            val rest: String = ""

        )
    }
}