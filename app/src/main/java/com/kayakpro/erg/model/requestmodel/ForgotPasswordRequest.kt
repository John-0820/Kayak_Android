package com.kayakpro.erg.model.requestmodel

data class ForgotPasswordRequest(

    val email:String,
    val key:String,
    val newPassword:String,

)
