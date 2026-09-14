package com.kayakpro.erg.interfaces

import android.bluetooth.BluetoothDevice


interface onSelectDevice {
    fun onDeviceSelected(device:BluetoothDevice)
}