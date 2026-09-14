package com.biocube.bioaccess.session

import android.content.Context

object TokenManager {

    private const val PREF_NAME = "auth_prefs"
    private const val KEY_JWT = "jwt_token"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_JWT, token).apply()
    }

    fun getToken(context: Context?): String? {
        val prefs = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs?.getString(KEY_JWT, null)
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}