package com.kayakpro.erg.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.ApiClient
import okhttp3.MultipartBody
import com.kayakpro.erg.network.ApiRepository
import kotlinx.coroutines.launch


class QuickStartTrainingViewModel : ViewModel() {
    private val apiRepository = ApiRepository(
        ApiClient.apiServices
    )

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