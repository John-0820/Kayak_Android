package com.kayakpro.erg.ui.login.activity

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ActivityResetPasswordBinding
import com.kayakpro.erg.model.requestmodel.ForgotPasswordRequest
import com.kayakpro.erg.network.Constants
import com.kayakpro.erg.viewmodels.ResetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: ResetPasswordViewModel by viewModels()
    private var pDialog: Dialog? = null
    private var email=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        email= intent.getStringExtra(Constants.EMAIL)!!
        initObserver()
        init()

        binding.ivPswdView.setOnClickListener {
            if (binding.etPswd.transformationMethod is PasswordTransformationMethod) {
                binding.etPswd.transformationMethod = SingleLineTransformationMethod()
                binding.ivPswdView.setImageDrawable(resources.getDrawable(R.drawable.ic_open_eye,null))
            } else {
                binding.etPswd.transformationMethod = PasswordTransformationMethod()
                binding.ivPswdView.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye,null))
            }
        }
        binding.ivCnfmpswdView.setOnClickListener {
            if (binding.etCnfmPswd.transformationMethod is PasswordTransformationMethod) {
                binding.etCnfmPswd.transformationMethod = SingleLineTransformationMethod()
                binding.ivCnfmpswdView.setImageDrawable(resources.getDrawable(R.drawable.ic_open_eye,null))
            } else {
                binding.etCnfmPswd.transformationMethod = PasswordTransformationMethod()
                binding.ivCnfmpswdView.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye,null))
            }
        }
    }

private fun init(){

    pDialog = Dialog(this)
    pDialog?.setContentView(R.layout.progress_bar_layout)
    pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    pDialog!!.setCancelable(false)
    binding.ivNext.setOnClickListener {
        val code = binding.etEmail.text.toString().trim()
        val pswd = binding.etPswd.text.toString().trim()
        val cnfmpswd = binding.etCnfmPswd.text.toString().trim()
        if (TextUtils.isEmpty(code)) {
            showDialog(this.resources.getString(R.string.empty_email))
        }

        else if (TextUtils.isEmpty(pswd)) {
            showDialog(this.resources.getString(R.string.empty_pswd))
        }
        else if (TextUtils.isEmpty(cnfmpswd)) {
            showDialog(this.resources.getString(R.string.empty_cnfm_pswd))
        }
        else if (pswd.length < 6 || cnfmpswd.length < 6) {
            showDialog(this.resources.getString(R.string.minimum_length))
        }
        else if (!pswd
                .equals(cnfmpswd)) {
            showDialog(this.resources.getString(R.string.pswd_mismatch))
        }
        else {
            val jsonobject= ForgotPasswordRequest(email,code,pswd)

            viewModel.finishResetPswd(jsonobject)
        }
    }
}
    private fun initObserver() {
        viewModel.loginResponse.observe(this){
            showDialog(resources.getString(R.string.passwored_reset_done))
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

    fun showDialog(message:String){
        val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.custom_error_dialog,null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnOkay = view.findViewById<Button>(R.id.btn_okay)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        builder.setView(view)
        if (message.contains(resources.getString(R.string.passwored_reset_done))) {
            btnOkay.visibility = View.VISIBLE
            btnOkay.setOnClickListener {
                startActivity(
                    Intent(this, LoginActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                )
                finish()
            }
        } else {
            btnOkay.setOnClickListener {
                builder.dismiss()
            }
        }
        tvMessage.setText(message)
        close.setOnClickListener {
            builder.dismiss()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
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

}