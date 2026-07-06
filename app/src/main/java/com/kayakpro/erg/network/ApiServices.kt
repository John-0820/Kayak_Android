package com.kayakpro.erg.network


import com.kayakpro.erg.model.AuthResponse
import com.kayakpro.erg.model.CreateProgResponseModel
import com.kayakpro.erg.model.CreateTrainingHistoryResponse
import com.kayakpro.erg.model.DeleteAccountResponse
import com.kayakpro.erg.model.GenericResponse
import com.kayakpro.erg.model.HistoryResponseModel
import com.kayakpro.erg.model.TrainingData
import com.kayakpro.erg.model.TrainingResponseModel
import com.kayakpro.erg.model.requestmodel.CreateAvatarRequest
import com.kayakpro.erg.model.requestmodel.CreateProgramRequest
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.model.requestmodel.ForgotPasswordRequest
import com.kayakpro.erg.model.requestmodel.LoginRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody

import retrofit2.Response
import retrofit2.http.*

interface ApiServices {



    @POST(Constants.LOGIN)
    suspend fun doLogin(@Body requestLogin: LoginRequest): Response<AuthResponse>
    @POST(Constants.REGISTER)
    suspend fun doRegister(@Body requestLogin: LoginRequest): Response<AuthResponse>
    @POST(Constants.FORGOTPSWD)
    suspend fun resetPswd(@Body requestLogin: ForgotPasswordRequest): Response<AuthResponse>
    @POST(Constants.FORGOTPSWDFINISH)
    suspend fun finishResetPswd(@Body requestLogin: ForgotPasswordRequest): Response<AuthResponse>

    @POST(Constants.CREATE_AVATAR_TRAINING)
    suspend fun createAvatarTraining(@Body createAvatarRequest: CreateAvatarRequest, @Header("Authorization") token:String): Response<CreateProgResponseModel>
    @POST(Constants.CREATE_PROGRAM_TRAINING)
    suspend fun createTraining(@Body createProgramRequest: CreateProgramRequest, @Header("Authorization") token:String): Response<CreateProgResponseModel>
    @PUT(Constants.CREATE_PROGRAM_TRAINING)
    suspend fun editTraining(@Body createProgramRequest: CreateProgramRequest, @Header("Authorization") token:String): Response<CreateProgResponseModel>
    @POST(Constants.CREATE_TRAINING_HISTORY)
    suspend fun createTrainingHistory(@Body createTrainingHistory: CreateTrainingHistoryRequest, @Header("Authorization") token:String): Response<CreateTrainingHistoryResponse>
    @PUT(Constants.UPDATE_PROGRAM_TRAINING+"/{id}")
    suspend fun updateTraining(@Path("id") id: String,@Body createProgramRequest: CreateProgramRequest, @Header("Authorization") token:String): Response<CreateProgResponseModel>

    @GET(Constants.GET_TRAINING)
    suspend fun getTrainings(@Query ("device_type") deviceType: String, @Header("Authorization") token:String): Response<TrainingResponseModel>

    @GET(Constants.GET_TRAINING_Data+"{id}")
    suspend fun getTrainingData(@Path("id") id:String,@Query ("device_type") deviceType: String, @Header("Authorization") token:String): Response<TrainingData>

    @DELETE(Constants.DELETE_TRAINING_HISTORY+"/{id}")
    suspend fun deleteTrainingHistory(@Path("id") id: String,@Header("Authorization") token: String): Response<ResponseBody>
    @DELETE(Constants.DELETE_TRAINING+"/{id}")
    suspend fun deleteTraining(@Path("id") id: String,@Header("Authorization") token: String): Response<ResponseBody>

    @GET(Constants.GET_FITFILE+"{id}/fit-file")
    suspend fun downloadFitFile(@Path("id") id: String,@Header("Authorization") token: String): Response<ResponseBody>

    @GET(Constants.GET_TRAINING_HISTORY)
    suspend fun getTrainingHistory(@Query ("device_type") deviceType: String, @Header("Authorization") token:String): Response<HistoryResponseModel>

    @Multipart
    @POST("api/email/send-fit")
    suspend fun sendFitFileByEmail(
        @Header("Authorization") token: String,
        @Header("app_version") appVersion: String,
        @Part file: MultipartBody.Part
    ): Response<GenericResponse>

    @DELETE("api/account")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<DeleteAccountResponse>


    /*  @GMultipartBodyET("${Constants.GET_TRAINING_HISTORY}+${}")
      suspend fun getProgramTrainings(@Body createProgramRequest: CreateProgramRequest): Response<AuthResponse>*/






}