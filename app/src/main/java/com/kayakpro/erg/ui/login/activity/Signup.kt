package com.kayakpro.erg.ui.login.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ActivitySignupBinding
import com.kayakpro.erg.helper.Utils
import com.kayakpro.erg.model.requestmodel.LoginRequest
import com.kayakpro.erg.viewmodels.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Signup : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    private val viewModel: RegisterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observer()
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
        binding.tvLogin.setOnClickListener {

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.ivNext.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pswd = binding.etPswd.text.toString().trim()
            val cnfmpswd = binding.etCnfmPswd.text.toString().trim()
            if (TextUtils.isEmpty(email)) {
                showDialog(this.resources.getString(R.string.empty_email))
            }
            else if (TextUtils.isEmpty(pswd)) {
                showDialog(this.resources.getString(R.string.empty_pswd))
            }
            else if (TextUtils.isEmpty(cnfmpswd)) {
                showDialog(this.resources.getString(R.string.empty_cnfm_pswd))
            }
            else if (!Utils.isEmailValid(email)) {
                showDialog(this.resources.getString(R.string.email_invalid))
            }
            else if (pswd.length < 6 || cnfmpswd.length < 6) {
                showDialog(this.resources.getString(R.string.minimum_length))
            }
            else if (!pswd
                    .equals(cnfmpswd)) {
                showDialog(this.resources.getString(R.string.pswd_mismatch))
            }
            else {
                viewModel.doRegister(LoginRequest("",email,pswd,binding!!.chkOptout.isChecked))
            }
        }
    }

    private fun observer() {
        viewModel.signupResponse .observe(this){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
        viewModel.error.observe(this){
            showDialog(it)
        }
    }

        fun showDialog(message:String){
            val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
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