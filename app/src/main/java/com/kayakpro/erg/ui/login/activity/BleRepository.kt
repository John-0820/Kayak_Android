package com.kayakpro.erg.ui.login.activity

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BleRepository {

    //distance, heartrate, watt, strokeRate, calorie, pace
    private val _distance = MutableStateFlow("0")
    val distance: StateFlow<String> = _distance

    private val _elapsedTime = MutableStateFlow("0")
    val elapsedTime: StateFlow<String> = _elapsedTime

    private val _pace = MutableStateFlow("0")
    val pace: StateFlow<String> = _pace

    private val _watt = MutableStateFlow("0")
    val watt: StateFlow<String> = _watt

    private val _calorie = MutableStateFlow("0")
    val calorie: StateFlow<String> = _calorie

    private val _heartRate = MutableStateFlow("0")
    val heartRate: StateFlow<String> = _heartRate

    private val _strokeRate = MutableStateFlow("0")
    val strokeRate: StateFlow<String> = _strokeRate

    var lastUpdateTime: Long = 0L

    private var TAG = "Wendy"
    fun updateData(
        newDistance: String,
        newElapsedTime: String,
        newPace: String,
        newWatt: String,
        newCalorie: String,
        newHeartRate: String,
        newStrokeRate: String
    ) {

        Log.d(TAG, "BleRepository*************")
        Log.d(TAG, "newDistance "+newDistance)
        Log.d(TAG, "newElapsedTime "+newElapsedTime)
        Log.d(TAG, "newPace "+newPace)
        Log.d(TAG, "newWatt "+newWatt)
        Log.d(TAG, "newCalorie "+newCalorie)
        Log.d(TAG, "newHeartRate "+newHeartRate)
        Log.d(TAG, "newStrokeRate "+newStrokeRate)
        _distance.value = newDistance
        _elapsedTime.value = newElapsedTime
        _pace.value = newPace
        _watt.value = newWatt
        _calorie.value = newCalorie
        _heartRate.value = newHeartRate
        _strokeRate.value = newStrokeRate

        lastUpdateTime = System.currentTimeMillis()
        onDataUpdated?.invoke()
    }

    /** Called on the main thread after each console packet is parsed. */
    var onDataUpdated: (() -> Unit)? = null

    fun reset() {
//        _distance.value = "0"
//        _elapsedTime = "0"
        _pace.value = "0"
        _watt.value = "0"
        _calorie.value = "0"
//        _heartRate.value = "0"
        _strokeRate.value = "0"
    }
}