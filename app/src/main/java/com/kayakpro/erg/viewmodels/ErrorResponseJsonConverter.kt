package com.kayakpro.erg.viewmodels

import com.kayakpro.erg.model.ErrorResponse
import com.google.gson.Gson

class ErrorResponseJsonConverter {
    companion object {
        fun fromJson(json: String): ErrorResponse? {
            val gson = Gson()
            return gson.fromJson(json, ErrorResponse::class.java)
        }
    }
}