package com.kayakpro.erg.viewmodels

import com.kayakpro.erg.model.AuthResponse
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.requestmodel.LoginRequest
import com.kayakpro.erg.network.ApiRepository

import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiRepository: ApiRepository
):ViewModel() {


    val signupResponse = MutableLiveData<AuthResponse>()
    val error: MutableLiveData<String> = MutableLiveData()
    val loading = MutableLiveData<Boolean>()

    fun doRegister(loginRequest: LoginRequest) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.doRegister(loginRequest).let {
                    loading.postValue(false)
                    val response=it
                    if (it.isSuccessful) {
                        signupResponse.postValue(it.body())
                    }
                    else if (it.code()==400){
                        val errorResponse = response.errorBody()?.let { ErrorResponseJsonConverter.fromJson(it.string()) }

                        errorResponse?.let { error.postValue(Utils.handleErrorResponse(it)) }
//                        //Log.d("Analysis__","In error code ${it.}")
                    }
                    else {
                        val res = Utils.getError(it)
                        val respons = Gson().toJson(res)
                       error.postValue(res.message)

                      // error.postValue("Error is there")
                        //Log.d("Analysis__",Gson().toJson(it.errorBody()))

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