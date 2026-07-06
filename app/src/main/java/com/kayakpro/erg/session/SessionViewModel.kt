package com.biocube.bioaccess.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel  @Inject constructor(
    var sharedPreferences: SesssionManager,
    application:Application
): AndroidViewModel(application)
{
    fun isSync():Boolean{
        return sharedPreferences.sp_login.getBoolean("IS_SYNC",true)
    }

    fun setSync(isSync:Boolean){
        sharedPreferences.sp_editor.putBoolean("IS_SYNC", isSync)
        sharedPreferences.sp_editor.commit()
    }
}



