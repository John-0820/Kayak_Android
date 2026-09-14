package com.kayakpro.erg.viewmodels// MyViewModel.kt
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayakpro.erg.helper.HistoryTypeCache
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.CreateTrainingHistoryResponse
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.network.ApiRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel@Inject constructor(
    private val apiRepository: ApiRepository
): ViewModel() {

    // Private mutable live data to store the received data
    val strokeRate = MutableLiveData<String>()
    val bpm = MutableLiveData<String>()
    val watts = MutableLiveData<String>()
    val device = MutableLiveData<BluetoothDevice>()
    var isDeviceConnected = MutableLiveData<Boolean>()
    val uploadData = MutableLiveData<CreateTrainingHistoryResponse>()
    val isDownloaded = MutableLiveData<Boolean>()
    val error: MutableLiveData<String> = MutableLiveData()
    val loading = MutableLiveData<Boolean>()
    // Public live data to expose the received data
//    val strokeRate: LiveData<String> = _strokeRate    // Public live data to expose the received data
//    val bpm: LiveData<String> = _bpm    // Public live data to expose the received data
//    val watts: LiveData<String> = _watts
//    val device: LiveData<BluetoothDevice> = _device

    // Function to update the received data
    fun updateData(data: List<Int>?) {
        strokeRate.postValue((data!!.get(2)/2).toString())
        bpm.postValue( data.get(data.size-3).toString())
        watts.postValue(data.get(10).toString())
        // runOnUiThread { binding.tvdata.text= "BPM is : "++"RPM is: ${}"+"Watt is: ${}" +
        Log.d("Analysis__","updated data in viewmodel is ${strokeRate.value} ")
    }

    // Function to clear the received data


    fun currentDevice(_device: BluetoothDevice){
        device.postValue(_device)
    }
    // Function to get the current data
    fun getCurrentDevice(): BluetoothDevice? {
        return device.value
    }


    fun updateDeviceStatus(isConnected:Boolean) {
        isDeviceConnected.postValue(isConnected)
    }

    fun createTrainingHistory(createProgramRequest: CreateTrainingHistoryRequest, token:String, displayTypeOverride: String? = null) {
        viewModelScope.launch {
            loading.postValue(true)
            Log.d("MyViewModel", "createTrainingHistory request json: ${Gson().toJson(createProgramRequest)}")
            try {
                apiRepository.createTrainingHistory(createProgramRequest,token).let {
                    loading.postValue(false)
                    val response = it
                    Log.d("MyViewModel", "createTrainingHistory response: code=${it.code()}, isSuccessful=${it.isSuccessful}, body=${it.body()}, error=${it.errorBody()?.string()}")
                    if (it.isSuccessful) {
                        response.body()?.body?.let { rbody ->
                            rbody.id?.let { id ->
                                val type = displayTypeOverride ?: rbody.type ?: createProgramRequest.type
                                HistoryTypeCache.setCachedType(id.toString(), type)
                                Log.d("MyViewModel", "Cached history type: id=$id, type=$type")
                            }
                        }
                        uploadData.postValue(response.body())
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