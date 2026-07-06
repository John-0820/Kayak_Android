package com.kayakpro.erg.ui.login.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ActivityForgotPasswordBinding
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.requestmodel.ForgotPasswordRequest
import com.kayakpro.erg.network.Constants
import com.kayakpro.erg.viewmodels.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()
    private var pDialog: Dialog? = null
    private var email=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pDialog = Dialog(this)
        pDialog?.setContentView(R.layout.progress_bar_layout)
        pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pDialog!!.setCancelable(false)
        initObserver()
        binding.ivNext.setOnClickListener {
             email =binding.etEmail.text.toString().trim()
            if (TextUtils.isEmpty(email)) {
            showDialog(this.resources.getString(R.string.empty_email))
        } else if (!Utils.isEmailValid(binding.etEmail.text.toString().trim())) {
            showDialog(this.resources.getString(R.string.email_invalid))
        } else {
                    val jsonObject= ForgotPasswordRequest(email,"","")
                viewModel.resetPswd(jsonObject)
        } }
        binding.ivBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.right_left_anim,R.anim.right_left_anim)
            finish()
        }
    }

    private fun initObserver() {

        viewModel.loginResponse.observe(this){
            showDialog(resources.getString(R.string.otp_sent,email))

        }
        viewModel.error.observe(this){
            showDialog(it)
        }

        viewModel.loading.observe(this) {
            try {
                if (it) {
                    showDialogs()
                } else {
                    dismissLoader()
                }
            } catch (e: Exception) {
                dismissLoader()
                e.printStackTrace()
            }
        }
    }
    fun dismissLoader() {
        if (pDialog != null) {
            pDialog!!.dismiss()
            pDialog = null
        }
    }

    fun showDialogs() {
        if (!pDialog!!.isShowing) {
            pDialog!!.show()
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun showDialog(message:String){
        val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.custom_error_dialog,null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        val btnOkay = view.findViewById<Button>(R.id.btn_okay)
        btnOkay.visibility=View.VISIBLE
            btnOkay.setOnClickListener {
                startActivity(Intent(this, ResetPasswordActivity::class.java).putExtra(Constants.EMAIL,email))
                overridePendingTransition(R.anim.right_left_anim,R.anim.right_left_anim)
                finish()
            }
        builder.setView(view)
        tvMessage.setText(message)
        close.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

}