package com.kayakpro.erg.ui.login.fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
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
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.animation.doOnEnd
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.biocube.bioaccess.session.SesssionManager
import com.biocube.bioaccess.session.TokenManager
import com.bumptech.glide.Glide
import com.kayakpro.erg.FitEncoder
import com.kayakpro.erg.R
import com.kayakpro.erg.ui.login.activity.BleRepository
import com.kayakpro.erg.bluetooth.Player2ConnectionManager
import com.kayakpro.erg.databinding.FragmentAvatarTrainingBinding
import com.kayakpro.erg.model.requestmodel.CreateTrainingHistoryRequest
import com.kayakpro.erg.viewmodels.AvatarTrainingViewModel
import com.kayakpro.erg.viewmodels.MyViewModel
import com.kayakpro.erg.viewmodels.QuickStartTrainingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import kotlin.math.roundToInt
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlin.math.min
import kotlin.math.max

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AvatarTraining.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AvatarTraining : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var animator: ObjectAnimator
    lateinit var animator1: ObjectAnimator
    private var _binding: FragmentAvatarTrainingBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null
    var distanceServer = "0"
    var paceServer = "0"
    private var bpm = "0"
    private var timemilli=0
    private var strokeRate = "0"
    private var watts = "0"
    private var distanceMain = "0"
    private var timeMain = "0"
    var kCal = 0
    var pace = "0.0"
    var speed = "0.00"
    private var isPause = false
    private var remainingTime: Long = 0
    private var isRunning: Boolean = false
    private var _timer: CountDownTimer? = null
    private var timer = Timer()
    private var timeReducing = "0"
    private var timeInSeconds = 0
    private val sp = SesssionManager.getInstance()!!
    private var timeRemaining: Long = 0
    private var isFirst = false
    private var totalTimeInSec = 0
    var isTimeFromDevice=false
    private var isTime = true
    private var distancee = 0
    var handler = Handler(Looper.getMainLooper())
    private var elapsedTime: Long = 0
    private var distance: Float = 0f
    private var distancecount = 0
    var distancefromMachine=0
    var differenceFromMachine=0
    var actualDistance=0
    private var consoleCal = 0
    private var firstCal = 0
    private var myDistance = 0
    private var botDistance = 0
    private var firstDistance = 0
    private var lastConsoleDistance = 0
    private var firstElapsedTime = 0
    private var listDistance = 0
    private var diffDistance = 0
    private var opponentDistance = 0
    private var hasReceivedOpponentData = false
    private var deadline = 0
    private var botTime = 0
    private var isTrackingActive = false
    private var localFinished = false
    private var opponentFinished = false

    private val viewModel: MyViewModel by activityViewModels()
    private val avatarViewModel: AvatarTrainingViewModel by activityViewModels()
    private var avatarTrainingId: String = ""
    var strokeRateServer = "0"
    var speedServer = "0"
    var caloriesServer = "0"
    var wattsServer = "0"
    var heartRateServer = "0"
    var isTimeOrDistanceServer = false
    var valueServer = "0"
    var paceForBoatServer = "0"

    private var tiktik=0
    private val powerData = mutableListOf<Entry>()
    private var timeCounter = 0
    private val EFFICIENCY_FACTOR = 0.005f

    private var fitEncoder: FitEncoder? = null
    private var fitStartTime: Long = 0L
    private val trainingViewModel: QuickStartTrainingViewModel by viewModels()
    lateinit var builder: AlertDialog

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
    ): View {
        _binding = FragmentAvatarTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        arguments?.getString("avatarTrainingId")?.let { avatarTrainingId = it }
        Log.d("AvatarTraining", "Received avatarTrainingId from bundle: $avatarTrainingId")
        avatarViewModel.createProgResponse.observe(viewLifecycleOwner) { response ->
            response?.body?.id?.let { avatarTrainingId = it }
        }
        exoPlayer = ExoPlayer.Builder(requireContext()).build().also { player ->
            binding.videoView.player = player

            val videoUri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.water_background}")
            val mediaItem = MediaItem.fromUri(videoUri)

            player.setMediaItem(mediaItem)
            player.repeatMode = ExoPlayer.REPEAT_MODE_ALL
            player.prepare()
            player.playWhenReady = true
        }
    }

    override fun onDestroyView() {
        binding.videoView.player = null

        exoPlayer?.release()
        exoPlayer = null

        if (arguments?.getBoolean("isPlayer2Mode", false) == true) {
            Player2ConnectionManager.onDataReceived = null
            Player2ConnectionManager.onGoalReachedReceived = null
        }
        BleRepository.onDataUpdated = null

        _binding = null
        stopTrackingTimer()
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    fun init() {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        resetTrainingSessionState()
        applyTrainingArguments()
        timeMain = sp.getAvatarTime() ?: "0"
        Log.d("Analysis__", timeMain)

        // Check if we're in Player2 mode
        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode", false) ?: false
        
        if (isPlayer2Mode) {
            // Setup Player2 data sync callback
            setupPlayer2DataSync()
        } else {
            // Show bot pace in center (default behavior)
            binding.tvPaceComp.visibility = View.VISIBLE
            binding.tvPaceCompValue.visibility = View.VISIBLE
            binding.ivDistanceP2.visibility = View.VISIBLE
            binding.tvDistanceValueP2.visibility = View.VISIBLE
            binding.tvDistanceUnitP2.visibility = View.VISIBLE
        }

        if (hasActiveTimeGoal()) {
            isTime = true
            calcualteByTime()
        } else {
            isTime = false
            calculateByDistance()
        }
        if (sp.isDeviceTimeSelected()) {
            isTimeFromDevice = true
            distancee = 0
        } else {
            distancee = 255
            isTimeFromDevice = false
        }
        if (isPlayer2Mode) {
            configurePlayer2StatsLayout()
        }
        setupChart()
        setupConsoleDataListener()
        
        // Handle pace value - in Player2 mode, pace is "0", in Bot mode it's "Xm Ys" or "MM:SS" format
        if (!isPlayer2Mode) {
            val paceValue = sp.getAvatarPace() ?: "0"
            binding.tvPaceCompValue.text = when {
                paceValue.contains(" ") -> {
                    val paceparts = paceValue.split(" ")
                    var pmin = paceparts[0].replace("m", "", ignoreCase = true)
                    var psec = paceparts.getOrNull(1)?.replace("s", "", ignoreCase = true) ?: "0"
                    if (pmin.length == 1) pmin = "0$pmin"
                    if (psec.length == 1) psec = "0$psec"
                    "$pmin:$psec"
                }
                paceValue.contains(":") -> paceValue
                paceValue != "0" && paceValue.isNotBlank() -> {
                    val digits = paceValue.filter { it.isDigit() }
                    when (digits.length) {
                        1, 2 -> String.format("%02d:00", digits.toIntOrNull() ?: 0)
                        else -> {
                            val minutes = digits.substring(0, digits.length - 2).toIntOrNull() ?: 0
                            val seconds = digits.substring(digits.length - 2).toIntOrNull() ?: 0
                            String.format("%02d:%02d", minutes, seconds)
                        }
                    }
                }
                else -> "00:00"
            }
        } else {
            binding.tvPaceCompValue.text = "00:00"
        }

        binding.ivStop.setOnClickListener {
            moveIconToTop(true)
            moveCompIconToTop(true)
        }

        binding.ivPlayPause.setOnClickListener {
            if (isPause) {
                pauseTimer()
                exoPlayer?.play()
                isPause = false
                stopAnimation(true)
                binding.ivPlayPause.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_play,
                        null
                    )
                )
            }
            else {
                stopAnimation(false)
                if (!isTrackingActive) {
                    startTracking()
                }
                if (isFirst) {
                    _startTimer()
                } else {
                    resumeTimer()
                }
                isPause = true
                exoPlayer?.pause()
                isFirst = true
                binding.ivPlayPause.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_pause,
                        null
                    )
                )
            }
        }

        binding.ivBack.setOnClickListener {
            _timer!!.cancel()
            stopAnimation(false)
            endPlayer2SessionIfNeeded()
            try {
//                inputStream = requireActivity().assets.open("dots.gif") // Replace with your GIF file name
//                binding.gifImageView.setImageDrawable(GifDrawable(inputStream))
            } catch (e: IOException) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace()
                _timer = null
                findNavController().popBackStack(R.id.nav_home,false,false)
            }

        }
        binding.ivStop.setOnClickListener {
            showEndDialog()
        }
        binding.ivPlayPause.performClick()

        builder = AlertDialog.Builder(requireContext(),R.style.CustomAlertDialog)
            .create()
    }

    private fun stopAnimation(b: Boolean) {
        if (!b){
            Glide.with(this)
                .asGif()
                .load("file:///android_asset/ic_left_kayak.gif")
                .into(binding.ivAvatarSelf)
            Glide.with(this)
                .asGif()
                .load("file:///android_asset/ic_right_kayak.gif")
                .into(binding.ivAvatarComp)
        }
        else{
            binding.ivAvatarComp.setImageResource(R.drawable.ic_avatar_comp)
            binding.ivAvatarSelf.setImageResource(R.drawable.ic_avatar_self)
        }
    }

    private fun stopSelfAvatar() {
        freezeGif(binding.ivAvatarSelf)
        if (::animator.isInitialized) {
            animator.cancel()
        }
    }

    private fun stopOpponentAvatar() {
        freezeGif(binding.ivAvatarComp)
        if (::animator1.isInitialized) {
            animator1.cancel()
        }
    }

    private fun freezeGif(imageView: ImageView) {
        val drawable = imageView.drawable ?: return
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        if (width <= 0 || height <= 0) return
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        imageView.setImageBitmap(bitmap)
    }

    private fun calcualteByTime() {
        totalTimeInSec = convertToSeconds(timeMain)
        remainingTime = convertToMilliseconds(timeMain)
        timeRemaining = convertToMilliseconds(timeMain)
    }
    fun convertToMilliseconds(time: String): Long {
        var totalMilliseconds = 0L

        // Split the input string by spaces
        val parts = time.split(" ")

        for (part in parts) {
            when {
                part.endsWith("m", ignoreCase = true) && !part.endsWith("min", ignoreCase = true) -> {
                    val minutes = part.dropLast(1).toIntOrNull() ?: 0
                    totalMilliseconds += minutes * 60 * 1000
                }
                part.endsWith("min", ignoreCase = true) -> {
                    val minutes = part.dropLast(3).toIntOrNull() ?: 0
                    totalMilliseconds += minutes * 60 * 1000
                }
                part.endsWith("s", ignoreCase = true) -> {
                    val seconds = part.dropLast(1).toIntOrNull() ?: 0
                    totalMilliseconds += seconds * 1000
                }
            }
        }
        if (totalMilliseconds == 0L && time.contains(":")) {
            totalMilliseconds = convertPaceToSeconds(time) * 1000L
        }
        if (totalMilliseconds == 0L && time.isNotBlank()) {
            time.filter { it.isDigit() }.toIntOrNull()?.let { digits ->
                totalMilliseconds = digits * 60L * 1000L
            }
        }
        return totalMilliseconds
    }
    fun convertToSeconds(time: String): Int {
        var totalSeconds = 0
        val parts = time.split(" ")
        for (part in parts) {
            when {
                part.endsWith("m", ignoreCase = true) && !part.endsWith("min", ignoreCase = true) -> {
                    val minutes = part.dropLast(1).toIntOrNull() ?: 0
                    totalSeconds += minutes * 60
                }
                part.endsWith("min", ignoreCase = true) -> {
                    val minutes = part.dropLast(3).toIntOrNull() ?: 0
                    totalSeconds += minutes * 60
                }
                part.endsWith("s", ignoreCase = true) -> {
                    val seconds = part.dropLast(1).toIntOrNull() ?: 0
                    totalSeconds += seconds
                }
            }
        }
        if (totalSeconds == 0 && time.contains(":")) {
            totalSeconds = convertPaceToSeconds(time)
        }
        if (totalSeconds == 0 && time.isNotBlank()) {
            time.filter { it.isDigit() }.toIntOrNull()?.let { digits ->
                totalSeconds = digits * 60
            }
        }
        return totalSeconds
    }
    private fun calculateByDistance() {
        distanceMain = sp.getAvatarDistance() ?: "0"
    }

    private fun applyTrainingArguments() {
        arguments?.getString("avatarTime")?.let { sp.setAvatarTime(it) }
        arguments?.getString("avatarDistance")?.let { sp.setAvatarDistacne(it) }
    }

    private fun resetTrainingSessionState() {
        isPause = false
        isRunning = false
        isFirst = false
        isTrackingActive = false
        timeInSeconds = 0
        tiktik = 0
        timeReducing = "00:00"
        elapsedTime = 0
        remainingTime = 0
        consoleCal = 0
        myDistance = 0
        botDistance = 0
        opponentDistance = 0
        hasReceivedOpponentData = false
        diffDistance = 0
        botTime = 0
        distancecount = 0
        localFinished = false
        opponentFinished = false
        _timer?.cancel()
        _timer = null
        stopTrackingTimer()
    }

    private fun completeSession() {
        isRunning = false
        pauseTimer()
        stopTrackingTimer()
        finishFitFile()
        showDialog(getString(R.string.session_completed_))
    }

    private fun setupConsoleDataListener() {
        BleRepository.onDataUpdated = {
            if (isTrackingActive && isRunning) {
                handler.post { refreshConsoleStats() }
            }
        }
    }

    private fun refreshConsoleStats() {
        val b = _binding ?: return
        if (!isAdded) return

        val paceValue = BleRepository.pace.value.toIntOrNull() ?: 0
        b.tvPaceValue.text = String.format("%02d:%02d", paceValue / 60, paceValue % 60)
        b.tvWattValue.text = BleRepository.watt.value
        b.tvBpmValue.text = BleRepository.heartRate.value

        if (!isTime) {
            updateDistanceGoalFromConsole()
            diffDistance = computePlayer2GapIfNeeded()
            b.tvDistanceValue.text = diffDistance.toString()
            b.tvDeadlineValue.text = deadline.toString()
            b.tvDeadlineUnit.text = "M"
        } else if (arguments?.getBoolean("isPlayer2Mode", false) == true) {
            updateRowedDistanceFromConsole()
            diffDistance = computePlayer2GapIfNeeded()
            b.tvDistanceValue.text = diffDistance.toString()
        }
    }

    private fun computePlayer2GapIfNeeded(): Int {
        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode", false) ?: false
        return if (isPlayer2Mode && hasReceivedOpponentData) {
            myDistance - opponentDistance
        } else if (isPlayer2Mode) {
            0
        } else {
            diffDistance
        }
    }

    private fun updateRowedDistanceFromConsole() {
        val current = BleRepository.distance.value.toIntOrNull() ?: 0
        if (current != lastConsoleDistance) {
            if (current != 0 && firstDistance == 0 && lastConsoleDistance > 0) {
                firstDistance = current
            }
            lastConsoleDistance = current
        }
        myDistance = if (firstDistance == 0) 0 else kotlin.math.max(0, firstDistance - current)
        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode", false) ?: false
        if (isPlayer2Mode && !isTime && listDistance > 0 && myDistance > listDistance) {
            myDistance = listDistance
        }
    }

    private fun parseAvatarDistance(raw: String?): Int {
        if (raw.isNullOrBlank() || raw == "0" || raw == "Select") return 0
        return raw.replace("m", "", ignoreCase = true)
            .replace("M", "")
            .trim()
            .toIntOrNull() ?: 0
    }

    private fun hasActiveTimeGoal(): Boolean {
        val time = timeMain.trim()
        if (time.isBlank() || time == "0" || time.equals("Select", ignoreCase = true)) {
            return false
        }
        return convertToSeconds(time) > 0
    }

    /**
     * Console sends decreasing values (e.g. 1000, 999, 998…) representing forward progress.
     * [firstDistance] is set when the value first changes from the repeated startup value.
     * Rowed = firstDistance − current; remaining goal = listDistance − rowed.
     */
    private fun updateDistanceGoalFromConsole() {
        val current = BleRepository.distance.value.toIntOrNull() ?: 0
        if (current != lastConsoleDistance) {
            if (current != 0 && firstDistance == 0 && lastConsoleDistance > 0) {
                firstDistance = current
            }
            lastConsoleDistance = current
        }
        if (firstDistance != 0) {
            deadline = listDistance - kotlin.math.max(0, firstDistance - current)
            if (deadline > listDistance) deadline = listDistance
            if (deadline < 0) deadline = 0
        }
        updateRowedDistanceFromConsole()
    }

    private fun applyWorkoutBaselineIfAvailable() {
        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode", false) ?: false
        if (isPlayer2Mode && Player2ConnectionManager.hasWorkoutBaseline()) {
            lastConsoleDistance = -1
            firstDistance = 0
            firstElapsedTime = Player2ConnectionManager.workoutFirstElapsedTime
        } else {
            lastConsoleDistance = -1
            firstDistance = 0
            firstElapsedTime = BleRepository.elapsedTime.value.toIntOrNull() ?: 0
        }
    }

    private fun stopTrackingTimer() {
        isTrackingActive = false
        timer.cancel()
        timer = Timer()
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

                    }

                    override fun onFinish() {
                        isRunning = false
                        _timer!!.cancel()
                        showDialog(getString(R.string.session_completed_))
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
                    }

                    override fun onFinish() {
                        // This won't be called since we use Long.MAX_VALUE
                    }
                }.start()
            }
        }
    }

    private fun moveIconToTop(isStop: Boolean) {
        if (isStop) {
            animator.cancel()
        } else {
            animator = ObjectAnimator.ofFloat(binding.ivAvatarSelf, "translationY", 0f, 500f)
            animator.duration = 2000 // Duration in milliseconds
            animator.repeatCount = ValueAnimator.INFINITE
            animator.start()
        }
    }

    private fun moveCompIconToTop(isStop: Boolean) {
        if (isStop) {
            animator1.cancel()
        }
        else {
            animator1 = ObjectAnimator.ofFloat(
                binding.ivAvatarComp,
                "translationY",
                0f,
                -500f
            ) // Move up by 500 pixels
            animator1.duration = 2000 // Duration for moving up

            val moveDown = ObjectAnimator.ofFloat(
                binding.ivAvatarComp,
                "translationY",
                -500f,
                0f
            ) // Move down back to original position
            moveDown.duration = 2000 // Duration for moving down

            // Chain the animations
            animator1.doOnEnd { moveDown.start() } // Start moving down after moving up
            // moveDown.doOnEnd { animator1.start() } // Start moving up after moving down

            // Start the animation
            animator1.start()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AvatarTraining.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AvatarTraining().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun startTracking() {
        if (isTrackingActive) return

        try {
            isTrackingActive = true
            timer.cancel()
            timer = Timer()

            fitStartTime = System.currentTimeMillis()
            fitEncoder = FitEncoder(requireContext())
            fitEncoder?.startSession(fitStartTime)

            firstCal = BleRepository.calorie.value.toIntOrNull() ?: 0
            applyWorkoutBaselineIfAvailable()
            listDistance = parseAvatarDistance(sp.getAvatarDistance())
            if (!isTime && listDistance > 0) {
                deadline = listDistance
            }
            isTimeOrDistanceServer = isTime
            valueServer =
                if(!isTime)
                    sp.getAvatarDistance() ?: "0"
                else
                    sp.getAvatarTime() ?: "0"
            paceForBoatServer = sp.getAvatarPace() ?: "0"
            myDistance = 0
            botDistance = 0
            opponentDistance = 0
            hasReceivedOpponentData = false
            consoleCal = 0
            botTime = 0
            distancecount = 0
            var rankChanged = 0
            wattsServer = BleRepository.watt.value

            timer.schedule(object : TimerTask() {
                override fun run() {
                    bpm = BleRepository.heartRate.value
                    watts = BleRepository.watt.value
                    strokeRate = BleRepository.strokeRate.value
                    distance = sp.getDeviceDistance()?.toFloatOrNull()
                        ?: BleRepository.distance.value.toFloatOrNull()
                        ?: 0f
                    distanceServer =
                        if(!isTime)
                            listDistance.toString()
                        else
                            kotlin.math.abs(
                                firstDistance - (BleRepository.distance.value.toIntOrNull() ?: 0)
                            ).toString()
                    val paceRaw = BleRepository.pace.value.toIntOrNull() ?: 0
                    speedServer =
                        if (paceRaw == 0)
                            "0"
                        else
                            String.format("%.0f", 500.0 / paceRaw)
                    heartRateServer = BleRepository.heartRate.value
                    if(distancecount==0) {
                        distancefromMachine = distance.toInt()
                        distancecount++
                    }
                    try {
                        differenceFromMachine =
                            kotlin.math.abs(distancefromMachine - distance.toInt())
                        if (!isTimeFromDevice) {
                            actualDistance =
                                kotlin.math.abs(parseAvatarDistance(distanceMain) - differenceFromMachine)
                        }else{
                            actualDistance = differenceFromMachine
                        }
                    }
                    catch (e:Exception){
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                    handler.post {
                        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode", false) ?: false
                        val isPlayerBotMode = !isPlayer2Mode

                        speed = calculateSpeed(actualDistance.toDouble(), timeInSeconds.toDouble())
                        kCal = calculateCalories(actualDistance.toDouble())

                        if (isTime) {
                            pace = calculatePace(actualDistance.toDouble(), tiktik.toDouble())
                        } else {
                            pace = calculatePace(actualDistance.toDouble(), tiktik.toDouble())
                        }
                        if (isRunning && isPlayerBotMode) {
                            botTime++
                        }
                        val pace2InSeconds = convertPaceToSeconds(
                            binding.tvPaceCompValue.text.toString().replace("m", "").replace("s", "")
                        )
                        if (isRunning) {
                            updatePowerData()
                        }

                        if (isTime) {
                            val targetSeconds = convertPaceToSeconds(
                                (sp.getAvatarTime() ?: "0").replace("m", "").replace("s", "").replace(" ", ":")
                            )
                            updateRowedDistanceFromConsole()
                            deadline = if (isPlayerBotMode) targetSeconds - botTime else targetSeconds - tiktik
                        } else {
                            if (!localFinished) {
                                updateDistanceGoalFromConsole()
                            }
                        }

                        if (!isTime && listDistance > 0 && deadline <= 0) {
                            if (isPlayer2Mode) {
                                if (!localFinished) {
                                    localFinished = true
                                    stopSelfAvatar()
                                    if (isPlayer2Mode && Player2ConnectionManager.isConnected) {
                                        val paceValue: Int = BleRepository.pace.value.toIntOrNull() ?: 0
                                        val gapToSend = if (hasReceivedOpponentData) {
                                            (myDistance - opponentDistance).toString()
                                        } else {
                                            "0"
                                        }
                                        val finalData = Player2ConnectionManager.TrainingData(
                                            distance = myDistance.toString(),
                                            time = timeReducing,
                                            pace = paceValue.toString(),
                                            strokeRate = BleRepository.strokeRate.value,
                                            calories = caloriesServer,
                                            heartRate = BleRepository.heartRate.value,
                                            watts = BleRepository.watt.value,
                                            remainingGoal = "0",
                                            distanceGap = gapToSend
                                        )
                                        Player2ConnectionManager.sendTrainingData(finalData)
                                    }
                                    Player2ConnectionManager.sendGoalReached()
                                    completeSession()
                                    return@post
                                }
                            } else {
                                completeSession()
                                return@post
                            }
                        }
                        if (isTime && deadline < 0) {
                            isRunning = false
                            pauseTimer()
                            stopTrackingTimer()

                            finishFitFile()
                            showDialog(getString(R.string.session_completed_))

                            return@post
                        }

                        if (isPlayerBotMode) {
                            botDistance = if (pace2InSeconds > 0) {
                                min(
                                    if (botTime <= 5) {
                                        (botTime.toDouble() * botTime.toDouble() * 50.0 / pace2InSeconds.toDouble()).roundToInt()
                                    } else {
                                        (500.0 / pace2InSeconds * (botTime.toDouble() - 2.5)).roundToInt()
                                    },
                                    if (!isTime) listDistance else 999999
                                )
                            } else {
                                0
                            }
                            diffDistance = myDistance - botDistance
                        } else {
                            diffDistance = computePlayer2GapIfNeeded()
                        }

                        if (isRunning) {
                            val paceValue: Int = BleRepository.pace.value.toIntOrNull() ?: 0
                            val temp = String.format("%02d:%02d", (paceValue / 60).toInt(), (paceValue % 60).toInt())

                            binding.tvTimeValue.text = timeReducing
                            binding.tvWattValue.text = BleRepository.watt.value
                            binding.tvBpmValue.text = BleRepository.heartRate.value
                            binding.tvPaceValue.text = temp
                            consoleCal += BleRepository.calorie.value.toIntOrNull() ?: 0
                            caloriesServer = String.format("%.1f", consoleCal / 3600.0)
                            binding.tvCalValue.text = String.format("%.1f", consoleCal / 3600.0)
                            binding.tvDistanceValue.text = diffDistance.toString()
                            if (isPlayerBotMode) {
                                binding.tvDistanceValueP2.text = botDistance.toString()
                            } else if (isPlayer2Mode && hasReceivedOpponentData) {
                                binding.tvDistanceValueP2.text = (opponentDistance - myDistance).toString()
                            }
                            binding.tvDeadlineValue.text = deadline.toString()
                            binding.tvDeadlineUnit.text = if(isTime) "S" else "M"

                            if (isPlayer2Mode && Player2ConnectionManager.isConnected) {
                                val gapToSend = if (hasReceivedOpponentData) {
                                    (myDistance - opponentDistance).toString()
                                } else {
                                    "0"
                                }
                                val myData = Player2ConnectionManager.TrainingData(
                                    distance = myDistance.toString(),
                                    time = timeReducing,
                                    pace = paceValue.toString(),
                                    strokeRate = BleRepository.strokeRate.value,
                                    calories = caloriesServer,
                                    heartRate = BleRepository.heartRate.value,
                                    watts = BleRepository.watt.value,
                                    remainingGoal = deadline.toString(),
                                    distanceGap = gapToSend
                                )
                                Player2ConnectionManager.sendTrainingData(myData)
                            }
                        }

                        if (isPlayerBotMode || isPlayer2Mode) {
                            if(diffDistance <= 0) {
                                if(rankChanged != -1) {
                                    binding.tvDistanceValue.setTextColor(Color.RED)
                                    animateView(binding.ivAvatarSelf, 20f, binding.ivAvatarComp)
                                    animateView(binding.ivAvatarComp, -20f, binding.ivAvatarSelf)
                                }
                                rankChanged = -1
                            } else {
                                if(rankChanged != 1) {
                                    binding.tvDistanceValue.setTextColor(Color.parseColor("#CDFB47"))
                                    animateView(binding.ivAvatarSelf, -20f, binding.ivAvatarComp)
                                    animateView(binding.ivAvatarComp, 20f, binding.ivAvatarSelf)
                                }
                                rankChanged = 1
                            }
                        }

                        if(isRunning) {
                            val paceForFit = (BleRepository.pace.value.toIntOrNull() ?: 0).takeIf { it > 0 } ?: 1
                            fitEncoder?.addRecord(
                                timestampMillis = System.currentTimeMillis(),
                                distanceMeters = myDistance.toFloat(),
                                speedMetersPerSecond = String.format("%.1f", 500.0 / paceForFit).toFloat(),
                                powerWatts = BleRepository.watt.value.toIntOrNull() ?: 0,
                                cadence = BleRepository.strokeRate.value.toIntOrNull() ?: 0,
                                heartRate = BleRepository.heartRate.value.toIntOrNull()
                            )
                        }

                    }
                }
            }, 0, 1000)
        } catch (e: Exception) {
            isTrackingActive = false
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
            name = "file",
            filename = fitFile.name,
            body = requestBody
        )
        trainingViewModel.sendFitFile(token, multipartFile)

        fitEncoder = null
    }
    private fun convertPaceToSeconds(pace: String): Int {
        val parts = pace.split(":")
        if (parts.size < 2) return 0
        val minutes = parts[0].toIntOrNull() ?: 0
        val seconds = parts[1].toIntOrNull() ?: 0
        return minutes * 60 + seconds
    }

    private fun animateView(view: View, deltaY: Float, view1: View) {
        val newTranslationY = view1.translationY + deltaY

        val animation = TranslateAnimation(0f, 0f, 0f, deltaY)
        animation.duration = 1000
        animation.fillAfter = true

        view.startAnimation(animation)
        view.translationY = newTranslationY
    }

    private fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            _timer?.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_timer != null) {
            _timer!!.cancel()
        }
    }
    fun calculateSpeed(distance: Double, time: Double): String {
        // Check to avoid division by zero
        if (time > 0) {
            val speed= distance / time
            return String.format("%.2f", speed)
        }

        println("Time must be greater than zero to calculate speed")
        return "0.00"
    }
    private fun _startTimer() {
        if (!isRunning) {
            isRunning = true

            if (remainingTime > 0) {
                _timer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        tiktik++
                        remainingTime = millisUntilFinished
                        updateTimerDisplay(remainingTime)
                    }

                    override fun onFinish() {
                        isRunning = false
                    }
                }.start()
            } else {
                _timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        tiktik++
                        elapsedTime += 1000
                        updateTimerDisplay(elapsedTime)
                        // Distance goal completion is handled by the tracking timer via [deadline].
                    }
                    override fun onFinish() {
                        // This won't be called since we use Long.MAX_VALUE
                    }
                }.start()
            }
        }
    }

    private fun setupChart() {
        binding.powerChart.setTouchEnabled(true)
        binding.powerChart.setPinchZoom(true)

        binding.powerChart.setDrawGridBackground(false)
        binding.powerChart.axisLeft.setDrawGridLines(false)
        binding.powerChart.xAxis.setDrawGridLines(false)
        binding.powerChart.axisRight.isEnabled = false
    }
    
    private fun configurePlayer2StatsLayout() {
        binding.ivPaceP2.visibility = View.VISIBLE
        binding.tvPaceValueP2.visibility = View.VISIBLE
        binding.tvPaceUnitP2.visibility = View.VISIBLE
        binding.viewPaceP2.visibility = View.VISIBLE
        binding.tvTimeP2.visibility = View.VISIBLE
        binding.tvTimeValueP2.visibility = View.VISIBLE
        binding.ivCalP2.visibility = View.VISIBLE
        binding.tvCalValueP2.visibility = View.VISIBLE
        binding.tvCalUnitP2.visibility = View.VISIBLE
        binding.ivBpmP2.visibility = View.VISIBLE
        binding.tvBpmValueP2.visibility = View.VISIBLE
        binding.tvBpmUnitP2.visibility = View.VISIBLE
        binding.ivWattsP2.visibility = View.VISIBLE
        binding.tvWattValueP2.visibility = View.VISIBLE
        binding.tvWattsUnitP2.visibility = View.VISIBLE
        binding.tvPaceComp.visibility = View.GONE
        binding.tvPaceCompValue.visibility = View.GONE

        binding.ivDistanceP2.visibility = View.VISIBLE
        binding.tvDistanceValueP2.visibility = View.VISIBLE
        binding.tvDistanceUnitP2.visibility = View.VISIBLE

        binding.ivDeadlineP2.visibility = View.VISIBLE
        binding.tvDeadlineValueP2.visibility = View.VISIBLE
        binding.tvDeadlineUnitP2.visibility = View.VISIBLE
        binding.tvDeadlineUnitP2.text = if (isTime) "S" else "M"
    }

    /**
     * Setup Player2 data synchronization
     * Receives opponent's training data and displays it on the right side
     */
    private fun setupPlayer2DataSync() {
        Player2ConnectionManager.onDataReceived = { data ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                try {
                    updateOpponentStatsDisplay(data)
                } catch (e: Exception) {
                    Log.e("Player2Data", "Error updating opponent stats: ${e.message}")
                }
            }
        }
        Player2ConnectionManager.onGoalReachedReceived = {
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                opponentFinished = true
                stopOpponentAvatar()
                if (localFinished) {
                    completeSession()
                }
            }
        }
    }

    /** Right side: opponent stats from Bluetooth. Left gap refreshed when opponent distance updates. */
    private fun updateOpponentStatsDisplay(data: Player2ConnectionManager.TrainingData) {
        opponentDistance = data.distance.toIntOrNull() ?: 0
        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode", false) ?: false
        if (isPlayer2Mode && !isTime && listDistance > 0 && opponentDistance > listDistance) {
            opponentDistance = listDistance
        }
        hasReceivedOpponentData = true

        binding.tvTimeValueP2.text = data.time

        val paceInt = data.pace.toIntOrNull() ?: 0
        binding.tvPaceValueP2.text = if (paceInt > 0) {
            String.format("%02d:%02d", paceInt / 60, paceInt % 60)
        } else {
            "00:00"
        }

        binding.tvCalValueP2.text = data.calories
        binding.tvBpmValueP2.text = data.heartRate
        binding.tvWattValueP2.text = data.watts
        binding.tvDistanceValueP2.text = (opponentDistance - myDistance).toString()
        binding.tvDeadlineValueP2.text = data.remainingGoal
        binding.tvDeadlineUnitP2.text = if (isTime) "S" else "M"

        diffDistance = myDistance - opponentDistance
        binding.tvDistanceValue.text = diffDistance.toString()

        Log.d(
            "Player2Data",
            "Opponent rowed=$opponentDistance myRowed=$myDistance leftGap=$diffDistance rightGap=${data.distanceGap}"
        )
    }
    
    fun calculateCalories(distance: Double): Int {
        val met = 3.5 // MET value for walking
        val energyExpenditurePerMeter = met * 1.05 / 60 // kcal/m, simplified estimate
        val energyExpenditure = distance * energyExpenditurePerMeter
        println("Calculated calories is $energyExpenditure")
        return energyExpenditure.toInt()
    }
    private fun updatePowerData() {
        try {
            val powerValue =
                Integer.parseInt(watts).toFloat()
            powerData.add(Entry(timeCounter.toFloat(), powerValue))

            val dataSet = LineDataSet(powerData, "Watts Data").apply {
                color = getColor(R.color.golden_fizz)
                valueTextColor = Color.WHITE
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
            }

            val lineData = LineData(dataSet)
            binding.powerChart.data = lineData
            binding.powerChart.invalidate() // Refresh the chart
            timeCounter++
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    fun calculatePace(distancePerSecond: Double, totalTime: Double): String {
        return if (totalTime > 0 && distancePerSecond > 0) {
            val paceInSeconds = (totalTime / distancePerSecond) * 500
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

    fun saveData(){
        val alWorkoutData = ArrayList<CreateTrainingHistoryRequest.Workout>()
        alWorkoutData.add(
            CreateTrainingHistoryRequest.Workout(
                distanceServer.toIntOrNull(),
                speedServer.toIntOrNull(),
                (tiktik * 1000).toLong(),
                caloriesServer.toDoubleOrNull(),
                wattsServer.toIntOrNull(),
                paceServer,
                strokeRateServer.toIntOrNull(),
                (timemilli * 1000).toLong(),
                heartRateServer.toIntOrNull()
            )
        )
        Log.d("AvatarTraining", "saveData called with avatarTrainingId=$avatarTrainingId")
        if (avatarTrainingId.isNotEmpty()) {
            submitHistory(avatarTrainingId, alWorkoutData)
        } else {
            avatarViewModel.createProgResponse.observe(viewLifecycleOwner) { response ->
                Log.d("AvatarTraining", "saveData fallback response: id=${response?.body?.id}")
                response?.body?.id?.let { submitHistory(it, alWorkoutData) }
            }
        }
    }

    private fun submitHistory(trainingId: String, alWorkoutData: ArrayList<CreateTrainingHistoryRequest.Workout>) {
        val numericValue = if (isTimeOrDistanceServer) parseTimeStringToSeconds(valueServer) else valueServer.toIntOrNull()
        val numericPace = parseTimeStringToSeconds(paceForBoatServer)
        Log.d("AvatarTraining", "submitHistory: type=Avatar, trainingId=$trainingId, isTimeOrDistance=$isTimeOrDistanceServer, value=$numericValue, paceForBoat=$numericPace")
        val request = CreateTrainingHistoryRequest(
            "Avatar",
            getString(R.string.avatar_training),
            sp.getSelectedMachine()!!,
            trainingId.toIntOrNull(),
            isTimeOrDistanceServer,
            numericValue,
            numericPace,
            alWorkoutData
        )
        viewModel.createTrainingHistory(request,"Bearer "+sp.getAToken().toString())
    }

    private fun parseTimeStringToSeconds(time: String?): Int? {
        if (time.isNullOrBlank() || time == "Select" || time == "0") return 0
        val minuteRegex = Regex("""(\d+)\s*m\s*(\d+)\s*s""")
        val colonRegex = Regex("""(\d+):(\d+)""")
        minuteRegex.matchEntire(time)?.let {
            val minutes = it.groupValues[1].toIntOrNull() ?: 0
            val seconds = it.groupValues[2].toIntOrNull() ?: 0
            return minutes * 60 + seconds
        }
        colonRegex.matchEntire(time)?.let {
            val minutes = it.groupValues[1].toIntOrNull() ?: 0
            val seconds = it.groupValues[2].toIntOrNull() ?: 0
            return minutes * 60 + seconds
        }
        return time.toIntOrNull()
    }

    private fun showDialog(message:String){

        Log.d("FitFile","Confirm!!")

        if (!isAdded || activity == null) return
        isPause = true
        if (builder.isShowing) {
            builder.dismiss()
        }
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.custom_error_dialog, null)
        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
        close.visibility=View.GONE
        val btnOk= view.findViewById<AppCompatButton>(R.id.btn_okay)
        btnOk.visibility=View.VISIBLE
        builder.setView(view)
        tvMessage.setText(message)
        btnOk.setOnClickListener {
            saveData()
            builder.dismiss()
            endPlayer2SessionIfNeeded()
            findNavController().popBackStack(R.id.nav_home,false,false)
        }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }

    private fun endPlayer2SessionIfNeeded() {
        if (arguments?.getBoolean("isPlayer2Mode", false) == true) {
            Player2ConnectionManager.endTrainingSession()
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
            saveData()
            endPlayer2SessionIfNeeded()
            findNavController().popBackStack(R.id.nav_home,false,false)
        }
        btnCancel.setOnClickListener { builder.dismiss() }
        builder.setCanceledOnTouchOutside(false)
        builder.show()
    }
}