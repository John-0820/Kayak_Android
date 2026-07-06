package com.kayakpro.erg.ui.login.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.BluetoothListAdapter
import com.kayakpro.erg.databinding.ActivityBluetoothScreenBinding
import com.kayakpro.erg.interfaces.OnClickBluetooth
import com.kayakpro.erg.model.BtRow
import com.kayakpro.erg.interfaces.BleManager
import com.kayakpro.erg.bluetooth.KayakBleConnectionManager
import com.kayakpro.erg.viewmodels.MyViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint

import okio.IOException
import okhttp3.OkHttpClient
import okhttp3.FormBody


@AndroidEntryPoint
class BluetoothScreen : AppCompatActivity(), OnClickBluetooth {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BT = 1
    private val btRows: MutableList<BtRow> = mutableListOf()
    private lateinit var bluetoothListAdapter: BluetoothListAdapter
    private var bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanning = false
    val builder = StringBuilder()
    private var pDialog: Dialog? = null
    private val viewModel: MyViewModel by viewModels()
    val sp = SesssionManager.getInstance()!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var cards: List<View>
    private var receiverRegistered = false

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val deviceName =
                result.scanRecord?.deviceName
                    ?: device.name
                    ?: ""
            
            // Log ALL devices found (for debugging)
            if (deviceName.isNotBlank()) {
                Log.d("BluetoothScan", "Found device: $deviceName (${device.address})")
            }
            
            // Skip devices with no name at all
            if (deviceName.isBlank()) {
                return
            }
            
            val nameLower = deviceName.lowercase()

            // CONNECTING DEVICES page: Show Kayak console/machine devices
            // Accept if device name contains any of these keywords
            val isKayakDevice = nameLower.contains("kayak") || 
                                nameLower.contains("gem") || 
                                nameLower.contains("kp") ||
                                nameLower.contains("ergometer") ||
                                nameLower.contains("kayakpro") ||
                                nameLower.contains("erg") ||
                                nameLower.contains("rowing") ||
                                nameLower.contains("paddle") ||
                                nameLower.contains("concept2") ||
                                nameLower.contains("c2") ||
                                nameLower.contains("pm5")
            
            if (!isKayakDevice) {
                Log.d("BluetoothScan", "Skipping non-kayak device: $deviceName")
                return  // Skip non-kayak devices
            }
            
            Log.d("BluetoothScan", "✓ Kayak device found: $deviceName")

            // Avoid duplicates but allow reconnect of connected device
            if (BleManager.btRows.none { it.device.address == device.address }) {
                BleManager.btRows.add(BtRow(device = device, isConnected = device.address == BleManager.connectedDeviceAddress))
                runOnUiThread { bluetoothListAdapter.notifyDataSetChanged() }
            }
        }
    }

    private val TAG = "Wendy"
    private val bluetoothPermissions: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+) requires these runtime permissions
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            // Android 11 and below (API 30 and lower)
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    fun getAccessTokenFromRefreshToken(
        clientId: String,
        clientSecret: String,
        refreshToken: String,
        callback: (String?) -> Unit
    ) {
        val client = OkHttpClient()

        val form = FormBody.Builder()
            .add("client_id", clientId)
            .add("client_secret", clientSecret)
            .add("refresh_token", refreshToken)
            .add("grant_type", "refresh_token")
            .build()

        val request = okhttp3.Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(form)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string() ?: ""
                val accessToken = Regex("\"access_token\"\\s*:\\s*\"(.*?)\"")
                    .find(body)?.groupValues?.get(1)
                callback(accessToken)
            }
        })
    }
    private lateinit var binding: ActivityBluetoothScreenBinding

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        KayakBleConnectionManager.initialize(this)
        setupConnectionCallbacks()

        // Use global BLE list
        bluetoothListAdapter = BluetoothListAdapter(BleManager.btRows, this)
        binding.rvBluetooth.adapter = bluetoothListAdapter


        val cardKayak = findViewById<LinearLayout>(R.id.card_kayak)
        val cardCanoe = findViewById<LinearLayout>(R.id.card_canoe)
        val cardDragonboat = findViewById<LinearLayout>(R.id.card_dragonboat)
        val cardOutrigger = findViewById<LinearLayout>(R.id.card_outrigger)
        val cardSwimfast = findViewById<LinearLayout>(R.id.card_swimfast)
        val cardSkifast = findViewById<LinearLayout>(R.id.card_skifast)
        val cardSup = findViewById<LinearLayout>(R.id.card_sup)

        cards = listOf(cardKayak, cardCanoe, cardDragonboat, cardOutrigger, cardSwimfast, cardSkifast, cardSup)
        val device_names = listOf("Kayak", "Canoe", "Dragon Boat", "OutRigger", "Swimfast", "Skifast", "Sup")
        cardKayak.isSelected = true
        sp!!.setSelectedMachine("Kayak")
        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectCard(card)
//                sp!!.setSelectedMachine(card.toString())
                sp.setSelectedMachine(device_names[index])
            }
        }

        if (!KayakBleConnectionManager.isConnected()) {
            sp.setDeviceConnected(false)
        }
        pDialog = Dialog(this)
        pDialog?.setContentView(R.layout.progress_bar_layout)
        pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pDialog!!.setCancelable(true)
        sp.setDeviceTimeConnected(false)

        if (!receiverRegistered) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            receiverRegistered = true
        }

        binding.ivRefresh.setOnClickListener {
            disconnectAndRefresh()
        }

        binding.ivNext.setOnClickListener {
            if (BleManager.selectedDevice != null && KayakBleConnectionManager.isConnected()) {
                moveToNext()
            } else {
                showDialog(resources.getString(R.string.unpaired), false)
            }
        }

        // Check and request Bluetooth permissions FIRST
        if (!isBluetoothPermissionsGranted()) {
            Log.d("BluetoothScreen", "Permissions not granted, requesting...")
            checkBluetoothPermissions()
        } else if (checkBluetoothSupport()) {
            Log.d("BluetoothScreen", "Permissions granted, starting scan...")
            scanForDevices()
        }

    }

    override fun onResume() {
        super.onResume()
        // Do not scan while connected or connecting — scanning interferes with GATT on many devices
        if (KayakBleConnectionManager.isConnected() ||
            KayakBleConnectionManager.connectionState == KayakBleConnectionManager.ConnectionState.CONNECTING ||
            KayakBleConnectionManager.connectionState == KayakBleConnectionManager.ConnectionState.RECONNECTING
        ) {
            bluetoothListAdapter.setConnectedDevice(BleManager.connectedDeviceAddress)
            return
        }
        if (checkBluetoothSupport() && isBluetoothPermissionsGranted() && !scanning) {
            scanForDevices()
        }
    }

    private fun setupConnectionCallbacks() {
        KayakBleConnectionManager.onBeforeConnect = {
            stopScanning()
        }
        KayakBleConnectionManager.onConnectionStateChanged = { state, address ->
            when (state) {
                KayakBleConnectionManager.ConnectionState.CONNECTED -> {
                    bluetoothListAdapter.setConnectedDevice(address)
                    sp!!.setDeviceConnected(true)
                    viewModel.updateDeviceStatus(true)
                }
                KayakBleConnectionManager.ConnectionState.DISCONNECTED -> {
                    bluetoothListAdapter.setConnectedDevice(null)
                    sp!!.setDeviceConnected(false)
                    viewModel.updateDeviceStatus(false)
                }
                KayakBleConnectionManager.ConnectionState.CONNECTING,
                KayakBleConnectionManager.ConnectionState.RECONNECTING -> {
                    bluetoothListAdapter.setConnectedDevice(null)
                }
            }
        }
        KayakBleConnectionManager.onFirstConnected = {
            showDialog(resources.getString(R.string.paired), false)
        }
        KayakBleConnectionManager.onConnectionFailed = { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            bluetoothListAdapter.setConnectedDevice(null)
            sp.setDeviceConnected(false)
            viewModel.updateDeviceStatus(false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScanning() {
        if (scanning) {
            bluetoothLeScanner?.stopScan(leScanCallback)
            scanning = false
        }
    }


    private fun moveToNext() {
        val bundle = Bundle()
        bundle.putParcelable("BluetoothDeviceModel", BleManager.selectedDevice)
        startActivity(Intent(this, HomeScreen::class.java).putExtras(bundle))
    }


    private fun initl() {

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            // Bluetooth is not supported or not enabled, handle accordingly
            showDialog(resources.getString(R.string.enable_bluetooth), true)
            // //Log.d("Analysis__","Bluetooth not enabled")
            return
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

        }
    }



    private fun startBluetoothScan() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            scanForDevices()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_SCAN), 1)
        }
        // scanForDevices()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                    // Permissions granted, proceed with Bluetooth functionality
                    scanForDevices()
                    initl()

                } else {
                    //initl()
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
                }
                Log.d("Analysis__", "gatt returning line no. 253")

                return
            }
            // Handle other permissions if needed
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun clickItem(id: Int, device: BluetoothDevice) {
        Log.d(TAG, "clickItem ********************************************")
        if (KayakBleConnectionManager.isConnected() &&
            BleManager.connectedDeviceAddress == device.address
        ) {
            showDialog(resources.getString(R.string.already_connected), true)
            return
        }

        KayakBleConnectionManager.connect(device)
    }

    private fun showDialog(message: String, showOkay: Boolean) {
        try {
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .create()
            val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            val btnOkay = view.findViewById<Button>(R.id.btn_okay)
            val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
            if (showOkay) {
                btnOkay.visibility = View.VISIBLE
            } else {
                btnOkay.visibility = View.GONE
            }
            builder.setView(view)
            tvMessage.setText(message)
            close.setOnClickListener {
                builder.dismiss()
            }
            btnOkay.setOnClickListener { builder.dismiss() }
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e("Analysis__", "${e.printStackTrace()}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanForDevices() {
        Log.d("BluetoothScreen", "=== scanForDevices() called ===")
        
        if (bluetoothAdapter?.isEnabled != true) {
            Log.e("BluetoothScreen", "Bluetooth is not enabled!")
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isBluetoothPermissionsGranted()) {
            Log.e("BluetoothScreen", "Permissions not granted!")
            checkBluetoothPermissions()
            return
        }

        Log.d("BluetoothScreen", "Bluetooth enabled: ${bluetoothAdapter?.isEnabled}")
        Log.d("BluetoothScreen", "Permissions granted: YES")
        
        // First, show already paired/bonded kayak devices
        val bondedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
        Log.d("BluetoothScreen", "Found ${bondedDevices.size} bonded devices")
        
        var addedBonded = 0
        for (device in bondedDevices) {
            val deviceName = device.name ?: ""
            if (deviceName.isNotBlank()) {
                val nameLower = deviceName.lowercase()
                // Check if it's a kayak device
                val isKayakDevice = nameLower.contains("kayak") || 
                                    nameLower.contains("gem") || 
                                    nameLower.contains("kp") ||
                                    nameLower.contains("ergometer") ||
                                    nameLower.contains("kayakpro") ||
                                    nameLower.contains("erg") ||
                                    nameLower.contains("rowing") ||
                                    nameLower.contains("paddle") ||
                                    nameLower.contains("concept2") ||
                                    nameLower.contains("c2") ||
                                    nameLower.contains("pm5")
                
                if (isKayakDevice) {
                    Log.d("BluetoothScreen", "✓ Bonded kayak device: $deviceName")
                    if (BleManager.btRows.none { it.device.address == device.address }) {
                        BleManager.btRows.add(
                            BtRow(
                                device = device,
                                isConnected = device.address == BleManager.connectedDeviceAddress
                            )
                        )
                        addedBonded++
                    }
                } else {
                    Log.d("BluetoothScreen", "Skipping bonded non-kayak: $deviceName")
                }
            }
        }
        
        Log.d("BluetoothScreen", "Added $addedBonded bonded kayak devices")
        runOnUiThread { 
            bluetoothListAdapter.notifyDataSetChanged()
            if (addedBonded > 0) {
                Toast.makeText(this, "Found $addedBonded paired kayak device(s)", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Then start scanning for new devices
        if (scanning) {
            Log.d("BluetoothScreen", "Already scanning, stopping previous scan...")
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
        
        scanning = true
        Log.d("BluetoothScreen", "Starting BLE scan (10 seconds)...")
        bluetoothLeScanner?.startScan(leScanCallback)

        // Stop scan after 10 seconds (longer for kayak devices)
        handler.postDelayed({
            Log.d("BluetoothScreen", "Scan complete. Total devices in list: ${BleManager.btRows.size}")
            bluetoothLeScanner?.stopScan(leScanCallback)
            scanning = false
            runOnUiThread {
                if (BleManager.btRows.isEmpty()) {
                    Toast.makeText(this, "No kayak devices found. Make sure kayak is powered on and nearby.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Found ${BleManager.btRows.size} kayak device(s)", Toast.LENGTH_SHORT).show()
                }
            }
        }, 10000)
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun fetchPairedDevices() {
        val bondedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
        for (device in bondedDevices) {
            if (btRows.none { it.device.address == device.address }) {
                btRows.add(
                    BtRow(
                        device = device,
                        isConnected = device.address == BleManager.connectedDeviceAddress
                    )
                )
            }
        }
        if (::bluetoothListAdapter.isInitialized) {
            runOnUiThread { bluetoothListAdapter.notifyDataSetChanged() }
        }
    }


    // Create a BroadcastReceiver for ACTION_FOUND
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Log.d("Analysis__","Device found")
            val action: String = intent.action!!
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
//                    val deviceName = device.name
//                    val deviceHardwareAddress = device.address // MAC address
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroy() {
        KayakBleConnectionManager.onConnectionStateChanged = null
        KayakBleConnectionManager.onConnectionFailed = null
        KayakBleConnectionManager.onFirstConnected = null
        KayakBleConnectionManager.onBeforeConnect = null
        stopScanning()
        if (receiverRegistered) {
            try {
                unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Receiver already unregistered", e)
            }
            receiverRegistered = false
        }
        super.onDestroy()
    }

    private fun selectCard(selected: View) {
        cards.forEach { card -> card.isSelected = (card == selected) }
    }

    private fun checkBluetoothSupport(): Boolean {
        if (bluetoothAdapter == null) {
            showDialog(resources.getString(R.string.bluetooth_not_supported), true)
            return false
        }
        if (!bluetoothAdapter!!.isEnabled) {
            showDialog(resources.getString(R.string.check_bluetooth_setting), true)
            return false
        }
        return true
    }

    private fun checkBluetoothPermissions() {
        if (!isBluetoothPermissionsGranted()) {
            requestPermissions(bluetoothPermissions, REQUEST_ENABLE_BT)
        } else {
            startBluetoothScan()
        }
    }
    private fun isBluetoothPermissionsGranted(): Boolean {
        return bluetoothPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnectAndRefresh() {
        Log.d("BluetoothScreen", "=== Refresh Button Clicked ===")
        
        if (!isBluetoothPermissionsGranted()) {
            Log.e("BluetoothScreen", "Permissions not granted!")
            Toast.makeText(this, "Bluetooth permissions required", Toast.LENGTH_SHORT).show()
            checkBluetoothPermissions()
            return
        }
        
        stopScanning()
        KayakBleConnectionManager.disconnect()
        BleManager.isFirstConnection = true

        Log.d("BluetoothScreen", "Clearing device list (had ${BleManager.btRows.size} devices)")
        BleManager.btRows.clear()
        bluetoothListAdapter.notifyDataSetChanged()

        Log.d("BluetoothScreen", "Starting scan...")
        scanForDevices()
        Toast.makeText(this, "Scanning for kayak devices...", Toast.LENGTH_SHORT).show()
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    private fun disconnectFromDevice() {
        stopScanning()
        KayakBleConnectionManager.disconnect()
        sp.setDeviceConnected(false)
        viewModel.updateDeviceStatus(false)
        Toast.makeText(this, "Disconnected from device", Toast.LENGTH_SHORT).show()

        btRows.clear()
        bluetoothListAdapter.notifyDataSetChanged()
        BleRepository.reset()
    }

}