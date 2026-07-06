package com.kayakpro.erg.ui.login.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.WorkoutTopAdapter
import com.kayakpro.erg.databinding.FragmentWorkoutBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.interfaces.OnClickTopWorkout
import com.kayakpro.erg.model.RBody
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.network.Constants.BPM
import com.kayakpro.erg.network.Constants.STROKE
import com.kayakpro.erg.network.Constants.WATTS
import com.kayakpro.erg.viewmodels.MyViewModel
import com.kayakpro.erg.viewmodels.RefreshViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [WorkoutFragmentBackup.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class WorkoutFragmentBackup : Fragment(), OnClickTopWorkout {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var workoutAdapter: WorkoutTopAdapter? = null
    var time = 0
    private lateinit var appBarCallback: AppBarCallback
    private lateinit var binding: FragmentWorkoutBinding
    var handler = Handler(Looper.getMainLooper())

    private val viewModel: MyViewModel by activityViewModels()
    private var bpm = "0"
    private var strokeRate = "0"
    private var watts = "0"
    private var distanceMain = "0"
    var isTimeFromDevice=false
    private var timeMain = "0"
    private var calory=0
    private var caloryServer=0
    private var timemilli=0
    private var isPause = false
    private var remainingTime: Long = 0 // 10 minutes in milliseconds
    private var isRunning: Boolean = false
    private var _timer: CountDownTimer? = null
    private var timer = Timer()
    private var timeReducing = "0"
    private var timeInSeconds = 0
    private var timeRemaining: Long = 0
    private var isFirst = false
    private var totalTimeInSec = 0
    private var isTime = true
    private var elapsedTime: Long = 0

    private var distance = 0f
    private var distanceCoveredSpecific=0
    var dialog:AlertDialog?=null
    val alWorkoutData= ArrayList<CreateTrainingHistoryRequest.Workout>()
    private val alWorkout = ArrayList<RBody.Workout>()
    private val sp = SesssionManager.getInstance()
    private var tiktik=0
    private var pending=0
    private val refreshViewModel: RefreshViewModel by activityViewModels()
    var pos=0
    var kCal = 0
    var pace = "0.0"
    var speed = "0.00"
    var paceServer=0
    var speedSever=0
    var distanceServer =0
    var wattsServer="0"
    var strokeRateServer="0"
    var bpmServer="0"
    private var distancecount = 0
    var distancefromMachine=0
    var differenceFromMachine=0
    var actualDistance=0
    private val EFFICIENCY_FACTOR = 0.005f
    private lateinit var model: RBody
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

            binding = FragmentWorkoutBinding.inflate(inflater)
            if (arguments != null) {
            }
            init()
        }
        return binding.root
    }
    private fun init() {
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        updateAppBarTitle()
        model = arguments?.getParcelable("model")!!
        Log.d("Analysis__", "alWorkout is : ${Gson().toJson(model.workouts)}")
        for (items in model.workouts) {
            alWorkout.add(items)
        }
        pending=alWorkout.size-1

        workoutAdapter = WorkoutTopAdapter(requireContext(), alWorkout, this)
        binding.rvWorkouts.adapter = workoutAdapter

        obserever()

        binding.ivPlayPause.setOnClickListener {
            Log.d("WorkoutFragmentBackup__","clicked on playpause  and isPause $isPause")

            if (isPause) {
                pauseTimer()
                isPause = false
                binding.ivPlayPause.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_play,
                        null
                    )
                )
            }
            else {
                Log.d("WorkoutFragmentBackup__","clicked on playpause  and isFirst $isFirst")
                if (isFirst) {
                    _startTimer()
                } else {
                    startTracking()
                    resumeTimer()
                }
                isPause = true
                isFirst = true
                binding.ivPlayPause.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_pause,
                        null
                    )
                )
            }

        }

        binding.ivStop.setOnClickListener {
            Log.d("WorkoutFragmentBackup__","Clicked on stopnal")
            showEndDialog() }

    }
    private fun resumeTimer() {
        if (!isRunning) {
            isRunning = true

            if (remainingTime > 0) {
                _timer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        tiktik++
                        remainingTime = millisUntilFinished
                        timeInSeconds++
                        val minutes = remainingTime / 60000
                        val seconds = (remainingTime % 60000) / 1000
                        timeReducing = String.format("%02d:%02d", minutes, seconds)
                        if (timeReducing.equals("00:00")){
                        }
                    }

                    override fun onFinish() {
                        isRunning = false
                        cancel()
                        startNextWorkOut()
                    }
                }.start()
            } else {
                _timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        tiktik++
                        timeInSeconds++ // Increment elapsed time by 1 second
                        val minutes = timeInSeconds / 60
                        val seconds = timeInSeconds % 60
                        timeReducing = String.format("%02d:%02d", minutes, seconds)

                        var remianing =
                            (Integer.parseInt(distanceMain) - actualDistance)

                        if (remianing <= 1) {
                            distanceCoveredSpecific = actualDistance+1
                            isRunning = false
                            cancel()
                            startNextWorkOut()
                        }
                    }

                    override fun onFinish() {
                    }
                }.start()
            }
        }
    }

    private fun _startTimer() {
        if (!isRunning) {
            isRunning = true
            // Check if there is remaining time
            if (remainingTime > 0) {
                // Start countdown timer
                _timer = object : CountDownTimer(remainingTime, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        remainingTime = millisUntilFinished
                        tiktik++
                        updateTimerDisplay(remainingTime)
                    }

                    override fun onFinish() {
                        isRunning = false
                        cancel()
                        startNextWorkOut()
                    }
                }.start()
            } else {
                _timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        elapsedTime += 1000 // Increment elapsed time by 1 second
                        tiktik++
                        updateTimerDisplay(elapsedTime)
                        var remianing =
                            (Integer.parseInt(distanceMain) - actualDistance)
                        if (remianing <= 1) {
                            distance = 0f
                            isRunning = false
                            cancel() // Stop the timer when distance reaches zero
                            startNextWorkOut()
                        }
                    }
                    override fun onFinish() {
                        // This won't be called since we use Long.MAX_VALUE
                    }
                }.start()
            }
        }
    }



    private fun startNextWorkOut() {
        timer.purge()

        if (pending!=0) {
            isFirst=false
            saveData()
            isRunning = true
            pending--


        }else{
            saveData()
            showDialog(getString(R.string.session_completed_))
        }
    }

    private fun showRestDialog(message:String){
        if (dialog==null) {
            dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .create()

        }
        val view = layoutInflater.inflate(R.layout.custom_error_dialog,null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        close.visibility=View.GONE
        val btnOk= view.findViewById<AppCompatButton>(R.id.btn_okay)
        btnOk.visibility=View.VISIBLE
        dialog?.setView(view)
        tvMessage.setText(message)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
    }
    private fun showDialog(message:String){
        val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
            .create()

        val view = layoutInflater.inflate(R.layout.custom_error_dialog,null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        close.visibility=View.GONE
        val btnOk= view.findViewById<AppCompatButton>(R.id.btn_okay)
        btnOk.visibility=View.GONE
        builder.setView(view)
        tvMessage.setText(message)
        btnOk.setOnClickListener {
            sendData()
            builder.dismiss()
            navBack()
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()


    }

    private fun navBack() {
        var value = sp!!.isPlayAgain()
        if (value==0) {
            findNavController().popBackStack(R.id.nav_pt, false, false)
        }else if (value == 1){
            findNavController().popBackStack(R.id.nav_pt_without, false, false)
        }
        else if (value ==2){
            findNavController().popBackStack(R.id.nav_home, false, false)
        }
        else{
            findNavController().popBackStack(R.id.nav_history, false, false)
        }
    }

    private fun showEndDialog(){
        val builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.dialog_end_session,null)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnEnd = view.findViewById<Button>(R.id.btn_end)

        builder.setView(view)

        btnEnd.setOnClickListener {
            builder.dismiss()
            _timer!!.cancel()
            sendData()
            navBack()
        }
        btnCancel.setOnClickListener { builder.dismiss() }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }
    private fun calcualteByTime() {
        totalTimeInSec = convertToSeconds(timeMain)

        remainingTime = convertToMilliseconds(timeMain)
        timeRemaining = convertToMilliseconds(timeMain)

    }
    fun convertToMilliseconds(time: String): Long {
        var totalMilliseconds = 0L

        val parts = time.split(" ")

        for (part in parts) {
            when {
                part.endsWith("m") -> {
                    val minutes = part.substringBefore("m").toIntOrNull() ?: 0
                    totalMilliseconds += minutes * 60 * 1000 // Convert minutes to milliseconds
                }
                part.endsWith("s") -> {
                    val seconds = part.substringBefore("s").toIntOrNull() ?: 0
                    totalMilliseconds += seconds * 1000 // Convert seconds to milliseconds
                }
            }
        }

        return totalMilliseconds
    }
    fun convertToSeconds(time: String): Int {
        var totalSeconds = 0
        val parts = time.split(" ")
        for (part in parts) {
            when {
                part.endsWith("m") -> {
                    val minutes = part.substringBefore("m").toIntOrNull() ?: 0
                    totalSeconds += minutes * 60
                }
                part.endsWith("s") -> {
                    val seconds = part.substringBefore("s").toIntOrNull() ?: 0
                    totalSeconds += seconds
                }
            }
        }
        return totalSeconds
    }
    private fun calculateByDistance() {
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WorkoutFragmentBackup.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WorkoutFragmentBackup().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle(
            "",
            false,
            true,
            false
        )
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }
    private fun obserever() {
        refreshViewModel.data.observe(requireActivity()){
            bpm = it.toString()
        }
        refreshViewModel.strok.observe(requireActivity()){
            strokeRate = it.toString()

        }
        refreshViewModel.dist.observe(requireActivity()){
            // distance = it
        }
        refreshViewModel.watt.observe(requireActivity()){
            watts = it.toString()

        }
        refreshViewModel.bp.observe(viewLifecycleOwner) {
            bpm = it.toString()

        }
        viewModel.bpm.observe(viewLifecycleOwner) {
            bpm = it
        }
        viewModel.strokeRate.observe(viewLifecycleOwner) {
            strokeRate = it
        }
        viewModel.isDeviceConnected.observe(viewLifecycleOwner) {
            if (it) {
                appBarCallback.updateConnectStatusImage(Color.parseColor("#CDFB47"))
            } else {
                appBarCallback.updateConnectStatusImage(Color.parseColor("#A5A1A1"))
            }
        }


    }
    fun calculateCalories(distance: Double): Int {
        val met = 3.5 // MET value for walking
        val energyExpenditurePerMeter = met * 1.05 / 60 // kcal/m, simplified estimate
        val energyExpenditure = distance * energyExpenditurePerMeter
        calory=(energyExpenditure).toInt()
        caloryServer=energyExpenditure.roundToInt()
        return calory
    }
    fun calculatePaceWithDistance(distanceCovered: Double, elapsedTime: Double): Double {
        if (elapsedTime <= 0) {
            return Double.MAX_VALUE // Return a large value if no time has elapsed to avoid division by zero
        }
        val paceInSecondsPer500m = (elapsedTime / (distanceCovered / 500.0))

        val paceInMinutesPer500m = paceInSecondsPer500m / 60.0

        return paceInMinutesPer500m
    }

    fun calculatePace(distancePerSecond: Double, totalTime: Double): String {
        return if (totalTime > 0 && distancePerSecond > 0) {
            val paceInSeconds = (totalTime / distancePerSecond) * 500
            paceServer=paceInSeconds.roundToInt()
            val minutes = (paceInSeconds / 60).toInt()
            val seconds = (paceInSeconds % 60).toInt()

            String.format("%d:%02d", minutes, seconds)
        } else {
            String.format("%d:%02d", 0, 0)
        }
    }

    private fun updateTimerDisplay(timeInMillis: Long) {
        val minutes = timeInMillis / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val timeDisplay = String.format("%02d:%02d", minutes, seconds)
        timeReducing = timeDisplay
    }
    private fun startTracking() {

        try {
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {

                    bpm = BPM.toString()
                    bpmServer = bpm
                    watts = WATTS.toString()
                    wattsServer = watts
                    strokeRate = STROKE.toString()
                    strokeRateServer = strokeRate
                    distance = sp!!.getDeviceDistance()!!.toFloat()

                    if(distancecount==0) {
                        distancefromMachine = distance.toInt() //1000
                        distancecount++ //
                    }
                    try {
                        differenceFromMachine =
                            abs(distancefromMachine - distance.toInt()) //==> 1000-1001
                        if (!isTimeFromDevice) {
                            actualDistance =
                                abs(distanceMain.toInt() - differenceFromMachine)
                        }else{
                            actualDistance = differenceFromMachine
                        }
                    }
                    catch (e:Exception){

                    }

                    handler.post {
                        val timeIntervalInMinutes = timeInSeconds / 60

                        var formattedPace = ""

                        speed =
                            calculateSpeed(actualDistance.toDouble(), timeInSeconds.toDouble())


                        kCal = calculateCalories(actualDistance.toDouble())
                        if (isTime) {
                            pace = calculatePace(actualDistance.toDouble(), totalTimeInSec.toDouble())
                        } else {
                            pace = calculatePaceWithDistance(
                                actualDistance.toDouble(),
                                timeInSeconds.toDouble()
                            ).toString()
                        }

                        var timeEp = ""

                        if (isTime) {
                            val timeElapsed =
                                (timeRemaining - remainingTime) / 1000.0 // Convert to seconds
                            timemilli = timeElapsed.toInt()
                            val minutesElapsed = (timeElapsed / 60).toInt()
                            val secondsElapsed = (timeElapsed % 60).toInt()
                            timeEp = String.format("%02d:%02d", minutesElapsed, secondsElapsed)
                        } else {
                            timeEp = timeReducing
                        }

                        if (isRunning) {
                            binding.tvDistanceValue.text = distance.toString()
                            binding.tvSpeed2.text = timeReducing
                            binding.tvSpeedValue.text = speed
                            binding.tvStrokeValue.text = strokeRate
                            binding.tvHeartRate.text = bpm
                            binding.tvCaloriesValue.text = kCal.toString()
                            binding.tvWattsValue.text = watts
                            binding.tvTimeElapsedValue.text = timeEp
                        }
                    }
                }

            }, 0, 1000) // 1000ms = 1 second
        } catch (e: Exception) {
            Log.d("WorkoutFragmentBackup__","Exception is ${e.printStackTrace()}")
            e.printStackTrace()
            timer.cancel()
        }
    }
    fun calculateSpeed(distance: Double, time: Double): String {
        // Check to avoid division by zero
        if (time > 0) {
            val speed= distance / time
            speedSever=speed.roundToInt()
            return String.format("%.2f", speed)
        }

        println("Time must be greater than zero to calculate speed")
        return "0.00"
    }
    private fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            _timer?.cancel()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _timer!!.cancel()
        sp!!.setBPM("0")
        sp.setWatts("0")
        sp.setCalculatedDistance("0.0")
        sp.setSTROKERATE("0.0")
    }
    override fun onPause() {
        super.onPause()
    }
    override fun clickItem(model: RBody.Workout, position: Int) {
        this.pos=position
        //  commented  workoutAdapter?.clicked(pos)
        remainingTime = 0
        if (model.is_time_or_distance) {
            isTime = true
            if (model.value.isNullOrEmpty()){
                timeMain="0"
            }else {
                timeMain = model.value
            }
            calcualteByTime()
        }
        else {
            isTime = false
            calculateByDistance()
            distanceMain = model.value
        }

        binding.ivPlayPause.performClick()
    }
    fun saveData(){
        val value = speed.toDouble()
        val intValue = value.toInt()
        alWorkoutData.add(CreateTrainingHistoryRequest.Workout(distanceServer,
            speedSever,
            (tiktik*1000).toLong(),
            caloryServer.toDouble(),
            wattsServer.toIntOrNull(),
            paceServer.toString(),
            strokeRateServer.toIntOrNull(),
            (timemilli*1000).toLong(),
            bpmServer.toIntOrNull()))
    }
    fun sendData(){
        val createTrainingHistoryRequest = CreateTrainingHistoryRequest(
            "Program",
            model.name ?: getString(R.string.last_program),
            sp?.getSelectedMachine()!!,
            model.id?.toIntOrNull(),
            false,
            0,
            0,
            alWorkoutData
        )
        viewModel.createTrainingHistory(createTrainingHistoryRequest,"Bearer "+sp?.getAToken().toString()!!)
    }
}