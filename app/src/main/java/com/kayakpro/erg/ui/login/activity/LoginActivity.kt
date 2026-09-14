package com.kayakpro.erg.ui.login.activity

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ActivityLoginBinding
import com.kayakpro.erg.helper.Utils.isEmailValid
import com.kayakpro.erg.model.requestmodel.LoginRequest
import com.kayakpro.erg.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var pDialog: Dialog? = null
    val sp= SesssionManager.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var aToken=sp!!.getAToken()
        if (sp.getAToken()!!.length>10){
            openPostLoginScreen()
            return
        }
        pDialog = Dialog(this)
        pDialog?.setContentView(R.layout.progress_bar_layout)
        pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pDialog!!.setCancelable(false)
        initObserver()
        binding.ivPswdView.setOnClickListener {
            if (binding.etPswd.transformationMethod is PasswordTransformationMethod) {
                binding.etPswd.transformationMethod = SingleLineTransformationMethod()
                binding.ivPswdView.setImageDrawable(resources.getDrawable(R.drawable.ic_open_eye,null))
            } else {
                binding.etPswd.transformationMethod = PasswordTransformationMethod()
                binding.ivPswdView.setImageDrawable(resources.getDrawable(R.drawable.ic_close_eye,null))
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
             overridePendingTransition(R.anim.right_left_anim,R.anim.right_left_anim)
            //finish()
        }
        binding.tvFrgtPswd.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
            overridePendingTransition(R.anim.right_left_anim,R.anim.right_left_anim)
            //finish()
        }
        binding.ivNext.setOnClickListener {
            val email=binding.etEmail.text.toString().trim()
            val pswd=binding.etPswd.text.toString().trim()
             if (TextUtils.isEmpty(email)) {
                showDialog(this.resources.getString(R.string.both_field_required))
            }
            else if (TextUtils.isEmpty(pswd)) {
                showDialog(this.resources.getString(R.string.both_field_required))
            }
            else if (!isEmailValid(email)) {
                showDialog(this.resources.getString(R.string.email_invalid))
            }
            else {
                viewModel.doLogin(LoginRequest(email,"",pswd))
            }
        }
    }
    private fun initObserver() {
        viewModel.loginResponse.observe(this){
           try {
                var sesssionManager=SesssionManager(this)
               sesssionManager.getInstance(this)!!.setAuthToken(it.body.id_token)
               sesssionManager.getInstance(this)!!.setRefreshToken(it.body.refresh_token)
           }catch (e:Exception){
               e.printStackTrace()
           }
            openPostLoginScreen()
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
    private fun openPostLoginScreen() {
        startActivity(
            Intent(this, BluetoothScreen::class.java),
            ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
        )
        finish()
    }

    fun dismissLoader() {
        if (pDialog != null) {
            pDialog!!.dismiss()
            pDialog = null
        }
    }
    fun showDialogs() {
        if (pDialog!=null)
        if (!pDialog!!.isShowing) {
            pDialog!!.show()
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