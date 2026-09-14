package com.kayakpro.erg.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.kayakpro.erg.interfaces.BleManager
import com.kayakpro.erg.ui.login.activity.BleRepository
import java.util.UUID

/**
 * Centralized BLE GATT manager for the kayak console connection.
 *
 * Uses application context (not Activity) so the connection survives screen
 * navigation. Stops scanning before connect, checks GATT status codes, and
 * auto-reconnects on unexpected disconnects.
 */
object KayakBleConnectionManager {

    private const val TAG = "KayakBleConnection"

    private val FITNESS_MACHINE_SERVICE =
        UUID.fromString("00001826-0000-1000-8000-00805F9B34FB")
    private val ROWING_DATA_CHARACTERISTIC =
        UUID.fromString("00002AD1-0000-1000-8000-00805F9B34FB")
    private val CLIENT_CONFIG_DESCRIPTOR =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private const val CONNECT_TIMEOUT_MS = 20_000L
    private const val RECONNECT_BASE_DELAY_MS = 2_000L
    private const val MAX_RECONNECT_ATTEMPTS = 5
    private const val MACHINE_STOP_TIMEOUT_MS = 3_000L
    private const val GATT_CLOSE_DELAY_MS = 300L

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING
    }

    private var appContext: Context? = null
    private var targetDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var userInitiatedDisconnect = false
    private var reconnectAttempts = 0
    private var isFirstConnection = true
    private var servicesDiscovered = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private var connectTimeoutRunnable: Runnable? = null
    private var reconnectRunnable: Runnable? = null
    private var machineStopRunnable: Runnable? = null

    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /** Called on the main thread when connection state changes. */
    var onConnectionStateChanged: ((ConnectionState, String?) -> Unit)? = null

    /** Called on the main thread when a connect attempt fails permanently. */
    var onConnectionFailed: ((String) -> Unit)? = null

    /** Called on the main thread the first time a device connects successfully. */
    var onFirstConnected: (() -> Unit)? = null

    /** Invoked on the main thread right before GATT connect — use to stop BLE scan. */
    var onBeforeConnect: (() -> Unit)? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun isConnected(): Boolean =
        connectionState == ConnectionState.CONNECTED && bluetoothGatt != null

    fun connect(device: BluetoothDevice) {
        if (!hasConnectPermission()) {
            onConnectionFailed?.invoke("Bluetooth connect permission not granted")
            return
        }

        userInitiatedDisconnect = false
        reconnectAttempts = 0
        targetDevice = device
        BleManager.selectedDevice = device
        servicesDiscovered = false

        onBeforeConnect?.invoke()
        connectInternal()
    }

    fun disconnect() {
        userInitiatedDisconnect = true
        isFirstConnection = true
        cancelConnectTimeout()
        cancelReconnect()
        cancelMachineStopChecker()
        closeGatt()
        targetDevice = null
        BleManager.selectedDevice = null
        BleManager.connectedDeviceAddress = null
        BleManager.bluetoothGatt = null
        updateState(ConnectionState.DISCONNECTED, null)
    }

    fun clear() {
        disconnect()
        isFirstConnection = true
    }

    private fun connectInternal() {
        val context = appContext
        val device = targetDevice
        if (context == null || device == null) {
            onConnectionFailed?.invoke("Bluetooth not initialized")
            return
        }

        cancelConnectTimeout()
        cancelReconnect()
        closeGatt()

        updateState(
            if (reconnectAttempts > 0) ConnectionState.RECONNECTING else ConnectionState.CONNECTING,
            device.address
        )

        Log.d(TAG, "Connecting to ${device.name} (${device.address}), attempt=${reconnectAttempts + 1}")

        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            @Suppress("DEPRECATION")
            device.connectGatt(context, false, gattCallback)
        }
        BleManager.bluetoothGatt = bluetoothGatt

        startConnectTimeout()
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange status=$status newState=$newState")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        handleConnectFailure(gatt, "Connection error (status $status)")
                        return
                    }
                    onConnected(gatt)
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    cancelConnectTimeout()
                    val wasConnected = connectionState == ConnectionState.CONNECTED
                    if (bluetoothGatt == gatt) {
                        bluetoothGatt = null
                        BleManager.bluetoothGatt = null
                    }
                    closeGattInstance(gatt)

                    if (userInitiatedDisconnect || targetDevice == null) {
                        BleManager.connectedDeviceAddress = null
                        BleManager.bluetoothGatt = null
                        updateState(ConnectionState.DISCONNECTED, null)
                        return
                    }

                    if (wasConnected || connectionState == ConnectionState.CONNECTING) {
                        scheduleReconnect(status)
                    } else {
                        BleManager.connectedDeviceAddress = null
                        BleManager.bluetoothGatt = null
                        updateState(ConnectionState.DISCONNECTED, null)
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(TAG, "onMtuChanged mtu=$mtu status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS && !servicesDiscovered) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(TAG, "onServicesDiscovered status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                handleConnectFailure(gatt, "Service discovery failed (status $status)")
                return
            }
            servicesDiscovered = true
            enableRowingNotifications(gatt)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Log.d(TAG, "onDescriptorWrite status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "Failed to enable notifications (status $status)")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid != ROWING_DATA_CHARACTERISTIC) return
            parseRowingData(value)
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val value = characteristic.value ?: return
            if (characteristic.uuid != ROWING_DATA_CHARACTERISTIC) return
            parseRowingData(value)
        }
    }

    private fun onConnected(gatt: BluetoothGatt) {
        cancelConnectTimeout()
        reconnectAttempts = 0
        BleManager.connectedDeviceAddress = gatt.device.address
        BleManager.bluetoothGatt = gatt
        updateState(ConnectionState.CONNECTED, gatt.device.address)

        if (isFirstConnection) {
            isFirstConnection = false
            mainHandler.post { onFirstConnected?.invoke() }
        }

        // Request a larger MTU before service discovery on supported devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!gatt.requestMtu(517)) {
                gatt.discoverServices()
            }
        } else {
            gatt.discoverServices()
        }
    }

    private fun enableRowingNotifications(gatt: BluetoothGatt) {
        val characteristic = gatt.getService(FITNESS_MACHINE_SERVICE)
            ?.getCharacteristic(ROWING_DATA_CHARACTERISTIC)
        if (characteristic == null) {
            handleConnectFailure(gatt, "Rowing data characteristic not found")
            return
        }

        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
        if (descriptor == null) {
            handleConnectFailure(gatt, "Notification descriptor not found")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }

    private fun parseRowingData(bufferValue: ByteArray) {
        if (bufferValue.size < 20) return

        val uint8Data = bufferValue.map { it.toInt() and 0xFF }
        val strokeRate = uint8Data[2] / 2
        val distance = uint8Data[5] or (uint8Data[6] shl 8) or (uint8Data[7] shl 16)
        val paceRaw = uint8Data[8] or (uint8Data[9] shl 8)
        val instPower = uint8Data[10] or (uint8Data[11] shl 8)
        val energyKJ = uint8Data[12] or (uint8Data[13] shl 8)
        val kcal = energyKJ
        val heartRate = uint8Data[17]
        val elapsedTime = uint8Data[18] or (uint8Data[19] shl 8)

        BleRepository.updateData(
            distance.toString(),
            elapsedTime.toString(),
            paceRaw.toString(),
            instPower.toString(),
            kcal.toString(),
            heartRate.toString(),
            strokeRate.toString()
        )
        scheduleMachineStopChecker()
    }

    private fun scheduleMachineStopChecker() {
        cancelMachineStopChecker()
        machineStopRunnable = Runnable { BleRepository.reset() }
        mainHandler.postDelayed(machineStopRunnable!!, MACHINE_STOP_TIMEOUT_MS)
    }

    private fun cancelMachineStopChecker() {
        machineStopRunnable?.let { mainHandler.removeCallbacks(it) }
        machineStopRunnable = null
    }

    private fun handleConnectFailure(gatt: BluetoothGatt, reason: String) {
        Log.w(TAG, reason)
        closeGattInstance(gatt)
        BleManager.bluetoothGatt = null

        if (userInitiatedDisconnect) {
            updateState(ConnectionState.DISCONNECTED, null)
            return
        }

        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            scheduleReconnect(BluetoothGatt.GATT_FAILURE)
        } else {
            BleManager.connectedDeviceAddress = null
            updateState(ConnectionState.DISCONNECTED, null)
            mainHandler.post { onConnectionFailed?.invoke(reason) }
        }
    }

    private fun scheduleReconnect(disconnectStatus: Int) {
        if (userInitiatedDisconnect || targetDevice == null) return

        reconnectAttempts++
        if (reconnectAttempts > MAX_RECONNECT_ATTEMPTS) {
            Log.w(TAG, "Max reconnect attempts reached")
            BleManager.connectedDeviceAddress = null
            updateState(ConnectionState.DISCONNECTED, null)
            mainHandler.post {
                onConnectionFailed?.invoke("Connection lost. Please try again.")
            }
            return
        }

        // Status 133 (0x85) is a common transient Android BLE error — wait longer
        val delay = if (disconnectStatus == 133) {
            RECONNECT_BASE_DELAY_MS * reconnectAttempts * 2
        } else {
            RECONNECT_BASE_DELAY_MS * reconnectAttempts
        }

        Log.d(TAG, "Scheduling reconnect in ${delay}ms (attempt $reconnectAttempts)")
        updateState(ConnectionState.RECONNECTING, targetDevice?.address)

        cancelReconnect()
        reconnectRunnable = Runnable {
            if (!userInitiatedDisconnect && targetDevice != null) {
                connectInternal()
            }
        }
        mainHandler.postDelayed(reconnectRunnable!!, delay)
    }

    private fun startConnectTimeout() {
        cancelConnectTimeout()
        connectTimeoutRunnable = Runnable {
            if (connectionState == ConnectionState.CONNECTING ||
                connectionState == ConnectionState.RECONNECTING
            ) {
                Log.w(TAG, "Connection timed out")
                closeGatt()
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && !userInitiatedDisconnect) {
                    scheduleReconnect(BluetoothGatt.GATT_FAILURE)
                } else {
                    BleManager.connectedDeviceAddress = null
                    updateState(ConnectionState.DISCONNECTED, null)
                    onConnectionFailed?.invoke("Connection timed out. Make sure the kayak is powered on and nearby.")
                }
            }
        }
        mainHandler.postDelayed(connectTimeoutRunnable!!, CONNECT_TIMEOUT_MS)
    }

    private fun cancelConnectTimeout() {
        connectTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        connectTimeoutRunnable = null
    }

    private fun cancelReconnect() {
        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null
    }

    private fun closeGatt() {
        val gatt = bluetoothGatt
        bluetoothGatt = null
        BleManager.bluetoothGatt = null
        closeGattInstance(gatt)
    }

    private fun closeGattInstance(gatt: BluetoothGatt?) {
        if (gatt == null) return
        try {
            gatt.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Error disconnecting GATT", e)
        }
        // Brief delay before close helps avoid status-133 on immediate reconnect (Samsung, Xiaomi)
        mainHandler.postDelayed({
            try {
                gatt.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing GATT", e)
            }
        }, GATT_CLOSE_DELAY_MS)
    }

    private fun updateState(state: ConnectionState, address: String?) {
        connectionState = state
        mainHandler.post { onConnectionStateChanged?.invoke(state, address) }
    }

    private fun hasConnectPermission(): Boolean {
        val context = appContext ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
