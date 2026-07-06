/*
package com.kayakpro.erg.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.kayakpro.erg.R

class CustomErrorDialog(private val context: Context) {

    private lateinit var dialog: Dialog

    fun showErrorDialog(
        msg: String,
        positiveButtonText: String,
        negativeButtonText: String? = null,
        onPositiveButtonClick: () -> Unit,
        onNegativeButtonClick: (() -> Unit)? = null
    ) {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_error_dialog, null)
        val error = view.findViewById<TextView>(R.id.tv_error)
        val positiveButton = view.findViewById<TextView>(R.id.btn_okay)
        val negativeButton = view.findViewById<ImageView>(R.id.iv_close_dialog)

        error.text = msg
        positiveButton.text = positiveButtonText

        positiveButton.setOnClickListener {
            onPositiveButtonClick()
            dialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onNegativeButtonClick?.invoke()
            dialog.dismiss()
        }

        dialog = Dialog(context, R.style.CustomErrorDialogTheme)
        dialog.setContentView(view)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        dialog.setCancelable(false)
        dialog.show()
    }

    fun dismissDialog(){
        dialog.dismiss()
    }
}*/
