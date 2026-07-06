package com.kayakpro.erg.helper

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.IBinder

class BluetoothManager_(private val context: Context) {
    private var bluetoothService: BluetoothService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bluetoothService = null
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (bluetoothService != null) {
            bluetoothService!!.connectToDevice(device)
        } else {
            // Start the service and connect to the device
            val intent = Intent(context, BluetoothService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            bluetoothService!!.connectToDevice(device)
        }
    }

    fun reconnectToDevice() {
        if (bluetoothService != null) {
            bluetoothService!!.reconnectToDevice()
        }
    }

    fun registerReceiver(receiver: BroadcastReceiver) {
        context.registerReceiver(receiver, IntentFilter("BluetoothData"))
    }

    fun unregisterReceiver(receiver: BroadcastReceiver) {
        context.unregisterReceiver(receiver)
    }
}