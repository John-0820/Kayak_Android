package com.kayakpro.erg.model.requestmodel

data class CreateTrainingHistoryRequest(

    val type: String,
    val name: String,
    val device_type: String,
    val training_id: Int?,
    val is_time_or_distance: Boolean,
    val value: Int?,
    val pace_for_boat: Int?,
    val workout_list: ArrayList<Workout>,


    ) {
    data class Workout(
        val distance: Int?,
        val speed: Int?,
        val time_milli_seconds: Long?,
        val calories: Double?,
        val watts: Int?,
        val pace_per_distance: String?,
        val stroke_rate: Int?,
        val time_elapsed_milli_seconds: Long?,
        val heart_rate: Int?,
    )
}
