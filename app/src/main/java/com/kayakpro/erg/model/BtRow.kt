package com.kayakpro.erg.model

import android.bluetooth.BluetoothDevice

data class BtRow(
    val device: BluetoothDevice,
    var isConnected: Boolean = false
)