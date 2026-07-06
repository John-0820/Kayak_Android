package com.kayakpro.erg.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefreshViewModel @Inject constructor(

):ViewModel() {
    private val _data = MutableLiveData<Int>()
    val data: LiveData<Int> get() = _data
    val watt: MutableLiveData<Int> = MutableLiveData()
    val bp: MutableLiveData<Int> = MutableLiveData()
    val strok: MutableLiveData<Int> = MutableLiveData()
    val dist: MutableLiveData<Int> = MutableLiveData()
    fun setData(value: Int) {
        _data.postValue(value)
    }
        fun refreshWatts(watts:Int){
            viewModelScope.launch {
                watt.postValue(watts)
            }
            Log.d("Spiral__","Setting value for watts is $watts")
        }
        fun refreshBPM(bpm:Int){
            viewModelScope.launch {
                bp.postValue(bpm)
            }
            Log.d("Spiral__","Setting value for bpm is $bpm")
        }
        fun refreshStroke(stroke:Int){
            viewModelScope.launch {
                strok.postValue(stroke)
            }
            Log.d("Spiral__","Setting value for stroke is $stroke")
        }
        fun refreshDistance(distance:Int){
            viewModelScope.launch {
                dist.postValue(distance)
            }
            Log.d("Spiral__","Setting value for distance is $distance")

        }


}
