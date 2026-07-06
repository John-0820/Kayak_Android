package com.kayakpro.erg.interfaces

import android.bluetooth.BluetoothDevice

interface OnClickBluetooth {
        fun clickItem(id: Int, name:BluetoothDevice)
    }