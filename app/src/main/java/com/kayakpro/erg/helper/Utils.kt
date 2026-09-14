package com.kayakpro.erg.helper

import com.kayakpro.erg.model.ErrorResponse
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.kayakpro.erg.model.ErrorModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Utils {


    fun getAppVersion(mCotext: Context): String {
        var version = ""
        try {
            val manager = mCotext.packageManager
            val info: PackageInfo
            info = manager.getPackageInfo(
                mCotext.packageName, 0
            )
            //version = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: Exception) {

        }

        return version
    }

    fun isEmailValid(email:String):Boolean{
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return false
        }
        return true
    }

    fun showToast(mContext: Context, message: String) {
        Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show()
    }
    fun <T> getError(result: retrofit2.Response<T>): ErrorModel {
        try {
            val jObjError = JSONObject(result.errorBody()!!.string())
//            try{
//                //Log.d("Analysis__","In error response ${Gson().toJson(result)}")
//            }catch (e:java.lang.Exception){
//                e.printStackTrace()
//            }
            val model = ErrorModel().apply {


                if (jObjError.getString("message").toString().isNullOrEmpty()) {
                    this.message = jObjError.getString("message").toString()
                } else {
                    this.message = jObjError.getString("message").toString()
                }
            }
            return model
        } catch (e: Exception) {
            val model = ErrorModel().apply {
                this.message = result.message()
                this.code = result.code().toString()
            }
            return model
        }


    }

     fun isAppInstalled(packageManager:PackageManager, packageName: String): Boolean {
        val packageManager = packageManager
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun handleErrorResponse(errorResponse: ErrorResponse):String? {
        // Handle error response
        // You can also handle field errors
        errorResponse.fieldErrors?.forEach { fieldError ->
            Log.e("Error", "Field error: ${fieldError.message} on field ${fieldError.field}")
            return "${fieldError.message}"
        }
        return "Please retry after sometime"
    }

    fun convertDate(
        dateString: String,
        inputFormat: String = "yyyy-MM-dd",
        outputFormat: String = "dd-MM-yyyy"
    ): String? {
        return try {
            val inputDateFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
            val outputDateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
            val date = inputDateFormat.parse(dateString) // Parse the input date string
            if (date != null) {
                outputDateFormat.format(date) // Format to output format
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertIsoDate(
        isoDate: String,
        outputFormat: String = "dd-MM-yyyy HH:mm:ss"
    ): String? {
        return try {
            // Parse the ISO 8601 date string
            val parsedDate = OffsetDateTime.parse(isoDate)
            // Create a formatter for the desired output format
            val formatter = DateTimeFormatter.ofPattern(outputFormat, Locale.getDefault())
            // Format the parsed date into the desired output format
            parsedDate.format(formatter)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}