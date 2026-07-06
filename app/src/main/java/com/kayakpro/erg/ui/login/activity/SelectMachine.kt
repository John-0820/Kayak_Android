package com.kayakpro.erg.ui.login.activity

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.MachinetListAdapter
import com.kayakpro.erg.databinding.ActivitySelectMachineBinding
import com.kayakpro.erg.model.MachineDetails
import dev.bluefalcon.BluetoothPermissionException

class SelectMachine : BaseActivity(),MachinetListAdapter.OnClickItem{
    private var param1: String? = null
    private var param2: String? = null
    private var alMachine=ArrayList<MachineDetails>()
    private lateinit var binding: ActivitySelectMachineBinding
    var adapter: MachinetListAdapter?=null
    private var width = 0
    private var height = 0
    private lateinit var sesssionManager:SesssionManager
    
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
        binding = ActivitySelectMachineBinding.inflate(layoutInflater)
        setContentView(binding.root)
       init()
    }
    private fun init() {
        try {
            sesssionManager= SesssionManager(this@SelectMachine)
            val bundle = intent.extras
            val bluetoothDeviceModel = bundle!!.getParcelable<BluetoothDevice>("BluetoothDeviceModel")
            if (bluetoothDeviceModel!=null) {
                if (!hasBluetoothConnectPermission()) {
                    return
                }
                val deviceName = bluetoothDeviceModel.name ?: "Unknown Device"
                Toast.makeText(this, "Device connected with $deviceName",Toast.LENGTH_SHORT).show()
            }
            binding.btnNext.setOnClickListener {
                if (sesssionManager.getSelectedMachine()!!.length>0) {
                    val bundle=Bundle()
                    bundle.putParcelable("BluetoothDeviceModel",bluetoothDeviceModel)
                    startActivity(Intent(this, HomeScreen::class.java).putExtras(bundle))
                    finish()
                }else{
                    showDialog(resources.getString(R.string.select_one_machine))
                }
            }
            binding.rvMachines.getViewTreeObserver()
                .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        if (binding.rvMachines.getViewTreeObserver()
                                .isAlive()
                        ) binding.rvMachines.getViewTreeObserver().removeOnPreDrawListener(this)
                        width = binding.rvMachines.getWidth()  / 3
                        height = binding.rvMachines.getHeight()  / 3

                        return true
                    }
                })


            adapter= MachinetListAdapter(this@SelectMachine,alMachine,this)
            binding.rvMachines.layoutManager= GridLayoutManager(this@SelectMachine,3)
            binding.rvMachines.adapter=adapter
            prepareData()

        } catch (exception: BluetoothPermissionException) {
        }
    }

    private fun prepareData() {
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_kayak)!!,resources.getString(R.string.kayak)))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_canoe)!!,resources.getString(R.string.canoe)))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_dragonboat)!!,resources.getString(R.string.dragon_boat)))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_swim)!!,resources.getString(R.string.swim)))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_ski)!!,resources.getString(R.string.ski)))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_row)!!,resources.getString(R.string.row),1))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_sup)!!,resources.getString(R.string.sup),1))
        alMachine.add(MachineDetails(AppCompatResources.getDrawable(this,R.drawable.ic_bike)!!,resources.getString(R.string.bike),1))

        if (sesssionManager.getSelectedMachine()!!.length>0){
                var selectedMachine=sesssionManager.getSelectedMachine()
                alMachine.forEachIndexed{index, machineDetails ->

                    if (machineDetails.name.equals(selectedMachine)){
                        machineDetails.isSelected=true
                    }
                }
        }
        adapter!!.notifyDataSetChanged()

    }

    override fun clickItem(id: Int, lastpos: Int) {
        alMachine.get(id).name?.let {
            sesssionManager.getInstance(this)!!.setSelectedMachine(it)
        }
        if (lastpos==-1){
            alMachine.get(id).isSelected=true
        }
        else {
            alMachine.get(lastpos).isSelected=false
            alMachine.get(id).isSelected=true
        }
        adapter!!.notifyDataSetChanged()
    }
}