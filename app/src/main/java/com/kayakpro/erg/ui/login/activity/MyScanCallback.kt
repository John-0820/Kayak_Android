package com.kayakpro.erg.ui.login.activity

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

class MyScanCallback(private val onDeviceFound: (ScanResult) -> Unit) : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        onDeviceFound(result)
    }
}