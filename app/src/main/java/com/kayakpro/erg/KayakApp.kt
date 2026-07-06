package com.kayakpro.erg

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kayakpro.erg.bluetooth.KayakBleConnectionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KayakApp:Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this
        KayakBleConnectionManager.initialize(this)
        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCustomKey("AppStartTime", System.currentTimeMillis());
    }

    companion object {

        lateinit var instance: KayakApp

    }
}