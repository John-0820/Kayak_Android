package com.kayakpro.erg.ui.login.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

import com.biocube.bioaccess.session.SesssionManager
import com.biocube.bioaccess.session.TokenManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.ValuesAdapter
import com.kayakpro.erg.adapters.ValuesAdapterr
import com.kayakpro.erg.databinding.FragmentQuickStartTrainingBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.interfaces.OnClickQuickItem
import com.kayakpro.erg.model.TrainingModel
import com.kayakpro.erg.viewmodels.MyViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask

import com.kayakpro.erg.FitEncoder
import com.kayakpro.erg.ui.login.activity.BleRepository
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.viewmodels.QuickStartTrainingViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class QuickStartTraining : Fragment(), OnClickQuickItem {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var rvOneClicked = false
    var rvTwoClicked = false
    var time = 0
    var isTimeFromDevice = false
    private lateinit var appBarCallback: AppBarCallback
    private lateinit var binding: FragmentQuickStartTrainingBinding
    private lateinit var adapter: ValuesAdapter
    private lateinit var adapterr: ValuesAdapterr
    private var alValues = ArrayList<TrainingModel>()
    private var alValuesOne = ArrayList<TrainingModel>()
    private var alValuesTwo = ArrayList<TrainingModel>()
    private val viewModel: MyViewModel by activityViewModels()
    private var bpm = "0"
    private var strokeRate = "0"
    private var watts = "0"
    private var speedForFit = "0"
    private var distanceMain = ""
    private var speedMain = ""
    private var timeMain = ""
    private var caloriesMain = ""
    private var paceMain = ""
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
    private val sp = SesssionManager.getInstance()
    private var distancecount = 0
    private var tiktik = 0
    private var handler = Handler(Looper.getMainLooper())
    private var kCal = 0
    private var pace = "0.0"
    private var speed = "0.00"
    private var distancee = 0
    var distancefromMachine = 0
    var differenceFromMachine = 0
    var actualDistance = 0
    private var consoleDistance = "0"
    private var listDistance = 0
    private var firstDistance = 0
    private var lastConsoleDistance = 0
    private var firstElapsedTime = 0
    private var showDistance = 0
    private var instanceSpeed = 0

    private var averageSpeed = 0
    private var consoleCal = 0
    private var firstCal = 0
    private var fitEncoder: FitEncoder? = null
    private var fitStartTime: Long = 0L
    private val trainingViewModel: QuickStartTrainingViewModel by viewModels()
    private var consolePace = "0"
    private val alWorkoutData = ArrayList<CreateTrainingHistoryRequest.Workout>()
    private var distanceServer = "0"
    private var speedServer = "0"
    private var calorieServer = "0"
    private var paceServer = "0"
    private var wattsServer = "0"
    private var strokeRateServer = "0"
    private var heartRateServer = "0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!this::binding.isInitialized) {
            binding = FragmentQuickStartTrainingBinding.inflate(inflater)
            if (arguments != null) {
            }
            init()
        }
        return binding.root
    }

    private fun obserever() {
        viewModel.watts.observe(viewLifecycleOwner) {
            watts = it
            alValues.forEach { model ->
                if (model.name!!.contains("atts")) {
                    model.value = watts
                }
            }
        }
        viewModel.bpm.observe(viewLifecycleOwner) {
            bpm = it
        }
        viewModel.strokeRate.observe(viewLifecycleOwner) {
            strokeRate = it
        }
        viewModel.isDeviceConnected.observe(viewLifecycleOwner) {
            if (it) {
                binding.icBadgeConnected.visibility = View.VISIBLE
                appBarCallback.updateConnectStatusImage(Color.parseColor("#CDFB47"))
            } else {
                binding.icBadgeConnected.visibility = View.GONE
                appBarCallback.updateConnectStatusImage(Color.parseColor("#A5A1A1"))
            }
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

    private fun init() {
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        timeMain = sp!!.getTime()!!

        if (timeMain == "0") {
            isTime = false
            calculateByDistance()
        } else {
            isTime = true
            calcualteByTime()
        }
        if (sp.isDeviceTimeSelected()) {
            isTimeFromDevice = true
            distancee = 0
        } else {
            distancee = 255
            isTimeFromDevice = false
        }
        updateAppBarTitle()
        setupRV()
        obserever()
        resetData()
        listDistance = sp!!.getListDistance()!!.toInt()
        binding.ivStop.setOnClickListener {
            showEndDialog()
        }
        binding.ivPlayPause.setOnClickListener {
            if (isPause) {
                pauseTimer()
                isPause = false
                binding.ivPlayPause.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_play,
                        null
                    )
                )
            } else {
                startTracking()
                if (isFirst) {
                    _startTimer()
                } else {
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

        binding.ivArrow1.setOnClickListener {
            if (rvOneClicked) {
                binding.ivArrow1.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvOne.visibility = View.GONE
                alValuesOne.clear()
                alValuesOne.addAll(alValues)
                adapter.notifyDataSetChanged()
                rvOneClicked = false
            } else {
                binding.ivArrow1.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvOne.visibility = View.VISIBLE
                removeModel(true)
                rvOneClicked = true
            }
        }
        binding.ivArrow2.setOnClickListener {
            if (rvTwoClicked) {
                binding.ivArrow2.animate()
                    .rotation(360f)
                    .setDuration(200)
                    .start()
                binding.rvTwo.visibility = View.GONE
                alValuesTwo.clear()
                alValuesTwo.addAll(alValues)
                adapterr.notifyDataSetChanged()
                rvTwoClicked = false
            } else {
                binding.ivArrow2.animate()
                    .rotation(180f)
                    .setDuration(200)
                    .start()
                binding.rvTwo.visibility = View.VISIBLE
                removeModel(false)
                rvTwoClicked = true
            }
        }
        binding.ivPlayPause.performClick()
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
        distanceMain = sp!!.getDistance()!!
    }
    private fun startTracking() {

        try {
            fitStartTime = System.currentTimeMillis()
            fitEncoder = FitEncoder(requireContext())
            fitEncoder?.startSession(fitStartTime)
            var hasReachedZero = false
            var beforeStrokeRate = ""
            consoleCal = 0
            lastConsoleDistance = -1
            firstDistance = 0
            firstElapsedTime = BleRepository.elapsedTime.value.toInt()
            firstCal = BleRepository.calorie.value.toInt()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    bpm = BleRepository.heartRate.value
                    watts = BleRepository.watt.value
                    speedForFit = String.format("%.1f", 500.0 / BleRepository.pace.value.toInt())
                    strokeRate = BleRepository.strokeRate.value
                    distance = sp?.getDeviceDistance()!!.toFloat()
                    if (distancecount == 0) {
                        distancefromMachine = distance.toInt() //1000
                        distancecount++ //
                    }

                    try {
                        differenceFromMachine =
                            kotlin.math.abs(distancefromMachine - distance.toInt()) //==> 1000-1001
                        if (!isTimeFromDevice) {
                            actualDistance =
                                kotlin.math.abs(distanceMain.toInt() - differenceFromMachine)
                        } else {
                            actualDistance = differenceFromMachine
                        }
                        consoleDistance = BleRepository.distance.value

                        averageSpeed =
                            if(timeInSeconds != 0)
                                consoleDistance.toInt() / timeInSeconds
                            else
                                999999
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                    handler.post {
                        speed =
                            calculateSpeed(actualDistance.toDouble(), timeInSeconds.toDouble())

                        kCal = calculateCalories(actualDistance.toDouble())

                        consolePace = BleRepository.pace.value
                        var timeEp = ""

                        if (isTime) {
                            val timeElapsed =
                                (timeRemaining - remainingTime) / 1000.0 + 1 // Convert to seconds
                            val minutesElapsed = (timeElapsed / 60).toInt()
                            val secondsElapsed = (timeElapsed % 60).toInt()
                            timeEp = String.format("%02d:%02d", minutesElapsed, secondsElapsed)
                            pace = calculatePace(actualDistance.toDouble(), tiktik.toDouble())
                        } else {
                            timeEp = timeReducing
                            pace = calculatePace(actualDistance.toDouble(), tiktik.toDouble())
                        }

                        if(isRunning)
                        {
                            val currentDistance = consoleDistance.toIntOrNull() ?: 0
                            if (currentDistance != lastConsoleDistance) {
                                if (currentDistance != 0 && firstDistance == 0 && lastConsoleDistance > 0) {
                                    firstDistance = currentDistance
                                }
                                lastConsoleDistance = currentDistance
                            }
                            val rowedDistance = if (firstDistance == 0) 0 else kotlin.math.max(0, firstDistance - currentDistance)
                            showDistance = listDistance - rowedDistance
                        }
                        if(!isTime && showDistance <= 0)
                        {
                            if(hasReachedZero){
                                isRunning = false
                                pauseTimer()
                                timer.cancel()
                                finishFitFile()
                                showDialog(getString(R.string.session_completed_))
                                return@post
                            }
                            else
                            {
                                showDistance = 0
                                hasReachedZero = true
                            }
                        }

                        alValues.forEach { model ->
                            if (model.name!!.contains("atts")) {
                                model.value = watts
                            }
                            if (model.name.contains("troke")) {
                                model.value = strokeRate
                            }
                            if (model.name.contains("eart")) {
                                model.value = bpm
                            }
                            if (model.name.contains("alories")) {
                                if (beforeStrokeRate != strokeRate)
                                    consoleCal += BleRepository.calorie.value.toInt()
                                model.value = String.format("%.1f", consoleCal / 3600.0)
                            }
                            if (model.name.contains("istance")) {
                                if(!isTime)
                                    model.value = showDistance.toString()
                                else
                                    model.value = kotlin.math.abs(firstDistance - BleRepository.distance.value.toInt()).toString()
                            }
                            if (model.name.contains("ace")) {
                                val paceValue: Int = consolePace.toInt();
                                val temp = String.format("%02d:%02d", (paceValue / 60).toInt(), (paceValue % 60).toInt())
                                model.value = temp
                            }

                            if (model.name.contains("lapsed")) {
                                model.value = timeEp
                            }
                            if (model.name.equals("Time")) {
                                model.value = timeReducing
                            }
                            if (model.name.contains("peed")) {
                                try {
                                    val paceInt = consolePace.toIntOrNull() ?: 0
                                    model.value =
                                        if (paceInt <= 0) {
                                            "0.00"
                                        } else {
                                            String.format("%.2f", (500.0 / paceInt) * 3.6)
                                        }
                                } catch (e: Exception) {
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                }
                            }
                        }
                        beforeStrokeRate = strokeRate

                        if(isRunning)
                            instanceSpeed = consoleDistance.toInt()
                        if(!isRunning)
                        {
                        } else {
                            resetData()
                        }

                        //fitFile Manage
                        if(isRunning){
                            var distanceMeters =
                                if(!isTime)
                                    showDistance
                                else
                                    kotlin.math.abs(firstDistance - BleRepository.distance.value.toInt())
                            fitEncoder?.addRecord(
                                timestampMillis = System.currentTimeMillis(),
                                distanceMeters = distanceMeters.toFloat(),
                                speedMetersPerSecond = speedForFit.toFloat(),
                                powerWatts = watts.toIntOrNull() ?: 0,
                                cadence = strokeRate.toIntOrNull() ?: 0,
                                heartRate = bpm.toIntOrNull()
                            )
                        }

                        //saveServer
                        distanceServer =
                            if(!isTime)
                                listDistance.toString()
                            else
                                kotlin.math.abs(firstDistance - BleRepository.distance.value.toInt()).toString()

                        val paceInt = consolePace.toIntOrNull() ?: 0
                        speedServer =
                            if (paceInt <= 0)
                                "0"
                            else
                                String.format("%.0f", 500.0 / BleRepository.pace.value.toInt())
                        calorieServer = String.format("%.0f", consoleCal / 3600.0)
                        wattsServer = watts
                        strokeRateServer = strokeRate
                        heartRateServer = BleRepository.heartRate.value
                    }
                }
            }, 0, 1000) // 1000ms = 1 second
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private fun finishFitFile() {
        val endTime = System.currentTimeMillis()
        val elapsedSec = (endTime - fitStartTime) / 1000f

        fitEncoder?.endSession(
            totalDistanceMeters = actualDistance.toFloat(),
            totalElapsedTimeSec = elapsedSec
        )

        val fitFile = fitEncoder?.getFitFile()
        val token = TokenManager.getToken(context)

        if (fitFile == null || token.isNullOrEmpty()) {
            return
        }
        val requestBody = fitFile.asRequestBody("application/octet-stream".toMediaType())
        val multipartFile = MultipartBody.Part.createFormData(
            "file",
            fitFile.name,
            requestBody
        )
        trainingViewModel.sendFitFile(token, multipartFile)

        fitEncoder = null
    }


    private fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            val currentDistance = BleRepository.distance.value.toIntOrNull() ?: 0
            if (currentDistance != lastConsoleDistance) {
                if (currentDistance != 0 && firstDistance == 0 && lastConsoleDistance > 0) {
                    firstDistance = currentDistance
                }
                lastConsoleDistance = currentDistance
            }
            val rowedDistance = if (firstDistance == 0) 0 else kotlin.math.max(0, firstDistance - currentDistance)
            listDistance = listDistance - rowedDistance
            _timer?.cancel()
        }
    }

    fun calculateSpeed(distance: Double, time: Double): String {
        // Check to avoid division by zero
        if (time > 0) {
            val speed = distance / time
            return String.format("%.2f", speed)
        }
        println("Time must be greater than zero to calculate speed")
        return "0.00"
    }

    fun calculateCalories(distance: Double): Int {
        val met = 3.5 // MET value for walking
        val energyExpenditurePerMeter = met * 1.05 / 60 // kcal/m, simplified estimate
        val energyExpenditure = distance * energyExpenditurePerMeter
        println("Calculated calories is $energyExpenditure") // Equivalent to Log.d in Kotlin
        return energyExpenditure.toInt()
    }

    //Himanshu's method to calculate Pace
    fun calculatePace(distancePerSecond: Double, totalTime: Double): String {
        return if (totalTime > 0 && distancePerSecond > 0) {
            val paceInSeconds = (totalTime / distancePerSecond) * 500

            // Convert paceInSeconds into minutes and seconds
            val minutes = (paceInSeconds / 60).toInt()
            val seconds = (paceInSeconds % 60).toInt()
            // Format the result as a string (e.g., "2:30" for 2 minutes 30 seconds)
            String.format("%d:%02d", minutes, seconds)
        } else {
            String.format("%d:%02d", 0, 0)
        }
    }

    private fun removeModel(isFirst: Boolean) {
        for (index in alValues.size - 1 downTo 0) {
            val machineDetails = alValues[index]
            if (isFirst) {
                if (machineDetails.name == binding.tvTitle1.text.toString()) {
                    alValuesOne.removeAt(index)
                    adapter.notifyDataSetChanged()
                    break
                }
            } else {
                if (machineDetails.name!!.contains(binding.tvTitle2.text.toString())) {
                    alValuesTwo.removeAt(index)
                    adapterr.notifyDataSetChanged()
                    break
                }
            }
        }
    }

    private fun setupRV() {
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_distance_, null),
                resources.getString(R.string.distance),
                distanceMain,
                resources.getString(R.string.distance_unit)
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_speed_, null),
                resources.getString(R.string.speed),
                speedMain,
                resources.getString(R.string.speed_unit)
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_stroke_rate, null),
                resources.getString(R.string.stroke_rate),
                strokeRate,
                resources.getString(R.string.stroke_unit)
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_calorie, null),
                resources.getString(R.string.calories),
                caloriesMain,
                resources.getString(R.string.calories_unit)
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_watts, null),
                resources.getString(R.string.watts),
                watts,
                resources.getString(R.string.watts_unit)
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_time, null),
                resources.getString(R.string.time),
                timeMain
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_elapsed, null),
                resources.getString(R.string.time_elapsed),
                "00:00"
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_bpm, null),
                resources.getString(R.string.heart_rate),
                bpm,
                resources.getString(R.string.heart_unit)
            )
        )
        alValues.add(
            TrainingModel(
                resources.getDrawable(R.drawable.ic_pace, null),
                resources.getString(R.string.pace),
                paceMain,
                resources.getString(R.string.pace_unit)
            )
        )

        alValuesOne.addAll(alValues)
        alValuesTwo.addAll(alValues)
        adapter = ValuesAdapter(requireContext(), alValuesOne, this@QuickStartTraining)
        adapterr = ValuesAdapterr(requireContext(), alValuesTwo, this@QuickStartTraining)
        binding.rvOne.adapter = adapter
        binding.rvTwo.adapter = adapterr
        adapter.notifyDataSetChanged()
        adapterr.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }

    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle(
            getString(R.string.quick_start_training),
            false,
            true,
            false
        )
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuickStartTraining().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun clickItem(id: Int, name: String, isFirst: Boolean, model: TrainingModel) {

        if (isFirst) {
            alValues.remove(model)
            alValues.add(0, model)
            binding.rvOne.visibility = View.GONE
            binding.ivArrow1.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            alValuesOne.clear()
            alValuesOne.addAll(alValues)
            adapter.notifyDataSetChanged()
        } else {
            alValues.remove(model)
            alValues.add(1, model)
            binding.rvTwo.visibility = View.GONE
            binding.ivArrow2.animate()
                .rotation(360f)
                .setDuration(200)
                .start()
            alValuesTwo.clear()
            alValuesTwo.addAll(alValues)
            adapterr.notifyDataSetChanged()
        }
        resetData()
    }

    private fun resetData() {
        binding.tvTitle1.text = alValues.get(0).name
        binding.tvTitle2.text = alValues.get(1).name
        binding.tvTitle3.text = alValues.get(2).name
        binding.tvTitle4.text = alValues.get(3).name
        binding.tvTitle5.text = alValues.get(4).name
        binding.tvTitle6.text = alValues.get(5).name
        binding.tvTitle7.text = alValues.get(6).name
        binding.tvTitle8.text = alValues.get(7).name
        binding.tvTitle9.text = alValues.get(8).name

        binding.tvValue1.text = alValues.get(0).value
        binding.tvValue2.text = alValues.get(1).value
        binding.tvValue3.text = alValues.get(2).value
        binding.tvValue4.text = alValues.get(3).value
        binding.tvValue5.text = alValues.get(4).value
        binding.tvValue6.text = alValues.get(5).value
        binding.tvValue7.text = alValues.get(6).value
        binding.tvValue8.text = alValues.get(7).value
        binding.tvValue9.text = alValues.get(8).value

        binding.tvUnit1.text = alValues.get(0).unit
        binding.tvUnit2.text = alValues.get(1).unit
        binding.tvUnit3.text = alValues.get(2).unit
        binding.tvUnit4.text = alValues.get(3).unit
        binding.tvUnit5.text = alValues.get(4).unit
        binding.tvUnit6.text = alValues.get(5).unit
        binding.tvUnit7.text = alValues.get(6).unit
        binding.tvUnit8.text = alValues.get(7).unit
        binding.tvUnit9.text = alValues.get(8).unit
    }
    private fun resumeTimer() {
        if (!isRunning) {
            isRunning = true

            // Check if there is remaining time
            if (remainingTime > 0) {
                // Resume countdown timer
                _timer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        tiktik++
                        remainingTime = millisUntilFinished
                        timeInSeconds++
                        val minutes = remainingTime / 60000
                        val seconds = (remainingTime % 60000) / 1000
                        timeReducing = String.format("%02d:%02d", minutes, seconds)
                    }

                    override fun onFinish() {
                        isRunning = false
                        finishFitFile()
                        showDialog(getString(R.string.session_completed_))
                    }
                }.start()
            } else {
                // Start timer from zero
                _timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        tiktik++
                        timeInSeconds++ // Increment elapsed time by 1 second
                        val minutes = timeInSeconds / 60
                        val seconds = timeInSeconds % 60
                        timeReducing = String.format("%02d:%02d", minutes, seconds)
                    }

                    override fun onFinish() {
                        // This won't be called since we use Long.MAX_VALUE
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
                    }
                }.start()
            } else {
                // Start timer from zero
                _timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        elapsedTime += 1000 // Increment elapsed time by 1 second
                        tiktik++
                        updateTimerDisplay(elapsedTime)
                    }

                    override fun onFinish() {
                        // This won't be called since we use Long.MAX_VALUE
                    }
                }.start()
            }
        }
    }

    private fun updateTimerDisplay(timeInMillis: Long) {
        val minutes = timeInMillis / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val timeDisplay = String.format("%02d:%02d", minutes, seconds)
        timeReducing = timeDisplay
    }

    private fun showDialog(message: String) {
        saveData()
        isPause = true

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .create()

        val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        close.visibility = View.GONE
        val btnOk = view.findViewById<AppCompatButton>(R.id.btn_okay)
        btnOk.visibility = View.VISIBLE

        dialog.setView(view)
        tvMessage.setText(message)
        btnOk.setOnClickListener {
            sendData()
            dialog.dismiss()
            findNavController().popBackStack(R.id.nav_home, false, false)
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.4f)
    }

    private fun showEndDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.dialog_end_session, null)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnEnd = view.findViewById<Button>(R.id.btn_end)

        builder.setView(view)

        btnEnd.setOnClickListener {
            builder.dismiss()
            _timer!!.cancel()
            findNavController().popBackStack(R.id.nav_home, false, false)
        }
        btnCancel.setOnClickListener { builder.dismiss() }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun saveData() {
        Log.d("Checkpoint", "Inside saveData function")
        alWorkoutData.add(
            CreateTrainingHistoryRequest.Workout(
                distanceServer.toIntOrNull(),
                speedServer.toIntOrNull(),
                (tiktik * 1000).toLong(),
                calorieServer.toDoubleOrNull(),
                wattsServer.toIntOrNull(),
                paceServer,
                strokeRateServer.toIntOrNull(),
                (tiktik * 1000).toLong(),
                heartRateServer.toIntOrNull()
            )
        )
    }

    private fun sendData() {
        val createTrainingHistoryRequest = CreateTrainingHistoryRequest(
            "QuickStart",
            getString(R.string.quick_start_training),
            sp?.getSelectedMachine()!!,
            null,
            false,
            null,
            null,
            alWorkoutData
        )
        Log.d("QuickStartTraining", "sendData: type=${createTrainingHistoryRequest.type}, name=${createTrainingHistoryRequest.name}")
        viewModel.createTrainingHistory(
            createTrainingHistoryRequest,
            "Bearer " + sp.getAToken().toString()
        )
    }

}