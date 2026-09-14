package com.biocube.bioaccess.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

import com.kayakpro.erg.KayakApp


import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SesssionManager @Inject constructor(@ApplicationContext context: Context?) {

    val sp_login: SharedPreferences =
        context!!.getSharedPreferences("KAYAK_PRO", Context.MODE_PRIVATE)
    var sp_editor: SharedPreferences.Editor = sp_login.edit()

    var BRANCHID = "branchid"
    val AuthToken = "aToken"
    val Bpm = "bpm"
    val Calorie = "calorie"
    val Pace = "pace"
    val RefershToken = "rToken"
    val MACHINE = "machine"
    val STROKERATE="stroke_rate"
    val TIME="time"
    val DISTANCE="distance"
    val LISTDISTANCE = "list_distance"
    val DEVICEDISTANCE="device_distance"
    val AVATAR_TIME="avttime"
    val AVATAR_PACE="avtpace"
    val AVATAR_DISTANCE="avtdistance"
    val WATTS="watts"
    val isDeviceConnected="isDeviceConnected"
    val isDeviceTimeSelected="isDeviceTime"
    val isPlayAgain="isPlayAgain"

    companion object {
        private var instance: SesssionManager? = null
        fun getInstance(): SesssionManager? {
            if (instance == null) {
                synchronized(SesssionManager::class.java) {
                    if (instance == null) {
                        instance = SesssionManager(KayakApp.instance)
                    }
                }
            }
            return instance
        }
    }

    private var instance_: SesssionManager? = null
    fun getInstance(context: Context?): SesssionManager? {
        if (instance == null) {
            synchronized(SesssionManager::class.java) {
                if (instance == null) {
                    instance = SesssionManager(context)
                }
            }
        }
        return instance
    }

    fun setDeviceConnected(boolean: Boolean){
        sp_editor.putBoolean(isDeviceConnected,boolean)
        sp_editor.commit()
    }
    fun setDeviceTimeConnected(boolean: Boolean){
        sp_editor.putBoolean(isDeviceTimeSelected,boolean)
        sp_editor.commit()
    }
    fun setPlayAgain(value: Int){//value 0=pt, 1=PTWithout, 2=home, 3=history
        sp_editor.putInt(isPlayAgain,value)
        sp_editor.commit()
    }
    fun setAuthToken(token: String) {
        sp_editor.putString(AuthToken, token)
        sp_editor.commit()
    }
    fun setTime(time: String) {
        sp_editor.putString(TIME, time)
        sp_editor.commit()
    }
    fun setDistacne(distance: String) {
        sp_editor.putString(DISTANCE, distance)
        sp_editor.commit()
    }
    fun setListDistance(listDistance: String) {
        sp_editor.putString(LISTDISTANCE, listDistance)
        sp_editor.commit()
    }


    fun setAvatarTime(time: String) {
        sp_editor.putString(AVATAR_TIME, time)
        sp_editor.commit()
    }
    fun setAvatarPace(pace: String) {
        Log.d("Pace__","inside sp pace storing is $pace")
        sp_editor.putString(AVATAR_PACE, pace)
        sp_editor.commit()
    }
    fun setAvatarDistacne(distance: String) {
        sp_editor.putString(AVATAR_DISTANCE, distance)
        sp_editor.commit()
    }

    fun setSTROKERATE(token: String) {
        sp_editor.putString(STROKERATE, token)
        sp_editor.commit()
    }

    fun setWatts(token: String) {
        sp_editor.putString(WATTS, token)
        sp_editor.commit()
    }
    fun setCalculatedDistance(distance: String){
        sp_editor.putString(DEVICEDISTANCE, distance)
        sp_editor.commit()
    }
    fun setBPM(bpm: String) {
        sp_editor.putString(Bpm, bpm)
        sp_editor.commit()
    }
    fun setCalorie(calorie: String) {
        sp_editor.putString(Calorie, calorie)
        sp_editor.commit()
    }
    fun setPace(pace: String){
        sp_editor.putString(Pace, pace)
        sp_editor.commit()
    }
    fun setSelectedMachine(machineName: String) {
        sp_editor.putString(MACHINE, machineName)
        sp_editor.commit()
    }
    fun setRefreshToken(token: String) {
        sp_editor.putString(RefershToken, token)
        sp_editor.commit()
    }
    fun getAToken(): String? {
        return sp_login.getString(AuthToken, "")
    }
    fun getStrokRate():String?{
        return sp_login.getString(STROKERATE,"0")
    }
    fun getDeviceDistance():String?{
        return sp_login.getString(DEVICEDISTANCE,"0")
    }
    fun getTime():String?{
        return sp_login.getString(TIME,"05")
    }
    fun getDistance():String?{
        return sp_login.getString(DISTANCE,"0")
    }
    fun getListDistance():String?{
        return sp_login.getString(LISTDISTANCE, "0")
    }
    fun getAvatarTime():String?{
        return sp_login.getString(AVATAR_TIME,"05")
    }
    fun getAvatarPace():String?{
        return sp_login.getString(AVATAR_PACE,"01")
    }
    fun getAvatarDistance():String?{
        return sp_login.getString(AVATAR_DISTANCE,"0")
    }
    fun getWatts():String?{
        return sp_login.getString(WATTS,"0")
    }
    fun getBPM(): String? {
        return sp_login.getString(Bpm, "0")
    }
    @JvmName("getCalorieValue")
    fun getCalorie(): String? {
        return sp_login.getString(Calorie, "0")
    }
    @JvmName("getPaceValue")
    fun getPace(): String? {
        return sp_login.getString(Pace, "0")
    }
    fun isDeviceConnected():Boolean {
        return sp_login.getBoolean(isDeviceConnected, false)
    }
    fun isDeviceTimeSelected():Boolean {
        return sp_login.getBoolean(isDeviceTimeSelected, false)
    }
    fun isPlayAgain():Int{
        return sp_login.getInt(isPlayAgain,0)
    }
    //    fun getStroke(): String? {
//        return sp_login.getString(Stroke, "")
//    }
//    fun getWatts(): String? {
//        return sp_login.getString(Watts, "")
//    }
    fun getSelectedMachine(): String? {
        return sp_login.getString(MACHINE, "")
    }
    fun getRToken(): String? {
        return sp_login.getString(RefershToken, "")
    }

    fun logoutSession(): Boolean {
        sp_editor.clear()
        sp_editor.commit()
        return true
    }





    fun setBranchId(value: String) {
        sp_editor.putString(BRANCHID, value)
        sp_editor.commit()
    }

}












