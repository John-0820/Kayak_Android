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
import java.util.Collections
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

    companion object {
        private val pendingDeleteIDs: MutableSet<String> =
            Collections.synchronizedSet(mutableSetOf())
    }

    fun getHistory(deviceType: String, token: String) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                val response = apiRepository.getHistory(deviceType, token)
                loading.postValue(false)
                Log.d("HistoryViewModel", "getHistory response: isSuccessful=${response.isSuccessful}, code=${response.code()}, body=${response.body()}, error=${response.errorBody()?.string()}")
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        Log.d("HistoryViewModel", "getHistory raw JSON: ${Gson().toJson(responseBody)}")
                        responseBody.body?.forEachIndexed { idx, item ->
                            Log.d("HistoryViewModel", "history item[$idx]: id=${item.id}, type=${item.type}, name=${item.name}, distance=${item.distance}, speed=${item.speed}, calories=${item.calories}, heart_rate=${item.heart_rate}, stroke_rate=${item.stroke_rate}, watts=${item.watts}, time=${item.time}, time_elapsed=${item.time_elapsed}, pace_per_distance=${item.pace_per_distance}, training_id=${item.training_id}, created_date=${item.created_date}")
                        }
                        val tombstones = synchronized(pendingDeleteIDs) { pendingDeleteIDs.toSet() }
                        if (tombstones.isNotEmpty()) {
                            responseBody.body = responseBody.body?.filter { !tombstones.contains(it.id) }
                        }
                        historyResponse.postValue(responseBody)
                    }
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
        Log.d("HistoryViewModel", "deleteTraining REQUEST: method=DELETE, endpoint=api/training-histories/$id, id=$id, token=$token")
        removeDeletedItem(id)
        synchronized(pendingDeleteIDs) { pendingDeleteIDs.add(id) }
        isDeleted.postValue("History deleted successfully")
        viewModelScope.launch {
            try {
                val response = apiRepository.deleteTrainingHistory(id, token)
                Log.d("HistoryViewModel", "DELETE response: id=$id, code=${response.code()}, isSuccessful=${response.isSuccessful}, body=${response.body()?.string()}, error=${response.errorBody()?.string()}")
                if (response.isSuccessful) {
                    Log.d("HistoryViewModel", "DELETE id=$id succeeded (${response.code()})")
                    synchronized(pendingDeleteIDs) { pendingDeleteIDs.remove(id) }
                } else {
                    Log.w("HistoryViewModel", "DELETE id=$id failed: code=${response.code()}, error=${response.errorBody()?.string()}")
                    synchronized(pendingDeleteIDs) { pendingDeleteIDs.remove(id) }
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "DELETE id=$id exception", e)
                synchronized(pendingDeleteIDs) { pendingDeleteIDs.remove(id) }
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
