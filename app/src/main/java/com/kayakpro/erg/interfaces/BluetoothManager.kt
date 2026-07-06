package com.kayakpro.erg.interfaces

import android.bluetooth.BluetoothDevice

interface BluetoothInterface {
    fun updateDeviceData(device: List<Byte>)
    fun getDeviceData(): BluetoothDevice?
}

