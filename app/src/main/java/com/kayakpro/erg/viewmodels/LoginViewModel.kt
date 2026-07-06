package com.kayakpro.erg.viewmodels

import com.kayakpro.erg.model.AuthResponse
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.requestmodel.LoginRequest
import com.kayakpro.erg.network.ApiRepository
import com.biocube.bioaccess.session.TokenManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val app: Application,
    private val apiRepository: ApiRepository
):ViewModel() {


    val loginResponse = MutableLiveData<AuthResponse>()
    val error: MutableLiveData<String> = MutableLiveData()
    val loading = MutableLiveData<Boolean>()
    val appContext = app.applicationContext

    fun doLogin(loginRequest: LoginRequest) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.doLogin(loginRequest).let {
                    loading.postValue(false)
                    val response = it
                    if (it.isSuccessful) {
                        if (it.body()!!.code.equals("K011")) {
                            error.postValue("APP update is required")

                        }
                        else {
                            val jwt = response.body()!!.body.id_token
                            if(!jwt.isNullOrEmpty()){
                                TokenManager.saveToken(appContext, jwt)
                            }
                            loginResponse.postValue(response.body())
                        }
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
