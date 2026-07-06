package com.kayakpro.erg.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.TrainingResponseModel
import com.kayakpro.erg.network.ApiRepository

import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingListViewModel @Inject constructor(
    private val apiRepository: ApiRepository
):ViewModel() {

    val trainingResponse = MutableLiveData<TrainingResponseModel>()
    val error: MutableLiveData<String> = MutableLiveData()
    val isDeleted= MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    fun getTraining(device_type: String,token:String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.getTrainings(device_type,token).let {
                    loading.postValue(false)
                    val response = it
                    if (it.isSuccessful) {
                        trainingResponse.postValue(response.body())
                    }
                    else if (it.code()==400){
                        val errorResponse = response.errorBody()?.let { ErrorResponseJsonConverter.fromJson(it.string()) }

                        errorResponse?.let { error.postValue(Utils.handleErrorResponse(it)) }
//                        //Log.d("Analysis__","In error code ${it.}")
                    }
                    else {
                     //   val res = Utility.getError(it)
                       // val respons = Gson().toJson(res)
                      //  error.postValue(res.message)
                        val res = Utils.getError(it)
                        val respons = Gson().toJson(res)
                        error.postValue(respons)
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
    fun deleteTraining(id: String,token:String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.deleteTraining(id,token).let {
                    loading.postValue(false)
                    val response = it
                    if (it.isSuccessful) {
                        isDeleted.postValue(true)
                    }
                    else if (it.code()==400){
                        val errorResponse = response.errorBody()?.let { ErrorResponseJsonConverter.fromJson(it.string()) }

                        errorResponse?.let { error.postValue(Utils.handleErrorResponse(it)) }
//                        //Log.d("Analysis__","In error code ${it.}")
                    }
                    else {
                     //   val res = Utility.getError(it)
                       // val respons = Gson().toJson(res)
                      //  error.postValue(res.message)
                        val res = Utils.getError(it)
                        val respons = Gson().toJson(res)
                        error.postValue(respons)
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


}
