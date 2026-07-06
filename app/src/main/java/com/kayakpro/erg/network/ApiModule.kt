package com.kayakpro.erg.network


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.Interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.kayakpro.erg.BuildConfig

import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {


    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        val OkHttpClient = OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(10, 60, TimeUnit.SECONDS))
            .addInterceptor(Interceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("app_version", BuildConfig.VERSION_NAME)
                  //  .addHeader("apiKeyMobile", BuildConfig.server_secret_key)
                    //  .addHeader("Authorization", "Bearer ${SesssionManager.getInstance()!!.getAToken()}")

                    .build()
                ////Log.d("Token","usertoken  : ${SesssionManager.getInstance()!!.getToken()}")

                chain.proceed(request)
            })
            .addInterceptor(logging)

        return OkHttpClient.build()

    }

    @Singleton
    @Provides
    fun provideConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient, gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://3.217.56.174:8080/")
//            .baseUrl("http://10.0.2.2:8080/")
//            .baseUrl("http://localhost:8080/")
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory).build()
    }

    @Singleton
    @Provides
    fun provideCurrencyService(retrofit: Retrofit): ApiServices =
        retrofit.create(ApiServices::class.java)

    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit.Builder {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
    }

}