package com.kayakpro.erg.ui.login.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.HistoryAdapter
import com.kayakpro.erg.databinding.FragmentHistoryBinding
import com.kayakpro.erg.interfaces.OnClickExport
import com.kayakpro.erg.model.HistoryResponseModel
import com.kayakpro.erg.viewmodels.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class HistoryFragment : Fragment(),OnClickExport {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentHistoryBinding
    private var alProgram=ArrayList<HistoryResponseModel.RBody>()
    private val viewModel: HistoryViewModel by viewModels()
    var adapter: HistoryAdapter?=null
    private var pDialog: Dialog? = null
    val sp= SesssionManager.getInstance()
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
        if (!this::binding.isInitialized)
        {
            binding = FragmentHistoryBinding.inflate(inflater)
            if (arguments != null)
            {
            }
            init()
        }
        return binding.root
    }
    private fun init() {
        adapter= HistoryAdapter(requireContext(),alProgram,this)
        binding.rvProgramHistory.adapter=adapter
        pDialog = Dialog(requireContext())
        pDialog?.setContentView(R.layout.progress_bar_layout)
        pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pDialog!!.setCancelable(false)
        observeData()
    }

    override fun onResume() {
        super.onResume()
        // Always refetch when the History tab becomes visible so a
        // just-completed training (QuickStart/Avatar/Program) shows up
        // immediately without requiring the user to relaunch the app.
        // Matches iOS's HistoryView, which refetches via .task on every
        // appearance and via the trainingHistoryDidUpdate notification.
        getHistory()
    }

    private fun observeData() {
        viewModel.trainingDataResponse.observe(requireActivity())
        {
            try {
                val bundle = Bundle()
                bundle.putInt("index",1)
                bundle.putParcelable("model",it.body)
                sp!!.setPlayAgain(4)
                findNavController().navigate(R.id.nav_loading,bundle)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        viewModel.historyResponse.observe(requireActivity())
        {
            try {
                alProgram.clear()
                it.body?.let { body -> alProgram.addAll(body) }
                adapter!!.notifyDataSetChanged()
            }catch (e:Exception)
            {
                e.printStackTrace()
            }
        }
        viewModel.isDownloaded.observe(requireActivity()){
            showDialog("Saved in download folder.")
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
        viewModel.isDeleted.observe(requireActivity()) {
            it?.let { message ->
                showDialog(message)
                viewModel.isDeleted.postValue(null)
            }
        }
    }

    private fun getHistory() {
        viewModel.getHistory(sp!!.getSelectedMachine().toString(),"Bearer "+sp?.getAToken().toString())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
        if (pDialog == null) {
            pDialog = Dialog(requireContext())
            pDialog?.setContentView(R.layout.progress_bar_layout)
            pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pDialog!!.setCancelable(false)
        }
        if (!pDialog!!.isShowing) {
            pDialog!!.show()
        }
    }
    fun showDialog(message:String){
        try {
            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .create()
            val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
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
        }catch (e:Exception){

        }
    }

    override fun clickItem(id: Int) {
        viewModel.getFitFile(id,"Bearer "+sp!!.getAToken()!!)
    }

    override fun clickPlayAgain(id: String) {
        viewModel.getTrainingData(id,sp!!.getSelectedMachine().toString(),"Bearer "+sp?.getAToken().toString())
    }

    override fun clickDelete(id: String) {
        viewModel.deleteTraining(id, "Bearer " + sp!!.getAToken())
    }
}