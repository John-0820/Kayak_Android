package com.kayakpro.erg.model.requestmodel

data class CreateAvatarRequest(

    val is_time_or_distance: Boolean,
    val value: String,
    val pace: String,
    val device_type: String,
)
//    val workout: Workout,
//
//    ) {
//    data class Workout(
//
//        val is_time_or_distance: Boolean,
//        val value: String,
//        val pace: String,
//
//    )

