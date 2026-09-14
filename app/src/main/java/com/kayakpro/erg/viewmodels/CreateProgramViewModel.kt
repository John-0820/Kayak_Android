package com.kayakpro.erg.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.CreateProgResponseModel
import com.kayakpro.erg.model.requestmodel.CreateProgramRequest
import com.kayakpro.erg.network.ApiRepository

import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProgramViewModel @Inject constructor(
    private val apiRepository: ApiRepository
):ViewModel() {

    val createProgResponse = MutableLiveData<CreateProgResponseModel>()
    val isDownloaded = MutableLiveData<Boolean>()
    val error: MutableLiveData<String> = MutableLiveData()
    val loading = MutableLiveData<Boolean>()


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
    fun editProgram(id:String,createProgramRequest: CreateProgramRequest,token:String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.editProgram(id,createProgramRequest,token).let {
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



}
