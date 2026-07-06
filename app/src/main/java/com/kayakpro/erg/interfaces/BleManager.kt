package com.kayakpro.erg.interfaces

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import androidx.annotation.RequiresPermission
import com.kayakpro.erg.model.BtRow

object BleManager {
    var selectedDevice: BluetoothDevice? = null
    var bluetoothGatt: BluetoothGatt? = null
    val btRows: MutableList<BtRow> = mutableListOf()
    var connectedDeviceAddress: String? = null
    var isFirstConnection: Boolean = true

    fun isConnected(): Boolean =
        bluetoothGatt != null && connectedDeviceAddress != null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun clear() {
        btRows.clear()
        com.kayakpro.erg.bluetooth.KayakBleConnectionManager.clear()
    }
}
