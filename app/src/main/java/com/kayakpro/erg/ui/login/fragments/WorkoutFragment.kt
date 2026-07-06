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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.biocube.bioaccess.session.TokenManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.WorkoutTopAdapter
import com.kayakpro.erg.databinding.FragmentWorkoutBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.interfaces.OnClickTopWorkout
import com.kayakpro.erg.model.RBody
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.viewmodels.MyViewModel
import com.kayakpro.erg.viewmodels.RefreshViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt

import com.kayakpro.erg.FitEncoder
import com.kayakpro.erg.ui.login.activity.BleRepository
import com.kayakpro.erg.viewmodels.QuickStartTrainingViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@AndroidEntryPoint
class WorkoutFragment : Fragment(), OnClickTopWorkout {

    private lateinit var binding: FragmentWorkoutBinding
    private var workoutAdapter: WorkoutTopAdapter? = null
    private lateinit var appBarCallback: AppBarCallback
    private var dialog: AlertDialog? = null   // <-- add this
    private val viewModel: MyViewModel by activityViewModels()
    private val refreshViewModel: RefreshViewModel by activityViewModels()
    private val sp = SesssionManager.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var bpm = "0"
    private var strokeRate = "0"
    private var watts = "0"

    // timers & tracking
    private var isRunning = false
    private var isPause = false
    private var isFirst = true
    private var _timer: CountDownTimer? = null
    private var trackerTimer = Timer()
    private var distance = 0f
    var distancefromMachine = 0
    var differenceFromMachine = 0
    var isTimeFromDevice = false
    private var distancee = 0
    private var timeReducing: String = "00:00"
    private var timeInSeconds = 0
    private var elapsedTime: Long = 0
    private var timeRemaining: Long = 0
    private var remainingTime: Long = 0
    private var totalTimeInSec = 0
    private var isTime = true
    private var isRest = 0
    private var distanceMain = "0"
    private var actualDistance = 0
    private var distancecount = 0
    private var pos = 0
    private val alWorkout = ArrayList<RBody.Workout>()
    private val alWorkoutData = ArrayList<CreateTrainingHistoryRequest.Workout>()
    private var kCal = 0
    private var pace = "0.0"
    private var speed = "0.00"
    private var timemilli = 0
    private var tiktik = 0
    private var paceServer = 0
    private var speedServer = "0"
    private var caloriesServer = "0"
    private var distanceServer = 0
    private var timeDistanceServer = 0
    private var totalDistance = 0
    private var wattsServer = "0"
    private var strokeRateServer = "0"
    private var bpmServer = "0"
    private var listDistance = 0
    private var firstDistance = 0
    private var lastConsoleDistance = 0
    private var firstElapsedTime = 0
    private var showDistance = 0
    private var consoleCal = 0
    private lateinit var model: RBody
    private lateinit var timeMain: String
    private var fitEncoder: FitEncoder? = null
    private var fitStartTime: Long = 0L
    private val trainingViewModel: QuickStartTrainingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = requireArguments().getParcelable("model")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!this::binding.isInitialized) {
            binding = FragmentWorkoutBinding.inflate(inflater, container, false)
            init()
        }
        return binding.root
    }
    private fun init() {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        updateAppBarTitle()

        alWorkout.clear()
        alWorkout.addAll(model.workouts)

        workoutAdapter = WorkoutTopAdapter(requireContext(), alWorkout, this)
        binding.rvWorkouts.adapter = workoutAdapter

        observer()

        binding.ivPlayPause.setOnClickListener { togglePlayPause() }
        binding.ivStop.setOnClickListener { showEndDialog() }

        fitStartTime = System.currentTimeMillis()
        fitEncoder = FitEncoder(requireContext())
        fitEncoder?.startSession(fitStartTime)

        if (alWorkout.isNotEmpty()) {
            workoutAdapter?.highlight(0)
            selectAndPrepareStep(0, autoStart = true)
        }
    }
    private fun togglePlayPause() {
        if (isRunning) {
            pauseTimer()
            isPause = false
            binding.ivPlayPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, null))
        } else {
            startCurrentStep()
        }
    }

    private fun startCurrentStep() {
        _timer?.cancel()
        try { trackerTimer.cancel() } catch (_: Exception) {}
        trackerTimer = Timer()

        startTracking()

        isFirst = true
        isPause = true
        isRunning = true
        binding.ivPlayPause.setImageDrawable(resources.getDrawable(R.drawable.ic_pause, null))

        _startTimer()
    }

    private fun prepareStep(model: RBody.Workout) {
        tiktik = 0
        timeInSeconds = 0
        elapsedTime = 0
        distancecount = 0
        distancefromMachine = 0
        actualDistance = 0
        timeRemaining = 0
        showDistance = 0
        if (sp!!.isDeviceTimeSelected()) {
            isTimeFromDevice = true
            distancee = 0
        } else {
            distancee = 255
            isTimeFromDevice = false
        }
        if (model.is_time_or_distance) {
            isTime = true
            timeMain = if (model.value.isNullOrEmpty()) "0" else model.value
            calcualteByTime()
        } else {
            isTime = false
            distanceMain = model.value
            distanceServer = model.value.toInt()
            listDistance = model.value.toInt()
            calculateByDistance()
        }
        isRest = model.is_rest
    }

    private fun selectAndPrepareStep(index: Int, autoStart: Boolean) {
        if (index !in alWorkout.indices) {
            saveData()
            showDialog(getString(R.string.session_completed_))
            return
        }
        pos = index
        val m = alWorkout[pos]
        workoutAdapter?.highlight(pos)
        prepareStep(m)

        if (autoStart)
            startCurrentStep()
        else
        {
            isRunning = false
            isPause = false
            binding.ivPlayPause.setImageDrawable(resources.getDrawable(R.drawable.ic_play, null))
        }
    }

    override fun clickItem(model: RBody.Workout, position: Int) {
        selectAndPrepareStep(position, autoStart = true)
    }

    private fun startNextWorkOut() {
        totalDistance +=
            if(isTime)
                timeDistanceServer
            else
                listDistance
        Log.d("Checkpoint", "${totalDistance}")

        saveData()
        val next = pos + 1
        if (next < alWorkout.size) {
            selectAndPrepareStep(next, autoStart = true)
        } else {
            isRunning = false
            pauseTimer()
            _timer?.cancel()
            trackerTimer.cancel()
            finishFitFile()
            showDialog(getString(R.string.session_completed_))
            return
        }
    }

    private fun _startTimer() {
        if (isTime) {
            _timer = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tiktik++
                    timeInSeconds++
                    remainingTime = millisUntilFinished
                    updateTimerDisplay(millisUntilFinished)
                }
                override fun onFinish() {
                    remainingTime = 0
                    isRunning = false
                    updateTimerDisplay(0)
                    startNextWorkOut()
                }
            }.start()
        } else {
            // distance mode: tick every second and check actualDistance vs target
            _timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tiktik++
                    timeInSeconds++
                    elapsedTime += 1000
                    updateTimerDisplay(elapsedTime)

                }
                override fun onFinish() {
                    Log.d("RowData", "DistFinished")
                    isRunning = false
                    startNextWorkOut()
                }
            }.start()
        }
    }
    private fun startTracking() {
        try {

            var beforeStrokeRate = ""
            lastConsoleDistance = -1
            firstDistance = 0
            firstElapsedTime = BleRepository.elapsedTime.value.toInt()
            timeDistanceServer = 0

            trackerTimer.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        bpm = BleRepository.heartRate.value
                        watts = BleRepository.watt.value
                        strokeRate = BleRepository.strokeRate.value
                        speedServer =
                            if(BleRepository.pace.value.toInt() == 0)
                                "0"
                            else
                                String.format("%.0f", 500.0 / BleRepository.pace.value.toInt())
                        bpmServer = BleRepository.heartRate.value
                        distance = sp!!.getDeviceDistance()!!.toFloat()

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
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }

                        handler.post {
                            speed =
                                calculateSpeed(actualDistance.toDouble(), timeInSeconds.toDouble())

                            kCal = calculateCalories(actualDistance.toDouble())
                            var timeEp = ""
                            if (isTime) {
                                val timeElapsed =
                                    (timeRemaining - remainingTime) / 1000.0 + 1
                                val minutesElapsed = (timeElapsed / 60).toInt()
                                val secondsElapsed = (timeElapsed % 60).toInt()
                                timeEp = String.format("%02d:%02d", minutesElapsed, secondsElapsed)
                                pace = calculatePace(actualDistance.toDouble(), tiktik.toDouble())
                            } else {
                                timeEp = timeReducing
                                pace = calculatePace(actualDistance.toDouble(), tiktik.toDouble())
                            }

                            binding.tvProgressValue.text = alWorkout[pos].name
                            if(alWorkout[pos].is_rest == 1)
                                binding.tvProgressValue.text = alWorkout[pos].name + " (Rest)"
                            else if(alWorkout[pos].is_rest == 2)
                                binding.tvProgressValue.text = alWorkout[pos].name + " (Easy)"
                            else
                                binding.tvProgressValue.text = alWorkout[pos].name
                            binding.tvNextValue.text =
                                if(pos + 1 < alWorkout.size)
                                    alWorkout[pos + 1].name
                                else
                                    ""

                            if (isRunning) {
                                if(!isTime)
                                    binding.tvDistanceValue.text = showDistance.toString()
                                else
                                    binding.tvDistanceValue.text = kotlin.math.abs(firstDistance - BleRepository.distance.value.toInt()).toString()
                                binding.tvSpeed2Value.text = timeReducing
                                if(isTime)
                                    timeDistanceServer = kotlin.math.abs(firstDistance - BleRepository.distance.value.toInt())

                                val paceInt = BleRepository.pace.value.toIntOrNull() ?: 0
                                binding.tvSpeedValue.text =
                                    if (paceInt <= 0) {
                                        "0.00"
                                    } else {
                                        String.format("%.2f", (500.0 / paceInt) * 3.6)
                                    }
                                binding.tvStrokeValue.text = strokeRate
                                binding.tvHeartRate.text = BleRepository.heartRate.value

                                if(beforeStrokeRate != strokeRate)
                                    consoleCal += BleRepository.calorie.value.toInt()
                                caloriesServer = String.format("%.0f", consoleCal / 3600.0)
                                binding.tvCaloriesValue.text = String.format("%.1f", consoleCal / 3600.0)

                                binding.tvWattsValue.text = watts
                                binding.tvTimeElapsedValue.text = timeEp

                                val paceValue: Int = BleRepository.pace.value.toInt();
                                val temp = String.format("%02d:%02d", (paceValue / 60).toInt(), (paceValue % 60).toInt())
                                binding.tvPaceValue.text = temp
                            }
                            if(!isTime)
                            {
                                val currentDistance = BleRepository.distance.value.toIntOrNull() ?: 0
                                if (currentDistance != lastConsoleDistance) {
                                    if (currentDistance != 0 && firstDistance == 0 && lastConsoleDistance > 0) {
                                        firstDistance = currentDistance
                                    }
                                    lastConsoleDistance = currentDistance
                                }
                                val rowedDistance = if (firstDistance == 0) 0 else kotlin.math.max(0, firstDistance - currentDistance)
                                showDistance = listDistance - rowedDistance
                                if(showDistance > listDistance)
                                {
                                    showDistance = listDistance
                                    firstDistance = currentDistance
                                }
                                if(showDistance < 0)
                                {
                                    startNextWorkOut()
                                }
                            }

                            if(isRunning){
                                var distanceMeters =
                                    if(!isTime)
                                        listDistance - showDistance
                                    else
                                        kotlin.math.abs(firstDistance - BleRepository.distance.value.toInt())
                                val paceSeconds = BleRepository.pace.value.toIntOrNull() ?: 0
                                val speed = if (paceSeconds > 0) 500f / paceSeconds else 0f
                                fitEncoder?.addRecord(
                                    timestampMillis = System.currentTimeMillis(),
                                    distanceMeters = distanceMeters.toFloat(),
                                    speedMetersPerSecond = speed,
                                    powerWatts = watts.toIntOrNull() ?: 0,
                                    cadence = strokeRate.toIntOrNull() ?: 0,
                                    heartRate = BleRepository.heartRate.value.toIntOrNull()
                                )
                            }
                            beforeStrokeRate = strokeRate
                        }
                    } catch (e: Exception) {
                    }
                }
            }, 0, 1000)
        } catch (e: Exception) {
        }
    }

    private fun finishFitFile() {
        val endTime = System.currentTimeMillis()
        val elapsedSec = (endTime - fitStartTime) / 1000f

        fitEncoder?.endSession(
            totalDistanceMeters = totalDistance.toFloat(),
            totalElapsedTimeSec = elapsedSec
        )

        val fitFile = fitEncoder?.getFitFile()
        val token = TokenManager.getToken(context)

        if (fitFile == null || token.isNullOrEmpty()) {
            return
        }

        val requestBody = fitFile.asRequestBody("application/octet-stream".toMediaType())
        val multipartFile = MultipartBody.Part.createFormData(
            name = "file",
            filename = fitFile.name,
            body = requestBody
        )
        trainingViewModel.sendFitFile(token, multipartFile)

        fitEncoder = null
    }

    private fun pauseTimer() {
        _timer?.cancel()
        isRunning = false
    }

    private fun updateTimerDisplay(timeInMillis: Long) {
        val minutes = timeInMillis / 60000
        val seconds = (timeInMillis % 60000) / 1000
        timeReducing = String.format("%02d:%02d", minutes, seconds)
    }

    private fun calculateCalories(distance: Double): Int {
        val met = 3.5
        val energyExpenditurePerMeter = met * 1.05 / 60
        val energyExpenditure = distance * energyExpenditurePerMeter
        return energyExpenditure.toInt()
    }

    private fun calculatePace(distancePerSecond: Double, totalTime: Double): String {
        return if (totalTime > 0 && distancePerSecond > 0) {
            val paceInSeconds = (totalTime / distancePerSecond) * 500
            paceServer = paceInSeconds.roundToInt()
            val minutes = (paceInSeconds / 60).toInt()
            val seconds = (paceInSeconds % 60).toInt()
            String.format("%d:%02d", minutes, seconds)
        } else "0:00"
    }
    private fun calculateSpeed(distance: Double, time: Double): String {
        return if (time > 0) String.format("%.2f", distance / time) else "0.00"
    }
    private fun calcualteByTime() {
        totalTimeInSec = convertToSeconds(timeMain)
        remainingTime = convertToMilliseconds(timeMain)
        timeRemaining = convertToMilliseconds(timeMain)
    }
    private fun calculateByDistance() {
        distanceMain = sp!!.getDistance()!!
    }

    private fun convertToMilliseconds(time: String): Long {
        var totalMilliseconds = 0L
        val parts = time.split(" ")
        for (part in parts) {
            when {
                part.endsWith("m") -> totalMilliseconds += (part.substringBefore("m").toIntOrNull() ?: 0) * 60 * 1000
                part.endsWith("s") -> totalMilliseconds += (part.substringBefore("s").toIntOrNull() ?: 0) * 1000
            }
        }
        return totalMilliseconds
    }

    private fun convertToSeconds(time: String): Int {
        var totalSeconds = 0
        val parts = time.split(" ")
        for (part in parts) {
            when {
                part.endsWith("m") -> totalSeconds += (part.substringBefore("m").toIntOrNull() ?: 0) * 60
                part.endsWith("s") -> totalSeconds += (part.substringBefore("s").toIntOrNull() ?: 0)
            }
        }
        return totalSeconds
    }

    private fun showDialog(message: String) {
        if (!isAdded) return

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .create()
        val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        close.visibility = View.GONE
        val btnOk = view.findViewById<AppCompatButton>(R.id.btn_okay)
        btnOk.visibility = View.VISIBLE
        dialog.setView(view)
        tvMessage.text = message
        btnOk.setOnClickListener {
            sendData()
            dialog.dismiss()
            if(isAdded) navBack()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.4f)
    }

    private fun showEndDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create()
        val view = layoutInflater.inflate(R.layout.dialog_end_session, null)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnEnd = view.findViewById<Button>(R.id.btn_end)
        builder.setView(view)
        btnEnd.setOnClickListener {
            builder.dismiss()
            _timer?.cancel()
            try { trackerTimer.cancel() } catch (_: Exception) {}
            sendData()
            navBack()
        }
        btnCancel.setOnClickListener { builder.dismiss() }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun navBack() {
        if(!isAdded){
            return
        }
        val value = sp!!.isPlayAgain()
        val navController = findNavController()
        if(navController.currentDestination?.id == R.id.nav_workout_fragment){
            navController.navigateUp()
        }
    }

    private fun saveData() {
        if(timeDistanceServer != 0)
            distanceServer = timeDistanceServer
        alWorkoutData.add(
            CreateTrainingHistoryRequest.Workout(
                distanceServer,
                speedServer.toIntOrNull(),
                (tiktik * 1000).toLong(),
                caloriesServer.toDoubleOrNull(),
                wattsServer.toIntOrNull(),
                paceServer.toString(),
                strokeRateServer.toIntOrNull(),
                (timemilli * 1000).toLong(),
                bpmServer.toIntOrNull()
            )
        )
    }

    private fun sendData() {
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
        viewModel.createTrainingHistory(
            createTrainingHistoryRequest,
            "Bearer " + sp?.getAToken().toString()!!
        )
    }

    private fun observer() {
        refreshViewModel.data.observe(viewLifecycleOwner) { bpm = it.toString() }
        refreshViewModel.strok.observe(viewLifecycleOwner) { strokeRate = it.toString() }
        refreshViewModel.dist.observe(viewLifecycleOwner) { /* handled by tracking */ }
        refreshViewModel.watt.observe(viewLifecycleOwner) { watts = it.toString() }

        viewModel.bpm.observe(viewLifecycleOwner) { bpm = it }
        viewModel.strokeRate.observe(viewLifecycleOwner) { strokeRate = it }
        viewModel.isDeviceConnected.observe(viewLifecycleOwner) {
            if (it) {
                binding.icBadgeConnected.visibility = View.VISIBLE
                appBarCallback.updateConnectStatusImage(Color.parseColor("#CDFB47"))
            }
            else
            {
                binding.icBadgeConnected.visibility = View.GONE
                appBarCallback.updateConnectStatusImage(Color.parseColor("#A5A1A1"))
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }

    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle("", false, true, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        _timer?.cancel()
        try { trackerTimer.cancel() } catch (_: Exception) {}
        sp!!.setBPM("0")
        sp.setWatts("0")
        sp.setCalculatedDistance("0.0")
        sp.setSTROKERATE("0.0")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _timer?.cancel()
        _timer = null

        try { trackerTimer.cancel() } catch (_: Exception) {}
        handler.removeCallbacksAndMessages(null)

        dialog?.dismiss()
        dialog = null
    }
}
