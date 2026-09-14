package com.kayakpro.erg.network

import com.kayakpro.erg.model.requestmodel.CreateAvatarRequest
import com.kayakpro.erg.model.requestmodel.CreateProgramRequest
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.model.requestmodel.ForgotPasswordRequest
import com.kayakpro.erg.model.requestmodel.LoginRequest
import okhttp3.MultipartBody

import javax.inject.Inject

class ApiRepository @Inject constructor(
    private val apiServices: ApiServices
) {

    suspend fun doLogin(loginRequest: LoginRequest) = apiServices.doLogin(loginRequest)
    suspend fun doRegister(loginRequest: LoginRequest) = apiServices.doRegister(loginRequest)
    suspend fun resetPswd(jsonObject: ForgotPasswordRequest) = apiServices.resetPswd(jsonObject)
    suspend fun finishResetPswd(jsonObject: ForgotPasswordRequest) = apiServices.finishResetPswd(jsonObject)
    suspend fun getHistory(jsonObject: String,token:String) = apiServices.getTrainingHistory(jsonObject,token)
    suspend fun getTrainings(jsonObject: String,token:String) = apiServices.getTrainings(jsonObject,token)
    suspend fun getTrainingData(id:String, jsonObject: String,token:String) = apiServices.getTrainingData(id,jsonObject,token)
    suspend fun deleteTrainingHistory(id: String, token: String) = apiServices.deleteTrainingHistory(id, token)
    suspend fun deleteTraining(id: String,token:String) = apiServices.deleteTraining(id,token)
    suspend fun getFitFile(id:String,token:String) = apiServices.downloadFitFile(id,token)
    suspend fun createAvatarTraining(createAvatarRequest: CreateAvatarRequest, token:String) = apiServices.createAvatarTraining(createAvatarRequest,token)
    suspend fun createProgram(createProgramRequest: CreateProgramRequest,token:String) = apiServices.createTraining(createProgramRequest,token)
    suspend fun editProgram(id:String,createProgramRequest: CreateProgramRequest,token:String) = apiServices.updateTraining(id,createProgramRequest,token)
    suspend fun createTrainingHistory(createProgramRequest: CreateTrainingHistoryRequest,token:String) = apiServices.createTrainingHistory(createProgramRequest,token)
    suspend fun sendFitFileByEmail(token: String, appVersion: String, file: MultipartBody.Part) = apiServices.sendFitFileByEmail(token, appVersion, file)
    suspend fun deleteAccount(token: String) = apiServices.deleteAccount(token)


}