package com.kayakpro.erg.ui.login.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.DistanceAdapter
import com.kayakpro.erg.adapters.HomeProgramAdapter
import com.kayakpro.erg.adapters.PaceTimeAdapter
import com.kayakpro.erg.adapters.TimeAdapter
import com.kayakpro.erg.databinding.FragmentAvatraBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.interfaces.OnClickSpinner
import com.kayakpro.erg.model.ProgramModel
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@AndroidEntryPoint
class AvatarFragment : Fragment(), OnClickSpinner {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var adapter: HomeProgramAdapter?=null
    var timeClicked=false
    var pacetimeClicked=false
    var distanceClicked=false
    var time="0"
    var pace="0"
    var distance="0"
    private lateinit var appBarCallback: AppBarCallback
    lateinit var timeAdapter:TimeAdapter
    lateinit var pacetimeAdapter:PaceTimeAdapter
    private lateinit var binding: FragmentAvatraBinding
    private var alProgram=ArrayList<ProgramModel>()
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
        if (!this::binding.isInitialized) {

            binding = FragmentAvatraBinding.inflate(inflater)
            if (arguments != null) {

            }
            init()
        }
        return binding.root
    }

    fun init(){
        updateAppBarTitle()

        adapter= HomeProgramAdapter(requireContext(),alProgram)
        binding.rvProgramHistory.adapter=adapter
        _setDistanceData()
        setTimeData()
        setPaceData()
        binding.btnStart.setOnClickListener {

            distance=binding.tvDistanceValue.text.toString()
            if (distance.contains("m")){
                distance=distance.replace("m","")
            }
            else if (distance.contains("M")){
                distance= distance.replace("M","")
            }
            time=binding.tvTimeValue.text.toString()
            pace=binding.tvPaceValue.text.toString()
            Log.d("Analysis__","Setting distance $distance $time")
            if (binding.tvDistanceValue.text.toString().equals("Select")) {
                sp!!.setAvatarDistacne("0")
            }else{
                sp!!.setAvatarDistacne(distance)
                sp!!.setListDistance(distance)
            }
            if (binding.tvTimeValue.text.toString().equals("Select")) {
                sp!!.setAvatarTime("0")
            }
            else{
                sp!!.setAvatarTime(time)
            }

            if (distance.equals("Select")&&time.equals("0m 0s")){
                Toast.makeText(requireContext(),"Please select time or distance",Toast.LENGTH_SHORT).show()
            }
            else if (pace.equals("0m 0s") || pace.equals("Select")){
                Toast.makeText(requireContext(),"Please select pace",Toast.LENGTH_SHORT).show()
            }
            else {
                findNavController().navigate(R.id.nav_avatartraining)
            }
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }
    private fun setPaceData() {
        binding.ivPaceArrow.setOnClickListener {
            if (pacetimeClicked){
                binding.ivPaceArrow.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvPace.visibility=View.GONE
                pacetimeClicked=false
            }
            else{
                binding.ivPaceArrow.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvPace.visibility=View.VISIBLE
                pacetimeClicked=true
            }
        }
        val pace = resources.getStringArray(R.array.time_array)

        pacetimeAdapter= PaceTimeAdapter(requireContext(),pace,this)
        binding.rvPace.adapter=pacetimeAdapter
    }

    private fun setTimeData() {
        binding.ivArrow.setOnClickListener {
            if (timeClicked){
                binding.ivArrow.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvSpinner.visibility=View.GONE
                timeClicked=false
            }
            else{
                binding.ivArrow.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvSpinner.visibility=View.VISIBLE
                timeClicked=true
            }

        }
        val time = resources.getStringArray(R.array.time_array)
        timeAdapter=TimeAdapter(requireContext(),time,this)
        binding.rvSpinner.adapter=timeAdapter

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
         * @return A new instance of fragment AvatraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AvatarFragment().apply {
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
            if (name.equals("5min")){
                binding.tvTimeValue.setText("05:00")
                time="05"
            }
            else if (name.equals("1min")){
                binding.tvTimeValue.setText("01:00")
                time="01"
            }
            else{
                time=name
                binding.tvTimeValue.setText(name)
            }
        }
        else if (isTime == 1){
            binding.ivArrowD.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvDistance.visibility=View.GONE
            distance=name
            binding.tvDistanceValue.setText(name)
            binding.tvTimeValue.setText("Select")
        }
        else{
            binding.ivPaceArrow.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            binding.rvPace.visibility=View.GONE
            if (name.equals("5min")){
                binding.tvPaceValue.setText("05:00")
                pace="05"
            }
            else if (name.equals("1min")){
                binding.tvPaceValue.setText("01:00")
                pace="01"
            }
            else{
                pace=name
                binding.tvPaceValue.setText(name)
            }
        }
    }
    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle(getString(R.string.avatar_training),false,true,false)
    }
}