package com.kayakpro.erg.model

// Success response
data class AuthResponse(
    val id_token: String,
    val code: String,
    val refresh_token: String,
    val body:Details

){
     data class Details(
        val user_id:String,
        val id_token:String,
        val refresh_token:String,
    )
}

// Error response
data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldError>
)

data class FieldError(
    val objectName: String,
    val field: String,
    val message: String
)