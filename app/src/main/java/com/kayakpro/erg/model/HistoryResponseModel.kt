package com.kayakpro.erg.model

import java.util.ArrayList
data class HistoryResponseModel(
    val code: String?,
    val message: String?,
    var body:List<RBody>?
) {
    data class RBody(
        val id: String?,
        val name: String?,
        val type: String?,
        val distance: String?,
        val speed: String?,
        val time: String?,
        val calories: String?,
        val watts: String?,
        val training_id: Int?,
        val pace_per_distance: String?,
        val stroke_rate: String?,
        val time_elapsed: String?,
        val heart_rate: String?,
        val created_date: String?,
        val workouts: ArrayList<Workout>
    ) {
        data class Workout(
            val id: Int?,
            val name: String,
            val is_time_or_distance: Boolean,
            val value: String

        )
    }
}