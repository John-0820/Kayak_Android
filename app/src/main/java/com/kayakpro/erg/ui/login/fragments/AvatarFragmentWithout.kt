package com.kayakpro.erg.ui.login.fragments

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.DistanceAdapter
import com.kayakpro.erg.adapters.HomeProgramAdapter
import com.kayakpro.erg.adapters.PaceTimeAdapter
import com.kayakpro.erg.adapters.PhoneBluetoothAdapter
import com.kayakpro.erg.bluetooth.Player2ConnectionManager
import com.kayakpro.erg.databinding.FragmentAvatraWithoutBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.interfaces.BleManager
import com.kayakpro.erg.interfaces.OnClickBluetooth
import com.kayakpro.erg.interfaces.OnClickSpinner
import com.kayakpro.erg.model.BtRow
import com.kayakpro.erg.model.ProgramModel
import com.kayakpro.erg.model.requestmodel.CreateAvatarRequest
import com.kayakpro.erg.viewmodels.AvatarTrainingViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [AvatarFragmentWithout.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AvatarFragmentWithout : Fragment(), OnClickSpinner, OnClickBluetooth {
    // TODO: Rename and change types of parameters

    var adapter: HomeProgramAdapter?=null
    var distanceClicked=false
    var time="0"
    var pace="0"
    var distance="0"
    private val viewModel: AvatarTrainingViewModel by activityViewModels()
    private lateinit var appBarCallback: AppBarCallback
    lateinit var pacetimeAdapter: PaceTimeAdapter
    private lateinit var binding: FragmentAvatraWithoutBinding
    private var alProgram=ArrayList<ProgramModel>()
    val sp = SesssionManager.getInstance()!!
    
    // Player mode variables
    private var isPlayerBotMode = true // true = Player & Bot, false = Player1 & Player2
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner? = null
    private val phoneDevices: MutableList<BtRow> = mutableListOf()
    private lateinit var phoneBluetoothAdapter: PhoneBluetoothAdapter
    private var selectedPlayer2Device: BluetoothDevice? = null
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private var scanStopRunnable: Runnable? = null


    private val TAG = "Wendy"

    private val discoverableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (!isAdded) return@registerForActivityResult
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d("AvatarFragment", "User declined discoverable mode - continuing with scan")
            Toast.makeText(
                requireContext(),
                "Visibility declined. You can still scan and connect to paired phones.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Device is now discoverable for ${result.resultCode} seconds",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("AvatarFragment", "Device made discoverable for ${result.resultCode} seconds")
        }
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (!isAdded) return@registerForActivityResult
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d("AvatarFragment", "All Bluetooth permissions granted")
            if (!isPlayerBotMode) {
                continuePlayer2Setup()
            }
        } else {
            Log.d("AvatarFragment", "Some Bluetooth permissions denied")
            Toast.makeText(
                requireContext(),
                "Bluetooth permissions are required for Player1 & Player2 mode",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AvatraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        @Suppress("unused")
        fun newInstance(param1: String, param2: String) =
            AvatarFragmentWithout().apply {
                arguments = Bundle().apply {

                }
            }
    }
    
    private val bluetoothPermissions: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    
    @Suppress("DEPRECATION")
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val ctx = context ?: return
            if (!hasBluetoothConnectPermission(ctx)) {
                return
            }
            val device = result.device ?: return
            addPhoneDevice(device, result.scanRecord?.deviceName ?: device.name)
        }
    }
    
    private fun hasBluetoothConnectPermission(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun getConnectedKayakAddress(): String? {
        return BleManager.connectedDeviceAddress ?: BleManager.bluetoothGatt?.device?.address
    }
    
    private fun isKayakConsoleDevice(device: BluetoothDevice, deviceName: String): Boolean {
        if (device.address == getConnectedKayakAddress()) {
            return true
        }
        val nameLower = deviceName.lowercase()
        return nameLower.contains("kayak") ||
            nameLower.contains("gem") ||
            nameLower.contains("kp") ||
            nameLower.contains("ergometer") ||
            nameLower.contains("kayakpro")
    }
    
    @Suppress("DEPRECATION")
    private fun addPhoneDevice(device: BluetoothDevice, rawName: String?) {
        if (!isAdded) return
        
        var deviceName = rawName
        if (deviceName.isNullOrBlank()) {
            val ctx = context ?: return
            if (hasBluetoothConnectPermission(ctx)) {
                try {
                    deviceName = device.name
                } catch (e: SecurityException) {
                    Log.w("AvatarFragment", "Cannot read device name: ${e.message}")
                }
            }
        }
        if (deviceName.isNullOrBlank() || deviceName == "Unknown") {
            return
        }
        
        if (isKayakConsoleDevice(device, deviceName)) {
            return
        }
        
        if (device.address == bluetoothAdapter?.address) {
            return
        }
        
        if (phoneDevices.none { it.device.address == device.address }) {
            phoneDevices.add(BtRow(device = device, isConnected = false))
            activity?.runOnUiThread {
                if (!isAdded || !::phoneBluetoothAdapter.isInitialized) return@runOnUiThread
                phoneBluetoothAdapter.notifyDataSetChanged()
                if (phoneDevices.isNotEmpty() && ::binding.isInitialized) {
                    binding.tvBluetoothTitle.text = "Found ${phoneDevices.size} device(s)"
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showTimePickerDialog(isTime:Boolean=false) {
        // Create a dialog
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.time_picker)

        val minutesPicker: NumberPicker = dialog.findViewById(R.id.minutesPicker)
        val secondsPicker: NumberPicker = dialog.findViewById(R.id.secondsPicker)
        val confirmButton: Button = dialog.findViewById(R.id.confirmButton)
        minutesPicker.textColor = Color.WHITE
        secondsPicker.textColor = Color.WHITE

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59

        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59

        confirmButton.setOnClickListener {
            val minutes = minutesPicker.value
            val seconds = secondsPicker.value
            if (isTime){
                binding.tvTimeValue.text = "${minutes}m ${seconds}s"
                binding.tvDistanceValue.setText("Select")
            }else {
                binding.tvPaceValue.text = "${minutes}m ${seconds}s"
            }
            dialog.dismiss() // Close the dialog
        }

        dialog.show() // Show the dialog
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!this::binding.isInitialized) {

            binding = FragmentAvatraWithoutBinding.inflate(inflater)
            if (arguments != null) {

            }
            init()
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun init(){
        updateAppBarTitle()

        // Initialize Bluetooth
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        
        // Initialize Player2 Connection Manager
        Player2ConnectionManager.initialize(bluetoothAdapter)
        setupConnectionCallbacks()
        
        // Initialize phone Bluetooth adapter
        phoneBluetoothAdapter = PhoneBluetoothAdapter(phoneDevices, this)
        binding.rvBluetoothDevices.adapter = phoneBluetoothAdapter

        adapter= HomeProgramAdapter(requireContext(), alProgram)
        binding.rvProgramHistory.adapter=adapter
        _setDistanceData()
        setTimeData()
        setPaceData()
        setupTabButtons()
        updateTabButtonStyles(playerBotSelected = true)
        setupBluetoothRefresh()
        
        binding.btnStart.setOnClickListener {
            if (isPlayerBotMode) {
                // Original Player & Bot mode logic
                startPlayerBotTraining()
            } else {
                // Player1 & Player2 mode logic - check connection first
                if (!Player2ConnectionManager.isConnected) {
                    Toast.makeText(requireContext(), "Please connect to Player2 first", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                startPlayer1Player2Training()
            }
        }
        
        // Initially disable START button in Player2 mode until connected
        updateStartButtonForPlayer2Mode()
    }
    
    private fun setupConnectionCallbacks() {
        // Handle incoming connection requests (Player2 receives this)
        Player2ConnectionManager.onConnectionRequest = { device ->
            activity?.runOnUiThread {
                val deviceName = device.name ?: "Unknown Device"
                Log.d("AvatarFragment", "Received connection request from $deviceName")
                showConnectionRequestDialog(device)
            }
        }
        
        // Handle successful connection (only after receiver confirms)
        Player2ConnectionManager.onConnected = {
            activity?.runOnUiThread {
                val connectedDeviceName = Player2ConnectionManager.connectedDevice?.name ?: "Unknown Device"
                Toast.makeText(requireContext(), "Connected to $connectedDeviceName", Toast.LENGTH_LONG).show()
                selectedPlayer2Device = Player2ConnectionManager.connectedDevice
                phoneBluetoothAdapter.setConnectedDevice(selectedPlayer2Device?.address)
                updateStartButtonForPlayer2Mode()
                Log.d("AvatarFragment", "Connection established successfully")
            }
        }
        
        // Handle connection failure (don't confuse with disconnection)
        Player2ConnectionManager.onConnectionFailed = { error ->
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Connection failed: $error", Toast.LENGTH_LONG).show()
                selectedPlayer2Device = null
                phoneBluetoothAdapter.setConnectedDevice(null)
                updateStartButtonForPlayer2Mode()

                Log.e("AvatarFragment", "Connection failed: $error")
            }
        }
        
        // Handle disconnection (after successful connection)
        Player2ConnectionManager.onDisconnected = {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Player2 disconnected", Toast.LENGTH_SHORT).show()
                selectedPlayer2Device = null
                phoneBluetoothAdapter.setConnectedDevice(null)
                updateStartButtonForPlayer2Mode()
                Log.d("AvatarFragment", "Connection lost")
            }
        }
        
        // Handle training start command from initiator (receiver navigates with shared goal + sync time)
        Player2ConnectionManager.onTrainingStartReceived = { bundle ->
            activity?.runOnUiThread {
                Log.d("AvatarFragment", "Received training start command, navigating to training...")
                applyRemoteTrainingSession(bundle)
            }
        }
    }

    private fun applyRemoteTrainingSession(bundle: Bundle) {
        val avatarTime = bundle.getString("avatarTime") ?: "0"
        val avatarDistance = bundle.getString("avatarDistance") ?: "0"
        val avatarPace = bundle.getString("avatarPace") ?: "0"

        sp.setAvatarTime(avatarTime)
        sp.setAvatarDistacne(avatarDistance)
        sp.setAvatarPace(avatarPace)

        val isTimeOrDistance = avatarTime != "0" && avatarTime != "Select"
        val amount = if (isTimeOrDistance) avatarTime else avatarDistance
        val createAvatarRequest = CreateAvatarRequest(
            isTimeOrDistance,
            amount,
            avatarPace,
            sp.getSelectedMachine()!!,
        )
        viewModel.createProgResponse.value = null
        viewModel.createAvatarTraining(createAvatarRequest, "Bearer " + sp.getAToken()!!)

        var navigated = false
        viewModel.createProgResponse.observe(viewLifecycleOwner) { response ->
            if (navigated || response == null) return@observe
            navigated = true
            response.body?.id?.let { bundle.putString("avatarTrainingId", it) }
            Log.d("AvatarFragment", "PvP receiver createAvatarTraining response: id=${response.body?.id}, navigating with avatarTrainingId=${bundle.getString("avatarTrainingId")}")
            findNavController().navigate(R.id.nav_loading, bundle)
        }
        handler.postDelayed({
            if (!navigated && isAdded) {
                navigated = true
                Log.d("AvatarFragment", "PvP receiver createAvatarTraining timeout, navigating without training id")
                findNavController().navigate(R.id.nav_loading, bundle)
            }
        }, 5000)
    }

    /** Only the player who sent the connection request may set the goal and start training. */
    private fun updateStartButtonForPlayer2Mode() {
        if (isPlayerBotMode) {
            binding.llQuickStart.visibility = View.VISIBLE
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStart.isEnabled = true
            binding.btnStart.alpha = 1.0f
            return
        }

        val canConfigureTraining = !Player2ConnectionManager.isConnected ||
            Player2ConnectionManager.isInitiator
        binding.llQuickStart.visibility = if (canConfigureTraining) View.VISIBLE else View.GONE
        if (!canConfigureTraining) {
            binding.rvDistance.visibility = View.GONE
            distanceClicked = false
        }

        if (!Player2ConnectionManager.isConnected) {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStart.isEnabled = false
            binding.btnStart.alpha = 0.5f
        } else if (Player2ConnectionManager.isInitiator) {
            binding.btnStart.visibility = View.VISIBLE
            binding.btnStart.isEnabled = true
            binding.btnStart.alpha = 1.0f
        } else {
            binding.btnStart.visibility = View.GONE
        }
    }
    
    private fun showConnectionRequestDialog(device: BluetoothDevice) {
        val ctx = context ?: return
        if (!hasBluetoothConnectPermission(ctx)) {
            return
        }
        
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_confirm_player_connection)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val tvPlayerName = dialog.findViewById<TextView>(R.id.tv_player_name)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnStartTraining = dialog.findViewById<Button>(R.id.btn_start_training)
        val ivClose = dialog.findViewById<ImageView>(R.id.iv_close_dialog)
        
        tvPlayerName.text = device.name ?: "Unknown Device"
        val deviceNameForMessage = device.name ?: "Unknown Device"
        tvMessage?.text = "$deviceNameForMessage wants to connect for training"
        btnStartTraining.text = "Accept"
        
        ivClose.setOnClickListener {
            Player2ConnectionManager.rejectConnectionRequest()
            dialog.dismiss()
        }
        
        btnCancel.setOnClickListener {
            Player2ConnectionManager.rejectConnectionRequest()
            dialog.dismiss()
        }
        
        btnStartTraining.setOnClickListener {
            Player2ConnectionManager.acceptConnection()
            dialog.dismiss()
            Toast.makeText(requireContext(), "Connected! Waiting for Player1 to start training...", Toast.LENGTH_LONG).show()
        }
        
        dialog.setCancelable(false)
        dialog.show()
    }
    
    private fun setupTabButtons() {
        binding.btnPlayerBot.setOnClickListener {
            if (!isPlayerBotMode) {
                switchToPlayerBotMode()
            }
        }
        
        binding.btnPlayerPlayer.setOnClickListener {
            if (isPlayerBotMode) {
                switchToPlayer1Player2Mode()
            }
        }
    }

    private fun updateTabButtonStyles(playerBotSelected: Boolean) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.avatar_tab_selected)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.avatar_tab_unselected)

        applyTabButtonStyle(binding.btnPlayerBot, selected = playerBotSelected, selectedColor, unselectedColor)
        applyTabButtonStyle(binding.btnPlayerPlayer, selected = !playerBotSelected, selectedColor, unselectedColor)
    }

    private fun applyTabButtonStyle(
        button: MaterialButton,
        selected: Boolean,
        selectedColor: Int,
        unselectedColor: Int
    ) {
        button.backgroundTintList = ColorStateList.valueOf(
            if (selected) selectedColor else unselectedColor
        )
        button.setTextColor(
            ContextCompat.getColor(requireContext(), if (selected) R.color.black else R.color.white)
        )
        button.isSelected = selected
    }
    
    private fun switchToPlayerBotMode() {
        isPlayerBotMode = true
        updateTabButtonStyles(playerBotSelected = true)
        
        // Show Pace selector, hide Bluetooth list
        binding.rlPace.visibility = View.VISIBLE
        binding.rlBluetoothList.visibility = View.GONE
        
        // Update Start button position to be below rl_pace
        val layoutParams = binding.btnStart.layoutParams as android.widget.RelativeLayout.LayoutParams
        layoutParams.removeRule(android.widget.RelativeLayout.BELOW)
        layoutParams.addRule(android.widget.RelativeLayout.BELOW, binding.rlPace.id)
        binding.btnStart.layoutParams = layoutParams
        
        // Stop scanning if active
        stopPhoneScanning()
        selectedPlayer2Device = null
        updateStartButtonForPlayer2Mode()
    }
    
    @Suppress("DEPRECATION")
    private fun switchToPlayer1Player2Mode() {
        isPlayerBotMode = false
        updateTabButtonStyles(playerBotSelected = false)
        
        // Hide Pace selector, show Bluetooth list
        binding.rlPace.visibility = View.GONE
        binding.rlBluetoothList.visibility = View.VISIBLE
        
        // Update Start button position to be below rl_bluetooth_list
        val layoutParams = binding.btnStart.layoutParams as android.widget.RelativeLayout.LayoutParams
        layoutParams.removeRule(android.widget.RelativeLayout.BELOW)
        layoutParams.addRule(android.widget.RelativeLayout.BELOW, binding.rlBluetoothList.id)
        binding.btnStart.layoutParams = layoutParams
        
        updateStartButtonForPlayer2Mode()
        
        // FIRST: Check and request Bluetooth permissions
        if (!checkBluetoothPermissions()) {
            Log.d("AvatarFragment", "Waiting for Bluetooth permissions...")
            return
        }
        
        continuePlayer2Setup()
    }
    
    private fun continuePlayer2Setup() {
        // Start server mode - wait for incoming connections (does not affect kayak BLE GATT)
        Player2ConnectionManager.startServerMode(requireContext())
        
        // Optional: make device visible to other phones (user can decline)
        makePhoneDiscoverable()
        
        // BLE-only scan: classic startDiscovery() disconnects the kayak console GATT connection
        scanForPhones()
    }
    
    private fun setupBluetoothRefresh() {
        // Tap to Scan circle
        binding.rlScanCircle.setOnClickListener {
            phoneDevices.clear()
            phoneBluetoothAdapter.notifyDataSetChanged()
            selectedPlayer2Device = null
            
            if (!isPlayerBotMode && checkBluetoothPermissions()) {
                scanForPhones()
                binding.tvBluetoothTitle.text = "Searching for Player2's Bluetooth..."
                Toast.makeText(requireContext(), "Scanning for phones...", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Refresh button
        binding.ivBluetoothRefresh.setOnClickListener {
            phoneDevices.clear()
            phoneBluetoothAdapter.notifyDataSetChanged()
            selectedPlayer2Device = null
            
            if (!isPlayerBotMode && checkBluetoothPermissions()) {
                scanForPhones()
                binding.tvBluetoothTitle.text = "Searching for Player2's Bluetooth..."
                Toast.makeText(requireContext(), "Scanning for phones...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun checkBluetoothPermissions(): Boolean {
        val allGranted = bluetoothPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (!allGranted) {
            Log.d("AvatarFragment", "Missing Bluetooth permissions, requesting...")
            bluetoothPermissionLauncher.launch(bluetoothPermissions)
            return false
        }
        return true
    }
    
    @Suppress("DEPRECATION")
    private fun scanForPhones() {
        if (!isAdded) return
        
        if (bluetoothAdapter?.isEnabled != true) {
            Toast.makeText(requireContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        
        val ctx = requireContext()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                checkBluetoothPermissions()
                return
            }
        }
        
        // Load paired phones first (no radio activity, safe while kayak GATT is connected)
        if (hasBluetoothConnectPermission(ctx)) {
            try {
                bluetoothAdapter?.bondedDevices?.forEach { device ->
                    addPhoneDevice(device, device.name)
                }
                if (phoneDevices.isNotEmpty() && ::phoneBluetoothAdapter.isInitialized) {
                    phoneBluetoothAdapter.notifyDataSetChanged()
                    binding.tvBluetoothTitle.text = "Found ${phoneDevices.size} paired device(s)"
                }
            } catch (e: SecurityException) {
                Log.e("AvatarFragment", "Cannot read paired devices: ${e.message}")
            } catch (e: Exception) {
                Log.e("AvatarFragment", "Error loading paired devices: ${e.message}")
            }
        }
        
        stopPhoneScanning()
        scanning = true
        
        // LOW_POWER BLE scan keeps the kayak console GATT connection alive
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        
        try {
            bluetoothLeScanner?.startScan(null, scanSettings, leScanCallback)
        } catch (e: SecurityException) {
            Log.e("AvatarFragment", "BLE scan permission denied: ${e.message}")
            scanning = false
            Toast.makeText(ctx, "Bluetooth scan permission required", Toast.LENGTH_SHORT).show()
            return
        } catch (e: Exception) {
            Log.e("AvatarFragment", "BLE scan failed: ${e.message}")
            scanning = false
            return
        }
        
        scanStopRunnable?.let { handler.removeCallbacks(it) }
        scanStopRunnable = Runnable {
            if (!isAdded) return@Runnable
            stopPhoneScanning()
            if (phoneDevices.isEmpty() && ::binding.isInitialized) {
                binding.tvBluetoothTitle.text =
                    "No phones found. Pair both phones in Settings → Bluetooth, then tap refresh."
            }
        }
        handler.postDelayed(scanStopRunnable!!, 12000)
    }
    
    @Suppress("DEPRECATION")
    private fun stopPhoneScanning() {
        scanStopRunnable?.let { handler.removeCallbacks(it) }
        scanStopRunnable = null
        
        if (!scanning) return
        
        val ctx = context ?: return
        val hasScanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        if (hasScanPermission) {
            try {
                bluetoothLeScanner?.stopScan(leScanCallback)
            } catch (e: Exception) {
                Log.w("AvatarFragment", "Error stopping BLE scan: ${e.message}")
            }
        }
        scanning = false
    }
    
    
    @Suppress("DEPRECATION")
    private fun makePhoneDiscoverable() {
        Log.d("AvatarFragment", "makePhoneDiscoverable called")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("AvatarFragment", "BLUETOOTH_ADVERTISE not granted, skipping discoverable prompt")
                return
            }
        }
        
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            discoverableLauncher.launch(discoverableIntent)
        } catch (e: Exception) {
            Log.e("AvatarFragment", "Error making device discoverable: ${e.message}")
        }
    }
    
    @Suppress("DEPRECATION", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun clickItem(id: Int, device: BluetoothDevice) {
        val ctx = context ?: return
        if (!hasBluetoothConnectPermission(ctx)) {
            Toast.makeText(ctx, "Bluetooth permission required", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Check if already connected
        if (Player2ConnectionManager.isConnected && Player2ConnectionManager.connectedDevice?.address == device.address) {
            val deviceName = device.name ?: "Unknown Device"
            Toast.makeText(requireContext(), "Already connected to $deviceName", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Stop scanning before RFCOMM connect (required on some devices)
        stopPhoneScanning()
        
        // Initiate connection to the selected device
        selectedPlayer2Device = device
        val deviceName = device.name ?: "Unknown Device"
        Toast.makeText(requireContext(), "Connecting to $deviceName...", Toast.LENGTH_SHORT).show()
        
        Log.d("AvatarFragment", "Initiating connection to $deviceName (${device.address})")
        Player2ConnectionManager.connectToPlayer(device, requireContext())
    }
    
    override fun onDestroyView() {
        stopPhoneScanning()
        // Clear setup callbacks when leaving setup screen; keep BT connection for training
        Player2ConnectionManager.clearSetupCallbacks()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        if (!::binding.isInitialized) return
        setupConnectionCallbacks()
        selectedPlayer2Device = if (Player2ConnectionManager.isConnected) {
            Player2ConnectionManager.connectedDevice
        } else {
            null
        }
        phoneBluetoothAdapter.setConnectedDevice(selectedPlayer2Device?.address)
        if (!isPlayerBotMode) {
            Player2ConnectionManager.ensureServerMode(requireContext())
        }
        updateStartButtonForPlayer2Mode()
    }
    
    override fun onDestroy() {
        stopPhoneScanning()
        super.onDestroy()
    }
    
    private fun isZeroOrUnsetTime(raw: String): Boolean {
        val time = raw.trim()
        if (time.isBlank() || time == "0" || time.equals("Select", ignoreCase = true)) {
            return true
        }
        return time.replace(" ", "").equals("0m0s", ignoreCase = true)
    }

    private fun startPlayerBotTraining() {
            distance=binding.tvDistanceValue.text.toString()
            time=binding.tvTimeValue.text.toString()
            val paceText = binding.tvPaceValue.text.toString()
            pace = if (paceText.contains(":") || paceText.contains("m") || paceText.contains("s")) {
                paceText
            } else {
                paceText.replace(" m","").replace(" s","")
            }
            sp.setAvatarPace(pace)
            if (distance.contains("m")){
                distance=distance.replace("m","")
            }
            else if (distance.contains("M")){
                distance= distance.replace("M","")
            }

            if (binding.tvDistanceValue.text.toString().equals("Select")) {
                sp.setAvatarDistacne("0")
            }else{
                sp.setAvatarDistacne(distance)
            }
            if (isZeroOrUnsetTime(time)) {
                sp.setAvatarTime("0")
            }else{
                sp.setAvatarTime(time)
            }

            if ((distance.equals("Select") || distance.isBlank()) &&
                (time.equals("Select") || time.equals("0m 0s") || time.isBlank())
            ) {
                Toast.makeText(
                    requireContext(),
                    "Please select time or distance",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (pace.equals("0m 0s") || pace.equals("Select") || pace.isBlank()){
                Toast.makeText(requireContext(), "Please select pace", Toast.LENGTH_SHORT).show()
            }
            else {
                val is_time_or_distance: Boolean
                val amount: String
                if(binding.tvTimeValue.text.toString().equals("Select") )
                {
                    is_time_or_distance = false
                    amount = distance
                }
                else
                {
                    is_time_or_distance = true
                    amount = time
                }
                val createAvatarRequest = CreateAvatarRequest(
                    is_time_or_distance,
                    amount,
                    pace,
                    sp.getSelectedMachine()!!,
                )
                viewModel.createProgResponse.value = null
                viewModel.createAvatarTraining(createAvatarRequest, "Bearer " + sp.getAToken()!!)

                var navigated = false
                viewModel.createProgResponse.observe(viewLifecycleOwner) { response ->
                    if (navigated || response == null) return@observe
                    navigated = true
                    val bundle = Bundle()
                    bundle.putInt("index",2)
                    response.body?.id?.let { bundle.putString("avatarTrainingId", it) }
                    Log.d("AvatarFragment", "Bot mode createAvatarTraining response: id=${response.body?.id}, navigating with avatarTrainingId=${bundle.getString("avatarTrainingId")}")
                    findNavController().navigate(R.id.nav_loading,bundle)
                }
                handler.postDelayed({
                    if (!navigated && isAdded) {
                        navigated = true
                        Log.d("AvatarFragment", "Bot mode createAvatarTraining timeout, navigating without training id")
                        val bundle = Bundle()
                        bundle.putInt("index",2)
                        findNavController().navigate(R.id.nav_loading,bundle)
                    }
                }, 5000)
            }
    }

    private fun startPlayer1Player2Training() {
        Log.d("AvatarFragment", "startPlayer1Player2Training called")

        if (!Player2ConnectionManager.isInitiator) {
            Toast.makeText(
                requireContext(),
                "Waiting for the other player to start training...",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        distance = binding.tvDistanceValue.text.toString()
        time = binding.tvTimeValue.text.toString()
        
        // Clean up distance string
        var cleanDistance = distance
        if (cleanDistance.contains("m")) {
            cleanDistance = cleanDistance.replace("m", "")
        } else if (cleanDistance.contains("M")) {
            cleanDistance = cleanDistance.replace("M", "")
        }
        
        // Validate inputs
        if (cleanDistance.equals("Select") && time.equals("Select")) {
            Toast.makeText(
                requireContext(),
                "Please select time or distance",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        // Check if connected to Player2
        if (!Player2ConnectionManager.isConnected) {
            Toast.makeText(
                requireContext(),
                "Please connect to Player2 first",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        Log.d("AvatarFragment", "Connection verified, preparing training data...")
        
        // Set session data
        sp.setAvatarDistacne(if (cleanDistance.equals("Select")) "0" else cleanDistance)
        sp.setAvatarTime(if (time.equals("Select")) "0" else time)
        sp.setAvatarPace(pace)
        
        // Determine if it's time or distance based
        val is_time_or_distance: Boolean
        val amount: String
        if (time.equals("Select")) {
            is_time_or_distance = false
            amount = cleanDistance
        } else {
            is_time_or_distance = true
            amount = time
        }
        
        // Create the avatar training request with the selected pace for Player2 mode
        val createAvatarRequest = CreateAvatarRequest(
            is_time_or_distance,
            amount,
            pace,
            sp.getSelectedMachine()!!,
        )
        
        Log.d("AvatarFragment", "Making API call for training setup...")

        viewModel.createProgResponse.value = null
        // Make the API call (required for the training screen to work)
        viewModel.createAvatarTraining(createAvatarRequest, "Bearer " + sp.getAToken()!!)

        var navigated = false
        viewModel.createProgResponse.observe(viewLifecycleOwner) { response ->
            if (navigated || response == null) return@observe
            navigated = true

            // Prepare navigation bundle with synchronized start time (20 s countdown)
            val bundle = Bundle()
            val trainingStartAt = System.currentTimeMillis() + 20000L
            bundle.putInt("index", 2)
            bundle.putBoolean("isPlayer2Mode", true)
            bundle.putString("player2Device", selectedPlayer2Device?.address ?: "")
            bundle.putString("avatarTime", if (time.equals("Select")) "0" else time)
            bundle.putString("avatarDistance", if (cleanDistance.equals("Select")) "0" else cleanDistance)
            bundle.putString("avatarPace", pace)
            bundle.putLong("trainingStartAt", trainingStartAt)
            response.body?.id?.let { bundle.putString("avatarTrainingId", it) }

            Log.d("AvatarFragment", "PvP initiator createAvatarTraining response: id=${response.body?.id}, navigating with avatarTrainingId=${bundle.getString("avatarTrainingId")}")

            // Send training start command to Player2
            Player2ConnectionManager.sendTrainingStart(bundle)

            // Navigate to loading screen (Player1)
            findNavController().navigate(R.id.nav_loading, bundle)
        }
        handler.postDelayed({
            if (!navigated && isAdded) {
                navigated = true
                Log.d("AvatarFragment", "PvP initiator createAvatarTraining timeout, navigating without training id")
                val bundle = Bundle()
                val trainingStartAt = System.currentTimeMillis() + 20000L
                bundle.putInt("index", 2)
                bundle.putBoolean("isPlayer2Mode", true)
                bundle.putString("player2Device", selectedPlayer2Device?.address ?: "")
                bundle.putString("avatarTime", if (time.equals("Select")) "0" else time)
                bundle.putString("avatarDistance", if (cleanDistance.equals("Select")) "0" else cleanDistance)
                bundle.putString("avatarPace", pace)
                bundle.putLong("trainingStartAt", trainingStartAt)
                Player2ConnectionManager.sendTrainingStart(bundle)
                findNavController().navigate(R.id.nav_loading, bundle)
            }
        }, 5000)
    }
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setPaceData() {
        binding.ivPaceArrow.setOnClickListener {
            showTimePickerDialog()
        }

        val pace = resources.getStringArray(R.array.time_array)

        pacetimeAdapter= PaceTimeAdapter(requireContext(), pace, this)
        binding.rvPace.adapter=pacetimeAdapter
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setTimeData() {
        binding.ivArrow.setOnClickListener {
            showTimePickerDialog(true)
        }
    }

    private fun _setDistanceData() {

        binding.ivArrowD.setOnClickListener {
            if (distanceClicked){
                binding.ivArrowD.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvDistance.visibility= View.GONE
                distanceClicked=false
            }
            else{
                binding.ivArrowD.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvDistance.visibility= View.VISIBLE
                distanceClicked=true
            }

        }
        val distance = resources.getStringArray(R.array.distance_array)
        val distanceAdapter= DistanceAdapter(requireContext(), distance, this)
        binding.rvDistance.adapter=distanceAdapter
    }

    // OnClickSpinner interface implementation for spinner items (time/distance/pace)
    override fun clickItem(id: Int, name: String, isTime: Int) {
        if (isTime==0) {
            binding.ivArrow.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvSpinner.visibility= View.GONE
            binding.tvDistanceValue.setText("Select")
            if (name.equals("5min")){
                binding.tvTimeValue.setText("5m 0s")
                time="5m 0s"
            }
            else if (name.equals("1min")){
                binding.tvTimeValue.setText("1m 0s")
                time="1m 0s"
            }
            else if (name.equals("Select Time")){
                binding.tvTimeValue.setText("Select")
                time="0"
            }
            else{
                time=name
                binding.tvTimeValue.setText(name)
            }
        }
        else if (isTime == 1){
            binding.ivArrowD.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvDistance.visibility= View.GONE
            distance=name
            if (name.equals("Select Distance")){
                binding.tvDistanceValue.setText("Select")
            }else {
                binding.tvDistanceValue.setText(name)
            }
            binding.tvTimeValue.setText("Select")
        }
        else{
            binding.ivPaceArrow.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvPace.visibility= View.GONE
            if (name.equals("5min")){
                binding.tvPaceValue.setText("05:00")
                pace="05"
            }
            else if (name.equals("1min")){
                binding.tvPaceValue.setText("01:00")
                pace="01"
            } else if (name.equals("Select Time")){
                binding.tvPaceValue.setText("Select")
                pace="0"
            }
            else{
                pace=name
                binding.tvPaceValue.setText(name)
            }
        }
    }
    
    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle(getString(R.string.avatar_training),false,true,false)
    }
}