package com.kayakpro.erg.model

data class GenericResponse(
    val code: String? = null,
    val body: Any? = null,
    val message: String? = null
)