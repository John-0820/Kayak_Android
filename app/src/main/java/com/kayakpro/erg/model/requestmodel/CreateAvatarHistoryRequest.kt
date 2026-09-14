package com.kayakpro.erg.model.requestmodel

data class CreateAvatarHistoryRequest(

    val type: String,
    val device_type: String,
    val distance: String,
    val pace: String,
    val strokeRate: String,
    val maxSpeed: String,
    val time: String,
    val timeElapsed: String,
    val calories: String,
    val watts: String,
    val heartRate: String,
//    val userId: Int,
    val isTimeOrDistance: Boolean,
    val value: String,
    val paceForBoat: String,

    )