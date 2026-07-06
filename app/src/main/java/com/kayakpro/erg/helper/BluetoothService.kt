package com.kayakpro.erg.helper

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import java.util.UUID

class BluetoothService(val context: Context) : Service() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val binder = LocalBinder()
    private var device: BluetoothDevice? = null

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    /**
     * Helper function to check Bluetooth connect permission based on Android version
     */
    @Suppress("DEPRECATION")
    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    @Suppress("DEPRECATION")
    fun startService(device: BluetoothDevice) {
        this.device = device
        if (!hasBluetoothConnectPermission()) {
            return
        }
        bluetoothGatt = device.connectGatt(context, true, gattCallback)
    }
    
    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    @Suppress("DEPRECATION")
    fun connectToDevice(device: BluetoothDevice) {
        this.device = device
        if (!hasBluetoothConnectPermission()) {
            return
        }
        bluetoothGatt = device.connectGatt(this, true, gattCallback)
    }
    
    @Suppress("DEPRECATION")
    fun stopService() {
        if (bluetoothGatt != null) {
            if (!hasBluetoothConnectPermission()) {
                return
            }
            bluetoothGatt!!.disconnect()
            bluetoothGatt!!.close()
            bluetoothGatt = null
        }
    }
    
    @Suppress("DEPRECATION")
    fun reconnectToDevice() {
        if (device != null) {
            if (!hasBluetoothConnectPermission()) {
                return
            }
            bluetoothGatt = device!!.connectGatt(this, true, gattCallback)
        }
    }

    @Suppress("DEPRECATION")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                println("Connected to device")
                // Discover services
                if (!hasBluetoothConnectPermission()) {
                    return
                }
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                println("Disconnected from device")
                // Try to reconnect
                reconnectToDevice()
            }
        }

        @Suppress("DEPRECATION")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val rowingService = gatt.getService(UUID.fromString("00001826-0000-1000-8000-00805F9B34FB"))
            if (rowingService != null) {
                val rowingDataCharacteristic = rowingService.getCharacteristic(UUID.fromString("00002AD1-0000-1000-8000-00805F9B34FB"))
                if (rowingDataCharacteristic != null) {
                    if (!hasBluetoothConnectPermission()) {
                        return
                    }
                    gatt.setCharacteristicNotification(rowingDataCharacteristic, true)
                    val descriptor = rowingDataCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            // Send the data to the activity using a broadcast
            val intent = Intent("BluetoothData")
            intent.putExtra("data", value)
            sendBroadcast(intent)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService {
            return this@BluetoothService
        }
    }
}
