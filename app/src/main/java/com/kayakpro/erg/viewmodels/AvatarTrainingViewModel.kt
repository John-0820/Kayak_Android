package com.kayakpro.erg.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.CreateProgResponseModel
import com.kayakpro.erg.model.requestmodel.CreateProgramRequest
import com.kayakpro.erg.model.requestmodel.CreateAvatarRequest
import okhttp3.MultipartBody
import com.kayakpro.erg.network.ApiRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private val TAG = "Wendy"

@HiltViewModel
class AvatarTrainingViewModel @Inject constructor(
    private val apiRepository: ApiRepository
):ViewModel() {
    val loading = MutableLiveData<Boolean>()
    val createProgResponse = MutableLiveData<CreateProgResponseModel>()
    val isDownloaded = MutableLiveData<Boolean>()
    val error: MutableLiveData<String> = MutableLiveData()

    fun createAvatarTraining(createAvatarRequest: CreateAvatarRequest, token: String) {
        Log.d(TAG, "createAvatarTraining");
        viewModelScope.launch {
            loading.postValue(true)
            try {
                Log.d("FitFile","Inside createAvatarTraining")
                apiRepository.createAvatarTraining(createAvatarRequest, token).let {
                    loading.postValue(false)
                    val response = it
                    if (it.isSuccessful) {
                        createProgResponse.postValue(response.body())
                        Log.d(TAG, "response.body()    "+response.body());
                    }
                    else {
                        val res = Utils.getError(it)
                        Log.d(TAG, "createAvatarTraining error: code=${it.code()}, message=${res.message}, errorBody=${it.errorBody()?.string()}")
                        error.postValue(res.message)
                    }
                }
            } catch (e: Exception) {
                loading.postValue(false)
                e.printStackTrace()
            }
        }
    }

    fun createProgram(createProgramRequest: CreateProgramRequest,token:String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.createProgram(createProgramRequest,token).let {
                    loading.postValue(false)
                    val response = it
                    if (it.isSuccessful) {
                        createProgResponse.postValue(response.body())
                    }

                    else {
                        //   val res = Utility.getError(it)
                        // val respons = Gson().toJson(res)
                        //  error.postValue(res.message)
                        val res = Utils.getError(it)
                        val respons = Gson().toJson(res)
                        error.postValue(res.message)
                        //  error.postValue("Error is there")
                    }
                }
            } catch (e: Exception) {
                loading.postValue(false)
                e.printStackTrace()
                if (!e.message.equals("Job was cancelled"))
                {
                    error.postValue(e.message)
                }
            }

        }
    }

    fun sendFitFile(token: String, file: MultipartBody.Part) {
        val appVersion = "1.0.1"
        viewModelScope.launch {
            try {
                Log.d("FitFile", "api successfully called")
                apiRepository.sendFitFileByEmail(
                    token = "Bearer $token",
                    appVersion = appVersion,
                    file = file
                )
            } catch (e: Exception) {
                Log.d("FitFile", "api failed")
                e.printStackTrace()
            }
        }
    }
}