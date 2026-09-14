package com.kayakpro.erg.model.requestmodel

data class CreateProgramRequest(

    val name: String,
    val details: String,
    val device_type: String,
    val workouts: ArrayList<Workout>,

    ) {
    data class Workout(

        val name: String,
        val is_time_or_distance: Boolean,
        val value: String,
        val is_rest: Int

    )
}
