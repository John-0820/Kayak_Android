package com.kayakpro.erg.viewmodels

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.HistoryResponseModel
import com.kayakpro.erg.model.TrainingData
import com.kayakpro.erg.model.TrainingResponseModel
import com.kayakpro.erg.network.ApiRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    val historyResponse = MutableLiveData<HistoryResponseModel>()
    val trainingResponse = MutableLiveData<TrainingResponseModel>()
    val trainingDataResponse = MutableLiveData<TrainingData>()
    val deletedTrainingId = MutableLiveData<String>()
    val isDeleted = MutableLiveData<String?>()
    val isDownloaded = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    val loading = MutableLiveData<Boolean>()

    fun getHistory(deviceType: String, token: String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                val response = apiRepository.getHistory(deviceType, token)
                loading.postValue(false)
                Log.d("HistoryViewModel", "getHistory response: isSuccessful=${response.isSuccessful}, code=${response.code()}, body=${response.body()}, error=${response.errorBody()?.string()}")
                if (response.isSuccessful) {
                    response.body()?.let { historyResponse.postValue(it) }
                } else {
                    handleApiError(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                loading.postValue(false)
                Log.e("HistoryViewModel", "getHistory failed", e)
                error.postValue(e.message)
            }
        }
    }

    fun getTraining(deviceType: String, token: String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                val response = apiRepository.getTrainings(deviceType, token)
                loading.postValue(false)
                if (response.isSuccessful) {
                    response.body()?.let { trainingResponse.postValue(it) }
                } else {
                    handleApiError(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                loading.postValue(false)
                Log.e("HistoryViewModel", "getTraining failed", e)
                error.postValue(e.message)
            }
        }
    }

    private fun removeDeletedItem(id: String) {
        val current = historyResponse.value ?: return
        val updatedList = current.body?.filter { it.id != id }
        current.body = updatedList

        historyResponse.postValue(current)
    }
    fun deleteTraining(id: String, token: String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                apiRepository.deleteTrainingHistory(id,token).let {
                    val response = it
                    if (it.isSuccessful) {
                        removeDeletedItem(id)
                        loading.postValue(false)
                        isDeleted.postValue("History deleted successfully")
                    }
                    else if (it.code()==400){
                        val errorResponse = response.errorBody()?.let { ErrorResponseJsonConverter.fromJson(it.string()) }
                        loading.postValue(false)
                        errorResponse?.let { error.postValue(Utils.handleErrorResponse(it)) }
                    }
                    else {
                        val res = Utils.getError(it)
                        val respons = Gson().toJson(res)
                        loading.postValue(false)
                        error.postValue(respons)
                    }
                }
            } catch (e: Exception) {
                loading.postValue(false)
                error.postValue(e.message)
            }
        }
    }
    fun getTrainingData(id: String, deviceType: String, token: String) {
        viewModelScope.launch {
            try {
                val response = apiRepository.getTrainingData(id, deviceType, token)
                if (response.isSuccessful) {
                    response.body()?.let { trainingDataResponse.postValue(it) }
                } else {
                    handleApiError(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "getTrainingData failed", e)
                error.postValue(e.message)
            }
        }
    }

    fun getFitFile(id: Int, token: String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                val response = apiRepository.getFitFile(id.toString(), token)
                loading.postValue(false)
                if (response.isSuccessful) {
                    response.body()?.byteStream()?.let { inputStream ->
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val file = File(downloadsDir, "downloaded_fit_file_${System.currentTimeMillis()}.fit")
                        FileOutputStream(file).use { output -> inputStream.copyTo(output) }
                        isDownloaded.postValue(true)
                    }
                } else {
                    handleApiError(response.code(), response.errorBody()?.string())
                }
            } catch (e: Exception) {
                loading.postValue(false)
                Log.e("HistoryViewModel", "getFitFile failed", e)
                error.postValue(e.message)
            }
        }
    }

    private fun handleApiError(code: Int, errorBody: String?) {
        try {
            if (!errorBody.isNullOrEmpty()) {
                val parsedError = Gson().fromJson(errorBody, Map::class.java)
                error.postValue(parsedError.toString())
            } else {
                error.postValue("Error code $code")
            }
        } catch (e: Exception) {
            Log.e("HistoryViewModel", "Error parsing API error", e)
            error.postValue("Unexpected server error")
        }
    }
}
