package com.kayakpro.erg.ui.login.activity

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ActivityHomeScreenBinding
import com.kayakpro.erg.drawer.AdvanceDrawerLayout
import com.kayakpro.erg.viewmodels.DeleteAccountViewModel

import com.kayakpro.erg.interfaces.AppBarCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreen : AppCompatActivity(), AppBarCallback {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController
    private val sp = SesssionManager.getInstance()
    
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (sp!!.isDeviceConnected()) {
            binding.appBarMain.isConnected.setBackgroundColor(
                resources.getColor(
                    R.color.golden_fizz,
                    null
                )
            )
        }
        else {
            binding.appBarMain.isConnected.setBackgroundColor(
                resources.getColor(
                    R.color.txt_color,
                    null
                )
            )
        }
        // Handle optional BluetoothDevice from intent extras (when coming from BluetoothScreen)
        val bundle = intent.extras
        if (bundle != null) {
            val bluetoothDeviceModel = bundle.getParcelable<BluetoothDevice>("BluetoothDeviceModel")
            if (bluetoothDeviceModel != null) {
                if (!hasBluetoothConnectPermission()) {
                    return
                }
                val deviceName = bluetoothDeviceModel.name ?: "Unknown Device"
                Toast.makeText(
                    this,
                    "Device connected with $deviceName",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setHomeAsUpIndicator(null)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        val drawerLayout: AdvanceDrawerLayout = binding.drawerLayout as AdvanceDrawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_pt, R.id.nav_history, R.id.nav_provideos
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        bottomNavigation.setupWithNavController(navController)
        val btnClose: ImageView = navView.getHeaderView(0).findViewById(R.id.iv_close_nav)
        btnClose.setOnClickListener { binding.drawerLayout.closeDrawers() }
        drawerLayout.setViewElevation(Gravity.START, 0f)
        drawerLayout.setRadius(Gravity.START, 80f)

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_dkino -> {
                    redirectToPlayStore("com.kinomap.training", this)
                    true
                }

                R.id.nav_dlogout -> {
                    logOut()
                    true
                }

                R.id.nav_ddeactivate -> {
                    showDeactivateConfirmDialog()
                    true
                }

                R.id.nav_dvideos -> {
                    binding.appBarMain.ivAppbar.visibility = View.VISIBLE
                    navController.navigate(R.id.nav_provideos)
                    true
                }

                R.id.nav_dhistory -> {
                    binding.appBarMain.ivAppbar.visibility = View.VISIBLE
                    navController.navigate(R.id.nav_history)

                    true
                }

                R.id.nav_dhome -> {
                    binding.appBarMain.ivAppbar.visibility = View.VISIBLE
                    navController.navigate(R.id.nav_home)
                    true
                }

                R.id.nav_davatar_training -> {
                    binding.appBarMain.ivAppbar.visibility = View.VISIBLE
                    navController.navigate(R.id.nav_avatar_without)
                    true
                }

                R.id.nav_dprogrammable_training -> {
                    binding.appBarMain.ivAppbar.visibility = View.VISIBLE
                    navController.navigate(R.id.nav_pt)
                    true
                }

                R.id.nav_dconnect_machine -> {
                    val intent = Intent(this, BluetoothScreen::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            val id = navDestination.id
            visibleBottomNavigation(id)
        }
    }

    private fun logOut() {
        var sesssionManager = SesssionManager(this)
        sesssionManager.getInstance(this)!!.logoutSession()
        startActivity(Intent(this@HomeScreen, LoginActivity::class.java))
        finish()
    }

    private fun showDeactivateConfirmDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnOk = view.findViewById<android.widget.Button>(R.id.btn_okay)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)

        tvMessage.text = getString(R.string.deactivate_confirm_message)
        btnOk.text = getString(R.string.deactivate_confirm_btn)
        builder.setView(view)

        close.setOnClickListener { builder.dismiss() }
        btnOk.setOnClickListener {
            builder.dismiss()
            deleteAccount()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun deleteAccount() {
        val deleteViewModel = ViewModelProvider(this)[DeleteAccountViewModel::class.java]
        val token = "Bearer " + sp!!.getAToken().toString()
        deleteViewModel.deleteAccount(token)

        deleteViewModel.deleteResult.observe(this) { response ->
            if (response != null) {
                showDialog(getString(R.string.deactivate_success))
                sp.logoutSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        deleteViewModel.error.observe(this) { errorMsg ->
            showDialog(getString(R.string.deactivate_failed))
        }
    }

    private fun openKinoMap() {
        val packageName = "com.kinomap.training"
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setPackage(packageName)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo != null) {
            startActivity(intent)
        } else {
            showDialog(resources.getString(R.string.kino_not_installed))
        }
    }

    fun redirectToPlayStore(packageName: String, context: android.content.Context) {
        try {
            val playStoreIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            playStoreIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        }
    }

    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        builder.setView(view)
        tvMessage.setText(message)
        close.setOnClickListener {
            builder.dismiss()
        }
        view.findViewById<android.widget.Button>(R.id.btn_okay).setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun visibleBottomNavigation(id: Int) {
        if (id == R.id.nav_home) {
            binding.appBarMain.appbarmain.targetElevation = 0f
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            binding.appBarMain.ivAppbar.visibility = View.VISIBLE
        } else if (id == R.id.nav_pt) {
            binding.appBarMain.appbarmain.targetElevation = 0f
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            binding.appBarMain.ivAppbar.visibility = View.VISIBLE
        } else if (id == R.id.nav_history) {
            binding.appBarMain.appbarmain.targetElevation = 0f
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            binding.appBarMain.ivAppbar.visibility = View.VISIBLE
        } else if (id == R.id.nav_provideos) {
            binding.appBarMain.appbarmain.targetElevation = 0f
            binding.appBarMain.toolbar.visibility = View.VISIBLE
            bottomNavigation.visibility = View.VISIBLE
            binding.appBarMain.ivAppbar.visibility = View.VISIBLE
        }
        else if (id == R.id.nav_workout_fragment) {
            binding.appBarMain.appbarmain.targetElevation = 0f
            bottomNavigation.visibility = View.VISIBLE
            binding.appBarMain.toolbar.visibility = View.VISIBLE
        } else if (id == R.id.nav_avatartraining) {
            binding.appBarMain.toolbar.visibility = View.GONE
            bottomNavigation.visibility = View.GONE
        } else if (id == R.id.nav_create_program) {
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun updateAppBarTitle(
        title: String,
        showImage: Boolean,
        showApp: Boolean,
        showBottom: Boolean
    ) {
        if (showImage) {
            binding.appBarMain.ivAppbar.visibility = View.VISIBLE
        } else {
            binding.appBarMain.ivAppbar.visibility = View.VISIBLE

        }
        if (showApp) {
            binding.appBarMain.toolbar.visibility = View.VISIBLE
        } else {
            binding.appBarMain.toolbar.visibility = View.GONE
        }
        if (showBottom) {
            bottomNavigation.visibility = View.VISIBLE
        } else {
            bottomNavigation.visibility = View.GONE
        }
    }

    override fun updateConnectStatusImage(connectionImage: Int) {
        binding.appBarMain.isConnected.setBackgroundColor(connectionImage)
    }
}