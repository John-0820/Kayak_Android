package com.kayakpro.erg.ui.login.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.DistanceAdapter
import com.kayakpro.erg.adapters.TimeAdapter
import com.kayakpro.erg.databinding.FragmentQuickStartBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.interfaces.OnClickSpinner


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuickStart.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuickStart : Fragment(),OnClickSpinner {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var timeAdapter:TimeAdapter
    private lateinit var appBarCallback: AppBarCallback
    var timeClicked=false
    var distanceClicked=false
    var time=""
    var distance=""
    val sp= SesssionManager.getInstance()
    private lateinit var dialog: Dialog
    private lateinit var binding: FragmentQuickStartBinding
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
        // Inflate the layout for this fragment
        if (!this::binding.isInitialized) {
            binding = FragmentQuickStartBinding.inflate(inflater)
            if (arguments != null) {

            }
            init()
        }
        return binding.root    }


    private fun init() {
        updateAppBarTitle()
        setTimeData()
        _setDistanceData()
        binding.btnStart.setOnClickListener {

            distance=binding.tvDistanceValue.text.toString()
            time=binding.tvTimeValue.text.toString()

            if (distance.contains("m")){
                distance=distance.replace("m","")
            }
            else if (distance.contains("M")){
                distance= distance.replace("M","")
            }

            if (distance.equals("Select")) {
                sp!!.setDistacne("0")
            }else{
                sp!!.setListDistance(distance)
                sp!!.setDistacne(distance)
            }
            if (time.equals("Select")) {
                sp!!.setTime("0")
            }else{
                sp!!.setTime(time)
            }
            if (distance.equals("Select")&&time.equals("Select")){
                Toast.makeText(requireContext(),"Please select time or distance",Toast.LENGTH_SHORT).show()
            }
            else if(distance.equals("Select")&&time.equals("0m 0s")){
                Toast.makeText(requireContext(),"Please select time or distance",Toast.LENGTH_SHORT).show()
            }

            else {
                val bundle = Bundle()
                bundle.putInt("index",0)
                findNavController().navigate(R.id.nav_loading,bundle)
            }
        }
        setDistanceData()
    }

    private fun setDistanceData() {
        val distance = resources.getStringArray(R.array.distance_array)

        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, distance)
        binding.spDistance.adapter = adapter
        binding.spDistance.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                Toast.makeText(requireContext(),
                    " " +
                            "" + distance[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setTimeData() {
        dialog = Dialog(requireActivity())
        dialog.setOnDismissListener {
            binding.ivArrow.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            timeClicked=false
            if (!binding.tvTimeValue.text.toString().equals("0m 0s")){
                binding.tvDistanceValue.setText("Select")
            }
        }
        binding.ivArrow.setOnClickListener {
            if (timeClicked){
                binding.ivArrow.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvSpinner.visibility=View.GONE
                hideTimeDialog()
                timeClicked=false
            }
            else{
                binding.ivArrow.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvSpinner.visibility=View.VISIBLE
                showTimePickerDialog()
                timeClicked=true
            }
        }
    }

    private fun _setDistanceData() {

        binding.ivArrowD.setOnClickListener {
            if (distanceClicked){
                binding.ivArrowD.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvDistance.visibility=View.GONE
                distanceClicked=false
            }
            else{
                binding.ivArrowD.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvDistance.visibility=View.VISIBLE
                distanceClicked=true
            }

        }
        val distance = resources.getStringArray(R.array.distance_array)
        val distanceAdapter=DistanceAdapter(requireContext(),distance,this)
        binding.rvDistance.adapter=distanceAdapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QuickStart.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuickStart().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun clickItem(id: Int, name: String, isTime: Int) {
        if (isTime==0) {
            binding.ivArrow.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvSpinner.visibility=View.GONE
            binding.tvDistanceValue.setText("Select")
            if (name.equals("5min")) {
                binding.tvTimeValue.setText("05:00")
                time = "05"
            }
            else if (name.equals("1min")){
                binding.tvTimeValue.setText("01:00")
                time = "01"
            }
            else{
                time=name.substring(0,2)
                binding.tvTimeValue.setText(name.substring(0,2)+":00")
            }
        }
        else{
            binding.ivArrowD.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvDistance.visibility=View.GONE
            distance=name
            binding.tvDistanceValue.setText(name)
            binding.tvTimeValue.setText("Select")
        }

    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showTimePickerDialog() {
        dialog.setContentView(R.layout.time_picker)
        val minutesPicker: NumberPicker = dialog.findViewById(R.id.minutesPicker)
        val secondsPicker: NumberPicker = dialog.findViewById(R.id.secondsPicker)
        val confirmButton: Button = dialog.findViewById(R.id.confirmButton)
        minutesPicker.textColor = Color.WHITE
        secondsPicker.textColor = Color.WHITE

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59

        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59

        minutesPicker.textSize = resources.getDimension(R.dimen.number_picker_text_size)
        secondsPicker.textSize = resources.getDimension(R.dimen.number_picker_text_size)

        confirmButton.setOnClickListener {
            val minutes = minutesPicker.value
            val seconds = secondsPicker.value
            binding.tvTimeValue.text = "${minutes}m ${seconds}s"
            dialog.dismiss() // Close the dialog
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show() // Show the dialog
        dialog.window?.setLayout(
            resources.getDimensionPixelSize(R.dimen.time_picker_dialog_width),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun hideTimeDialog(){
        dialog.dismiss()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }

    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle(getString(R.string.quick_start),false,true,false)
    }
}