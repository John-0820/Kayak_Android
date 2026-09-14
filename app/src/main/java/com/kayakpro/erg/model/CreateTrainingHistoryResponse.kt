package com.kayakpro.erg.model

data class CreateTrainingHistoryResponse(
    val code: String?,
    val message: String?,
    var body:RBody?
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
        val training_id: String?,
        val pace_per_distance: String?,
        val stroke_rate: String?,
        val time_elapsed: String?,
        val heart_rate: String?,
        val created_date: String?,

    )
}