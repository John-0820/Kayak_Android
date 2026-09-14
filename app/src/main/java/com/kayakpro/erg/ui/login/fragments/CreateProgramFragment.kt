package com.kayakpro.erg.ui.login.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.WorkoutAdapter
import com.kayakpro.erg.databinding.FragmentCreateProgramBinding
import com.kayakpro.erg.interfaces.OnDeleteItem
import com.kayakpro.erg.model.RBody
import com.kayakpro.erg.model.WorkoutModel
import com.kayakpro.erg.model.requestmodel.CreateProgramRequest
import com.kayakpro.erg.viewmodels.CreateProgramViewModel
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateProgramFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class CreateProgramFragment : DialogFragment(), OnDeleteItem {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCreateProgramBinding
    private lateinit var adapter: WorkoutAdapter
    private var alWorkout = ArrayList<WorkoutModel>()
    private val sp = SesssionManager.getInstance()
    private val viewModel: CreateProgramViewModel by viewModels()
    private var pDialog: Dialog? = null
    private var model: RBody?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            model=it.getParcelable("model")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!this::binding.isInitialized) {

            binding = FragmentCreateProgramBinding.inflate(inflater)
            init()

        }
        return binding.root
    }


    private fun init() {
        if (model!=null){
            binding.etProgName.setText(model!!.name)
            binding.etDetails.setText(model!!.details)
            for (item in model!!.workouts){
                alWorkout.add(WorkoutModel(item.id?.toString(),item.name,"Time","Distance",item.is_rest, true,item.is_time_or_distance,item.value))
            }
        }
        else {
            alWorkout.add(WorkoutModel("1", "Name the Program", "Time", "Distance", 0, false))
        }
        adapter = WorkoutAdapter(requireContext(), alWorkout, this)
        binding.btnAddWorkout.setOnClickListener {
            if (alWorkout.size < 30) {
                alWorkout.add(
                    WorkoutModel(
                        "${alWorkout.size}",
                        "Name the workout",
                        "Time",
                        "Distance",
                        0,
                        false
                    )
                )
                adapter!!.notifyItemChanged(adapter.itemCount)
                binding.rvWorkout.smoothScrollToPosition(adapter.itemCount)
            }
        }
        binding.rvWorkout.hasFixedSize()
        binding.rvWorkout.adapter = adapter
        binding.btnStart.setOnClickListener {
            if (checkValidation()) {
                if (alWorkout.size > 0) {
                    val alWork = ArrayList<CreateProgramRequest.Workout>()

                    for (item in alWorkout) {
                        alWork.add(
                            CreateProgramRequest.Workout(
                                item.worokoutName!!,
                                item.is_time_or_distance,
                                item.value,
                                item.is_rest
                            )
                        )
                    }
                    val createProgramRequest = CreateProgramRequest(
                        binding.etProgName.text.toString().trim(),
                        binding.etDetails.text.toString().trim(),
                        sp?.getSelectedMachine()!!,
                        alWork
                    )
                    if (model!=null){
                        viewModel.editProgram(model!!.id!!,createProgramRequest, "Bearer " + sp.getAToken()!!)
                    }else {
                        viewModel.createProgram(createProgramRequest, "Bearer " + sp.getAToken()!!)
                    }
                } else {
                    Toast.makeText(requireContext(), "Please create a segment first.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        observeData()
    }

    private fun checkValidation(): Boolean {
        if (binding.etProgName.text.toString().trim().length==0){
            showDialog("Training name is required")
            return false
        }
        return true
    }

    private fun observeData() {
        viewModel.createProgResponse.observe(requireActivity())
        {
            try {
                findNavController().navigateUp()
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
    fun showDialogs() {
        if (pDialog!=null)
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
    fun dismissLoader() {
        if (pDialog != null) {
            pDialog!!.dismiss()
            pDialog = null
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateProgramFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateProgramFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun clickItem(id: Int, model: WorkoutModel) {
        alWorkout.remove(model)
        adapter.notifyDataSetChanged()
        binding.rvWorkout.smoothScrollToPosition(adapter.itemCount)
    }

    override fun clickSave(id: Int, model: WorkoutModel) {
        model.shouldEdit=false
        alWorkout[id] = model
    }

    override fun clickEdit(id: Int, model: WorkoutModel) {
        model.shouldEdit=true
        alWorkout[id] = model

    }
}