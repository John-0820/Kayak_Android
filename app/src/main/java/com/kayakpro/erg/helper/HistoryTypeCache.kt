package com.kayakpro.erg.helper

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryTypeCache {

    private const val PREFS_NAME = "kayak_history_type_cache"
    private const val MAP_KEY = "history_type_map"
    private const val MAX_ENTRIES = 500

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun ensureInit(): SharedPreferences {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("HistoryTypeCache.init(context) must be called first")
        }
        return prefs
    }

    fun getCachedType(id: String): String? {
        if (id.isEmpty()) return null
        val map = getMap()
        return map[id]
    }

    fun setCachedType(id: String, type: String) {
        if (id.isEmpty() || type.isEmpty()) return
        val map = getMap().toMutableMap()
        map[id] = type
        if (map.size > MAX_ENTRIES) {
            val drop = map.size - MAX_ENTRIES
            map.keys.sorted().take(drop).forEach { map.remove(it) }
        }
        ensureInit().edit().putString(MAP_KEY, Gson().toJson(map)).apply()
    }

    fun clearCachedType(id: String) {
        if (id.isEmpty()) return
        val map = getMap().toMutableMap()
        map.remove(id)
        ensureInit().edit().putString(MAP_KEY, Gson().toJson(map)).apply()
    }

    private fun getMap(): Map<String, String> {
        val json = ensureInit().getString(MAP_KEY, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
