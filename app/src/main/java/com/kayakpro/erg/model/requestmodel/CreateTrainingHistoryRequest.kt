package com.kayakpro.erg.model.requestmodel

data class CreateTrainingHistoryRequest(

    val type: String,
    val device_type: String,
    val training_id: String,
    val is_time_or_distance: Boolean,
    val value: String,
    val pace_for_boat: String,
    val workout_list: ArrayList<Workout>,


    ) {
    data class Workout(
        val distance: String?,
        val speed: String,
        val time_milli_seconds: String,
        val calories: String,
        val watts: String,
        val pace_per_distance: String,
        val stroke_rate: String,
        val time_elapsed_milli_seconds: String,
        val heart_rate: String,
    )
}
