package com.kayakpro.erg

import com.kayakpro.erg.network.ApiServices
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://3.217.56.174:8080/")
//        .baseUrl("http://10.0.2.2:8080/")
//            .baseUrl("http://localhost:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiServices: ApiServices =
        retrofit.create(ApiServices::class.java)
}