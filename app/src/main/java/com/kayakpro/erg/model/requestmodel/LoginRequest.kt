package com.kayakpro.erg.model.requestmodel

data class LoginRequest(

    val username:String,
    val email:String,
    val password:String,
    val subscribeNewsLetter:Boolean=true,
    val langKey:String="en"
)
