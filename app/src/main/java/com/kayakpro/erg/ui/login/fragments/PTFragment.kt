package com.kayakpro.erg.ui.login.fragments

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.kayakpro.erg.adapters.TrainingAdapter
import com.kayakpro.erg.databinding.FragmentPTBinding
import com.kayakpro.erg.interfaces.OnClickBluetooth
import com.kayakpro.erg.interfaces.OnClickDelete
import com.kayakpro.erg.model.RBody
import com.kayakpro.erg.viewmodels.TrainingListViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.bluefalcon.BluetoothPermissionException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PTFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class PTFragment : Fragment(),OnClickBluetooth,OnClickDelete {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPTBinding
    private var alProgram=ArrayList<RBody>()
    private val viewModel: TrainingListViewModel by viewModels()
    val sp= SesssionManager.getInstance()
    private var pDialog: Dialog? = null
    var adapter: TrainingAdapter?=null
    var models:RBody?=null
    var builder:AlertDialog?=null
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
        try {
            // Inflate the layout for this fragment
            if (!this::binding.isInitialized) {
                Log.d("Analysis__", "Inside PTFRAGMENT")
                binding = FragmentPTBinding.inflate(inflater)
                if (arguments != null) {
                }
                init()
            }
        }catch (e:Exception){

        }
        return binding.root    }

    private fun init() {
        try {
            pDialog = Dialog(requireContext())
            pDialog?.setContentView(R.layout.progress_bar_layout)
            pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pDialog!!.setCancelable(false)
            observeData()
            binding.addFab.setOnClickListener {
                showDialog()
            }
        } catch (exception: BluetoothPermissionException) {
            // Toast.makeText(requireContext(),exception.message,Toast.LENGTH_SHORT).show()
            //request the ACCESS_COARSE_LOCATION permission
        }
    }
    private fun observeData() {
        viewModel.trainingResponse.observe(requireActivity())
        {
            try {
                alProgram.clear()
                alProgram.addAll(it.body)
                adapter= TrainingAdapter(requireContext(),alProgram,this)
                binding.rvProgramHistory.adapter=adapter
                if (adapter!!.itemCount>0){
                    binding.tvAddProg.visibility = View.GONE
                }else{
                    binding.tvAddProg.visibility = View.VISIBLE
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        viewModel.error.observe(requireActivity()){
            showDialog(it)
        }
        viewModel.isDeleted.observe(requireActivity()){
            if (models!=null) {
                alProgram.remove(models)
                adapter!!.notifyDataSetChanged()
                if (adapter!!.itemCount>0){
                    binding.tvAddProg.visibility = View.GONE
                }else
                {
                    binding.tvAddProg.visibility = View.VISIBLE
                }
            }
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

    override fun onResume() {
        super.onResume()
        if (builder!=null){
            builder!!.dismiss()
        }
        getTrainings()
    }

    override fun onStop() {
        super.onStop()
        if (builder!=null){
            builder!!.dismiss()
            builder=null
        }
    }
    fun showDialog(){
        findNavController().navigate(R.id.nav_create_program)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PTFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PTFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun clickItem(id: Int, name: BluetoothDevice) {

    }
    private fun getTrainings() {

        viewModel.getTraining(sp!!.getSelectedMachine().toString(),"Bearer "+sp?.getAToken().toString())
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
        try {
            builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .create()
            val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
            builder!!.setView(view)
            tvMessage.setText(message)
            close.setOnClickListener {
                builder!!.dismiss()
            }
            view.findViewById<android.widget.Button>(R.id.btn_okay).setOnClickListener {
                builder!!.dismiss()
            }
            builder!!.setCanceledOnTouchOutside(false)
            builder!!.show()
        }catch (e:Exception){
            builder!!.dismiss()
        }
    }

    override fun clickDelete(model: RBody) {

        models=model
        if (models!=null) {
            viewModel.deleteTraining(model.id!!, "Bearer " + sp!!.getAToken())
        }
    }

    override fun clickSava(model: RBody) {
        val bundle = Bundle()
        bundle.putInt("index",1)
        bundle.putParcelable("model",model)
        sp!!.setPlayAgain(0)
        findNavController().navigate(R.id.nav_loading,bundle)
    }

    override fun clickEdit(model: RBody) {
        val bundle =Bundle()
        bundle.putParcelable("model",model)
        findNavController().navigate(R.id.nav_create_program,bundle)
    }
}