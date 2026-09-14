package com.kayakpro.erg.ui.login.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.TrainingAdapter
import com.kayakpro.erg.databinding.FragmentPTBinding
import com.kayakpro.erg.interfaces.OnClickDelete
import com.kayakpro.erg.model.RBody
import com.kayakpro.erg.viewmodels.TrainingListViewModel
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TrainingList.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrainingList : Fragment(),OnClickDelete {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPTBinding
    private var alProgram=ArrayList<RBody>()
    var adapter: TrainingAdapter?=null
    private val viewModel: TrainingListViewModel by viewModels()
    val sp= SesssionManager.getInstance()
    private var pDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!this::binding.isInitialized) {

            binding = FragmentPTBinding.inflate(inflater)
            if (arguments != null) {
            }

            init()
        }
        return binding.root
    }
    private fun init() {

        adapter= TrainingAdapter(requireContext(),alProgram,this)
        binding.rvProgramHistory.adapter=adapter
        getTrainings()
        observeData()

    }
    private fun observeData() {
        viewModel.trainingResponse.observe(requireActivity())
        {
            try {
                alProgram.addAll(it.body)
                adapter!!.notifyDataSetChanged()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
        viewModel.error.observe(requireActivity()){
            showDialog(it)
        }
        viewModel.loading.observe(requireActivity()) {
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
    fun showDialog(message:String){
        val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
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
    private fun getTrainings() {
        viewModel.getTraining(sp!!.getSelectedMachine().toString(),"Bearer "+sp?.getAToken().toString())
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TrainingList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TrainingList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun clickDelete(model: RBody) {
    }

    override fun clickSava(model: RBody) {
    }

    override fun clickEdit(model: RBody) {
    }
}