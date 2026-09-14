package com.kayakpro.erg.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object FileLogger {

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context, fileName: String = "app_logs.txt") {
        val dir = context.getExternalFilesDir(null)  // private folder
        logFile = File(dir, fileName)
        if (!logFile!!.exists()) logFile!!.createNewFile()
    }

    fun log(tag: String, message: String) {
        Log.d(tag, message)  // keep it in Logcat too
        writeToFile("[$tag] ${dateFormat.format(Date())}: $message")
    }

    private fun writeToFile(text: String) {
        try {
            val writer = FileWriter(logFile, true)
            writer.appendLine(text)
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getLogFile(): File? = logFile
}