package com.kayakpro.erg.ui.login.activity

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kayakpro.erg.R

open class BaseActivity:AppCompatActivity() {

    fun showDialog(message:String){
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.custom_error_dialog,null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        builder.setView(view)
        tvMessage.setText(message)
        close.setOnClickListener {
            builder.dismiss()
        }
        view.findViewById<android.widget.Button>(R.id.btn_okay).setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

}