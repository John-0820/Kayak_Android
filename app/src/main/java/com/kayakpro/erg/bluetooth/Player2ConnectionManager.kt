package com.kayakpro.erg.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.kayakpro.erg.ui.login.activity.BleRepository
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Manages peer-to-peer Bluetooth connection between two players
 * Handles connection, data sync, and training start coordination
 */
object Player2ConnectionManager {
    
    private const val TAG = "Player2Connection"
    private const val SERVICE_NAME = "KayakProPlayer2"
    private val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
    
    // Connection state
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private val sendLock = Any()
    
    var isConnected = false
        private set
    /** true = this phone sent the connection request and may start training */
    var isInitiator = false
        private set
    var isServer = false // true = waiting for connection, false = initiating connection
        private set
    var connectedDevice: BluetoothDevice? = null
        private set

    /** Console readings captured when the loading countdown ends — shared start line for both athletes. */
    var workoutFirstDistance: Int = 0
        private set
    var workoutFirstElapsedTime: Int = 0
        private set
    private var workoutBaselineCaptured = false
    // Callbacks
    var onConnectionRequest: ((BluetoothDevice) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null
    var onConnectionFailed: ((String) -> Unit)? = null
    var onTrainingStartReceived: ((Bundle: android.os.Bundle) -> Unit)? = null
    var onDataReceived: ((TrainingData) -> Unit)? = null
    var onGoalReachedReceived: (() -> Unit)? = null
    
    // Training data model
    data class TrainingData(
        val distance: String = "0",
        val time: String = "0",
        val pace: String = "0",
        val strokeRate: String = "0",
        val calories: String = "0",
        val heartRate: String = "0",
        val watts: String = "0",
        val remainingGoal: String = "0",
        val distanceGap: String = "0"
    )
    
    // Message protocol
    private const val MSG_CONNECTION_REQUEST = "CONN_REQ"
    private const val MSG_CONNECTION_ACCEPT = "CONN_ACCEPT"
    private const val MSG_CONNECTION_REJECT = "CONN_REJECT"
    private const val MSG_TRAINING_START = "TRAINING_START"
    private const val MSG_TRAINING_DATA = "TRAINING_DATA"
    private const val MSG_GOAL_REACHED = "GOAL_REACHED"
    private const val MSG_DISCONNECT = "DISCONNECT"
    
    /**
     * Helper function to check Bluetooth connect permission based on Android version
     */
    @Suppress("DEPRECATION")
    private fun hasBluetoothConnectPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun initialize(adapter: BluetoothAdapter?) {
        bluetoothAdapter = adapter
    }
    
    /**
     * Start server mode - wait for incoming connections from other player
     * Server keeps running to accept multiple connection attempts
     */
    @Suppress("DEPRECATION")
    fun startServerMode(context: Context) {
        // Check permissions based on Android version
        if (!hasBluetoothConnectPermission(context)) {
            Log.e(TAG, "Missing Bluetooth permission for server mode")
            return
        }
        
        // Don't start multiple servers
        if (serverSocket != null) {
            Log.d(TAG, "Server already running")
            return
        }
        
        isServer = true
        isInitiator = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use secure RFCOMM (requires pairing but more reliable)
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    SERVICE_NAME,
                    SERVICE_UUID
                )
                Log.d(TAG, "=== Server Started ===")
                Log.d(TAG, "Service: $SERVICE_NAME")
                Log.d(TAG, "UUID: $SERVICE_UUID")
                Log.d(TAG, "Mode: Secure RFCOMM (requires pairing)")
                
                // Keep accepting connections in a loop
                while (serverSocket != null && !isConnected) {
                    try {
                        Log.d(TAG, "Waiting for incoming connection...")
                        // This blocks until connection received
                        val socket = serverSocket?.accept()
                        
                        socket?.let {
                            val deviceName = it.remoteDevice.name ?: "Unknown Device"
                            Log.d(TAG, "✓ Incoming connection from $deviceName (${it.remoteDevice.address})")
                            handleServerConnection(it)
                            // Don't break - keep server running for future connections
                        }
                    } catch (e: IOException) {
                        if (serverSocket != null) {
                            Log.e(TAG, "Error accepting connection: ${e.message}")
                            // Don't break - keep trying
                        } else {
                            Log.d(TAG, "Server socket closed intentionally")
                            break
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Server socket accept failed: ${e.message}")
            }
        }
    }
    
    /**
     * Connect to another player (client mode)
     * Implements robust connection with pairing support
     */
    @Suppress("DEPRECATION")
    fun connectToPlayer(device: BluetoothDevice, context: Context) {
        // Check permissions based on Android version
        if (!hasBluetoothConnectPermission(context)) {
            Log.e(TAG, "Missing Bluetooth permission for client mode")
            CoroutineScope(Dispatchers.Main).launch {
                onConnectionFailed?.invoke("Bluetooth permission not granted")
            }
            return
        }

        closeActiveSocketOnly()
        isConnected = false
        
        isServer = false
        isInitiator = true
        connectedDevice = device
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Discovery must be stopped before RFCOMM connect; also protects kayak GATT
                try {
                    bluetoothAdapter?.cancelDiscovery()
                } catch (e: SecurityException) {
                    Log.w(TAG, "Could not cancel discovery: ${e.message}")
                }
                
                Log.d(TAG, "=== Connection Attempt Started ===")
                Log.d(TAG, "Target device: ${device.name ?: "Unknown Device"} (${device.address})")
                Log.d(TAG, "Bond state: ${getBondStateString(device.bondState)}")
                
                // Step 1: Ensure device is bonded (paired)
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "Device not bonded, initiating pairing...")
                    
                    withContext(Dispatchers.Main) {
                        onConnectionFailed?.invoke("Pairing with ${device.name ?: "Unknown Device"}... Please accept pairing request on both phones.")
                    }
                    
                    val bonded = device.createBond()
                    if (!bonded) {
                        throw IOException("Failed to initiate pairing")
                    }
                    
                    // Wait for pairing to complete (max 30 seconds)
                    var waitTime = 0
                    while (device.bondState == BluetoothDevice.BOND_BONDING && waitTime < 30000) {
                        Thread.sleep(500)
                        waitTime += 500
                    }
                    
                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        throw IOException("Pairing failed or was rejected")
                    }
                    
                    Log.d(TAG, "Pairing successful!")
                    // Give Bluetooth stack time to stabilize after pairing
                    Thread.sleep(1000)
                }
                
                // Step 2: Try connection methods in order
                var connected = false
                var lastError = ""
                
                // Method 1: Insecure RFCOMM (fastest, no encryption)
                if (!connected) {
                    try {
                        Log.d(TAG, "Method 1: Trying insecure RFCOMM...")
                        clientSocket = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
                        clientSocket?.connect()
                        connected = true
                        Log.d(TAG, "✓ Insecure RFCOMM connected!")
                    } catch (e: IOException) {
                        lastError = "Insecure: ${e.message}"
                        Log.w(TAG, "✗ Insecure RFCOMM failed: ${e.message}")
                        try { clientSocket?.close() } catch (e2: Exception) {}
                        clientSocket = null
                    }
                }
                
                // Method 2: Secure RFCOMM (with encryption)
                if (!connected) {
                    try {
                        Log.d(TAG, "Method 2: Trying secure RFCOMM...")
                        clientSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                        clientSocket?.connect()
                        connected = true
                        Log.d(TAG, "✓ Secure RFCOMM connected!")
                    } catch (e: IOException) {
                        lastError = "Secure: ${e.message}"
                        Log.w(TAG, "✗ Secure RFCOMM failed: ${e.message}")
                        try { clientSocket?.close() } catch (e2: Exception) {}
                        clientSocket = null
                    }
                }
                
                // Method 3: Reflection method (channel 1)
                if (!connected) {
                    try {
                        Log.d(TAG, "Method 3: Trying reflection fallback (channel 1)...")
                        val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                        clientSocket = method.invoke(device, 1) as BluetoothSocket
                        clientSocket?.connect()
                        connected = true
                        Log.d(TAG, "✓ Reflection method connected!")
                    } catch (e: Exception) {
                        lastError = "Reflection: ${e.message}"
                        Log.w(TAG, "✗ Reflection method failed: ${e.message}")
                        try { clientSocket?.close() } catch (e2: Exception) {}
                        clientSocket = null
                    }
                }
                
                if (!connected) {
                    throw IOException("All connection methods failed. Last errors: $lastError")
                }
                
                // Step 3: Setup streams; wait for CONN_ACCEPT before notifying UI
                clientSocket?.let { socket ->
                    inputStream = socket.inputStream
                    outputStream = socket.outputStream
                    
                    Log.d(TAG, "=== RFCOMM socket open, sending connection request ===")
                    sendMessage(MSG_CONNECTION_REQUEST)
                    listenForMessages()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "=== Connection Failed ===")
                Log.e(TAG, "Device: ${device.name ?: "Unknown Device"} (${device.address})")
                Log.e(TAG, "Error: ${e.message}")
                e.printStackTrace()
                
                // Cleanup
                try {
                    clientSocket?.close()
                } catch (e2: Exception) {
                    Log.e(TAG, "Error closing socket: ${e2.message}")
                }
                clientSocket = null
                inputStream = null
                outputStream = null
                isConnected = false
                isInitiator = false
                
                // Determine user-friendly error message
                val userMessage = when {
                    e.message?.contains("pairing", ignoreCase = true) == true -> 
                        "Pairing failed. Please check both phones accepted the pairing request."
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Connection timeout. Make sure ${device.name ?: "the device"} is in Player1&Player2 mode and nearby."
                    e.message?.contains("refused", ignoreCase = true) == true ->
                        "${device.name ?: "The device"} refused connection. Make sure it's in Player1&Player2 mode."
                    else ->
                        "Cannot connect to ${device.name ?: "the device"}. Both phones must be in Player1&Player2 mode and Bluetooth must be on."
                }
                
                withContext(Dispatchers.Main) {
                    onConnectionFailed?.invoke(userMessage)
                }
            }
        }
    }
    
    private fun getBondStateString(state: Int): String {
        return when (state) {
            BluetoothDevice.BOND_NONE -> "NOT_BONDED"
            BluetoothDevice.BOND_BONDING -> "BONDING"
            BluetoothDevice.BOND_BONDED -> "BONDED"
            else -> "UNKNOWN($state)"
        }
    }
    
    private fun handleServerConnection(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                clientSocket = socket
                inputStream = socket.inputStream
                outputStream = socket.outputStream
                connectedDevice = socket.remoteDevice
                
                Log.d(TAG, "Server accepted connection from ${connectedDevice?.name ?: "Unknown Device"}")
                
                // Wait for connection request message
                val message = readMessage()
                Log.d(TAG, "Received message from client: $message")
                
                if (message == MSG_CONNECTION_REQUEST) {
                    // Notify UI about connection request
                    withContext(Dispatchers.Main) {
                        onConnectionRequest?.invoke(connectedDevice!!)
                    }
                } else {
                    Log.e(TAG, "Expected CONNECTION_REQUEST but got: $message")
                    rejectConnectionRequest()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error handling server connection: ${e.message}")
                e.printStackTrace()
                closeActiveSocketOnly()
                connectedDevice = null
            }
        }
    }
    
    /**
     * Decline a pending connection request without shutting down the listening server.
     * Allows the initiator to try connecting again.
     */
    fun rejectConnectionRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendMessage(MSG_CONNECTION_REJECT)
            } catch (e: Exception) {
                Log.w(TAG, "Could not send reject message: ${e.message}")
            }
            closeActiveSocketOnly()
            connectedDevice = null
            isConnected = false
            isInitiator = false
        }
    }
    
    /**
     * Accept the connection request (called by Player2 when accepting dialog)
     */
    fun acceptConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendMessage(MSG_CONNECTION_ACCEPT)
                isConnected = true
                
                withContext(Dispatchers.Main) {
                    onConnected?.invoke()
                }
                
                // Start listening for messages
                listenForMessages()
            } catch (e: IOException) {
                Log.e(TAG, "Error accepting connection: ${e.message}")
            }
        }
    }
    
    fun captureWorkoutBaseline() {
        workoutFirstDistance = BleRepository.distance.value.toIntOrNull() ?: 0
        workoutFirstElapsedTime = BleRepository.elapsedTime.value.toIntOrNull() ?: 0
        workoutBaselineCaptured = true
        Log.d(TAG, "Workout baseline captured: distance=$workoutFirstDistance elapsed=$workoutFirstElapsedTime")
    }

    fun hasWorkoutBaseline(): Boolean = workoutBaselineCaptured

    fun clearWorkoutBaseline() {
        workoutBaselineCaptured = false
        workoutFirstDistance = 0
        workoutFirstElapsedTime = 0
    }

    /**
     * Send training start command to the other player.
     * Only the initiating player calls this.
     */
    fun sendTrainingStart(bundle: android.os.Bundle) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = "${MSG_TRAINING_START}|" +
                        "index:${bundle.getInt("index", 2)}|" +
                        "isPlayer2Mode:${bundle.getBoolean("isPlayer2Mode", true)}|" +
                        "player2Device:${bundle.getString("player2Device", "")}|" +
                        "avatarTime:${bundle.getString("avatarTime", "0")}|" +
                        "avatarDistance:${bundle.getString("avatarDistance", "0")}|" +
                        "avatarPace:${bundle.getString("avatarPace", "0")}|" +
                        "trainingStartAt:${bundle.getLong("trainingStartAt", 0L)}"
                sendMessage(data)
                Log.d(TAG, "Sent training start command")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending training start: ${e.message}")
            }
        }
    }
    
    /** Notify opponent that this player reached the training goal */
    fun sendGoalReached() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendMessage(MSG_GOAL_REACHED)
                Log.d(TAG, "Sent goal reached notification")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending goal reached: ${e.message}")
            }
        }
    }

    /**
     * Send training data to other player (real-time sync)
     */
    fun sendTrainingData(data: TrainingData) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val message = "${MSG_TRAINING_DATA}|" +
                        "distance:${data.distance}|" +
                        "time:${data.time}|" +
                        "pace:${data.pace}|" +
                        "strokeRate:${data.strokeRate}|" +
                        "calories:${data.calories}|" +
                        "heartRate:${data.heartRate}|" +
                        "watts:${data.watts}|" +
                        "remainingGoal:${data.remainingGoal}|" +
                        "distanceGap:${data.distanceGap}"
                sendMessage(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending training data: ${e.message}")
            }
        }
    }
    
    private fun listenForMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Keep reading while the socket is open. The initiator must listen before
                // isConnected becomes true so it can receive CONN_ACCEPT after CONN_REQ.
                while (inputStream != null) {
                    val message = readMessage()
                    if (message.isNotEmpty()) {
                        handleMessage(message)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading messages: ${e.message}")
                if (isConnected) {
                    disconnect()
                } else if (isInitiator) {
                    closeActiveSocketOnly()
                    isInitiator = false
                    connectedDevice = null
                    CoroutineScope(Dispatchers.Main).launch {
                        onConnectionFailed?.invoke("Connection declined. You can try again.")
                    }
                }
            }
        }
    }
    
    private fun handleMessage(message: String) {
        Log.d(TAG, "Received message: $message")
        
        when {
            message == MSG_CONNECTION_ACCEPT -> {
                isConnected = true
                CoroutineScope(Dispatchers.Main).launch {
                    onConnected?.invoke()
                }
            }

            message == MSG_CONNECTION_REJECT -> {
                closeActiveSocketOnly()
                isConnected = false
                isInitiator = false
                connectedDevice = null
                CoroutineScope(Dispatchers.Main).launch {
                    onConnectionFailed?.invoke("Connection declined. You can try again.")
                }
            }
            
            message.startsWith(MSG_TRAINING_START) -> {
                // Parse training start data
                val parts = message.split("|")
                val bundle = android.os.Bundle()
                parts.forEach { part ->
                    when {
                        part.startsWith("index:") -> {
                            bundle.putInt("index", part.substringAfter(":").toIntOrNull() ?: 2)
                        }
                        part.startsWith("isPlayer2Mode:") -> {
                            bundle.putBoolean("isPlayer2Mode", part.substringAfter(":").toBoolean())
                        }
                        part.startsWith("player2Device:") -> {
                            bundle.putString("player2Device", part.substringAfter(":"))
                        }
                        part.startsWith("avatarTime:") -> {
                            bundle.putString("avatarTime", part.substringAfter(":"))
                        }
                        part.startsWith("avatarDistance:") -> {
                            bundle.putString("avatarDistance", part.substringAfter(":"))
                        }
                        part.startsWith("avatarPace:") -> {
                            bundle.putString("avatarPace", part.substringAfter(":"))
                        }
                        part.startsWith("trainingStartAt:") -> {
                            bundle.putLong("trainingStartAt", part.substringAfter(":").toLongOrNull() ?: 0L)
                        }
                    }
                }
                
                CoroutineScope(Dispatchers.Main).launch {
                    onTrainingStartReceived?.invoke(bundle)
                }
            }

            message == MSG_GOAL_REACHED -> {
                CoroutineScope(Dispatchers.Main).launch {
                    onGoalReachedReceived?.invoke()
                }
            }
            
            message.startsWith(MSG_TRAINING_DATA) -> {
                // Parse training data
                val parts = message.split("|")
                var distance = "0"
                var time = "0"
                var pace = "0"
                var strokeRate = "0"
                var calories = "0"
                var heartRate = "0"
                var watts = "0"
                var remainingGoal = "0"
                var distanceGap = "0"
                
                parts.forEach { part ->
                    when {
                        part.startsWith("distance:") -> distance = part.substringAfter(":")
                        part.startsWith("time:") -> time = part.substringAfter(":")
                        part.startsWith("pace:") -> pace = part.substringAfter(":")
                        part.startsWith("strokeRate:") -> strokeRate = part.substringAfter(":")
                        part.startsWith("calories:") -> calories = part.substringAfter(":")
                        part.startsWith("heartRate:") -> heartRate = part.substringAfter(":")
                        part.startsWith("watts:") -> watts = part.substringAfter(":")
                        part.startsWith("remainingGoal:") -> remainingGoal = part.substringAfter(":")
                        part.startsWith("distanceGap:") -> distanceGap = part.substringAfter(":")
                    }
                }
                
                val data = TrainingData(
                    distance, time, pace, strokeRate, calories, heartRate, watts, remainingGoal, distanceGap
                )
                CoroutineScope(Dispatchers.Main).launch {
                    onDataReceived?.invoke(data)
                }
            }
            
            message == MSG_DISCONNECT -> {
                disconnect()
            }
        }
    }
    
    private fun sendMessage(message: String) {
        synchronized(sendLock) {
            try {
                val bytes = message.toByteArray(Charsets.UTF_8)
                // Send length first (4 bytes), then message
                val length = bytes.size
                outputStream?.write(
                    byteArrayOf(
                        (length shr 24).toByte(),
                        (length shr 16).toByte(),
                        (length shr 8).toByte(),
                        length.toByte()
                    )
                )
                outputStream?.write(bytes)
                outputStream?.flush()
                Log.d(TAG, "Sent message: $message")
            } catch (e: IOException) {
                Log.e(TAG, "Error sending message: ${e.message}")
                throw e
            }
        }
    }
    
    private fun readMessage(): String {
        try {
            // Read length (4 bytes)
            val lengthBytes = ByteArray(4)
            var bytesRead = 0
            while (bytesRead < 4) {
                val read = inputStream?.read(lengthBytes, bytesRead, 4 - bytesRead) ?: -1
                if (read == -1) throw IOException("Stream closed")
                bytesRead += read
            }
            
            val length = ((lengthBytes[0].toInt() and 0xFF) shl 24) or
                        ((lengthBytes[1].toInt() and 0xFF) shl 16) or
                        ((lengthBytes[2].toInt() and 0xFF) shl 8) or
                        (lengthBytes[3].toInt() and 0xFF)
            
            // Read message
            val messageBytes = ByteArray(length)
            bytesRead = 0
            while (bytesRead < length) {
                val read = inputStream?.read(messageBytes, bytesRead, length - bytesRead) ?: -1
                if (read == -1) throw IOException("Stream closed")
                bytesRead += read
            }
            
            return String(messageBytes, Charsets.UTF_8)
        } catch (e: IOException) {
            Log.e(TAG, "Error reading message: ${e.message}")
            throw e
        }
    }
    
    fun disconnect(notifyDisconnected: Boolean = true, keepServerAlive: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isConnected) {
                    sendMessage(MSG_DISCONNECT)
                }
            } catch (e: Exception) {
                // Ignore
            }
            
            isConnected = false
            isInitiator = false
            
            closeActiveSocketOnly()

            if (!keepServerAlive) {
                try {
                    serverSocket?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Error closing server socket: ${e.message}")
                }
                serverSocket = null
            }
            connectedDevice = null
            
            if (notifyDisconnected) {
                withContext(Dispatchers.Main) {
                    onDisconnected?.invoke()
                }
            }
        }
    }

    /** Tear down an active Player1 & Player2 session after training without UI noise. */
    fun endTrainingSession() {
        clearWorkoutBaseline()
        disconnect(notifyDisconnected = false, keepServerAlive = false)
    }

    /** Ensure the setup screen can accept a new incoming connection attempt. */
    fun ensureServerMode(context: Context) {
        if (!isConnected && serverSocket == null) {
            startServerMode(context)
        }
    }

    private fun closeActiveSocketOnly() {
        try {
            inputStream?.close()
            outputStream?.close()
            clientSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing active socket: ${e.message}")
        }
        inputStream = null
        outputStream = null
        clientSocket = null
    }
    
    /** Clears setup-screen callbacks without tearing down the active BT connection. */
    fun clearSetupCallbacks() {
        onConnectionRequest = null
        onConnected = null
        onDisconnected = null
        onConnectionFailed = null
        onTrainingStartReceived = null
    }

    fun cleanup() {
        disconnect()
        clearSetupCallbacks()
        onDataReceived = null
        onGoalReachedReceived = null
    }
}
