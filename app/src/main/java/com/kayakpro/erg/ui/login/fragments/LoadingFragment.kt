package com.kayakpro.erg.ui.login.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.kayakpro.erg.R
import com.kayakpro.erg.bluetooth.Player2ConnectionManager
import com.kayakpro.erg.databinding.FragmentLoadingBinding
import com.kayakpro.erg.interfaces.AppBarCallback
import com.kayakpro.erg.model.RBody

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoadingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoadingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentLoadingBinding
    private lateinit var appBarCallback: AppBarCallback
    private var countdownTimer: CountDownTimer? = null
    private val syncHandler = Handler(Looper.getMainLooper())
    private var syncRunnable: Runnable? = null
    private var index=0
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

            binding = FragmentLoadingBinding.inflate(inflater)
            if (arguments != null) {
                // The getPrivacyPolicyLink() method will be created automatically.

            }

            init()
        }
        return binding.root    }

    private fun init() {
        index = arguments?.getInt("index") ?: return
        val isPlayer2Mode = arguments?.getBoolean("isPlayer2Mode") == true
        val trainingStartAt = arguments?.getLong("trainingStartAt") ?: 0L

        if (isPlayer2Mode && trainingStartAt > 0L) {
            startSyncedPlayer2Countdown(trainingStartAt)
        } else {
            startDefaultCountdown()
        }
    }

    private fun startDefaultCountdown() {
        countdownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isAdded) return

                val secondsRemaining = millisUntilFinished / 1000
                binding.tvCounter.text = secondsRemaining.toString()
                binding.progressBar.progress = (20 - secondsRemaining).toInt() * 5

                val progress = binding.progressBar.progress.toFloat() / 100
                val iconX = (progress * (binding.progressBar.width - binding.iconImageView.width)).toInt()
                binding.iconImageView.translationX = iconX.toFloat()
            }

            override fun onFinish() {
                navigateToTraining()
            }
        }
        countdownTimer?.start()
    }

    /** Both phones count down to the same absolute timestamp shared by the initiator. */
    private fun startSyncedPlayer2Countdown(trainingStartAt: Long) {
        val totalMs = 20000L
        syncRunnable = object : Runnable {
            override fun run() {
                if (!isAdded) return

                val remaining = trainingStartAt - System.currentTimeMillis()
                if (remaining <= 0L) {
                    binding.tvCounter.text = "0"
                    binding.progressBar.progress = 100
                    binding.iconImageView.translationX = binding.progressBar.width.toFloat()
                    navigateToTraining()
                    return
                }

                val secondsRemaining = ((remaining + 999) / 1000).toInt()
                binding.tvCounter.text = secondsRemaining.toString()
                val elapsed = totalMs - remaining
                binding.progressBar.progress = ((elapsed * 100) / totalMs).toInt().coerceIn(0, 100)

                val progress = binding.progressBar.progress.toFloat() / 100
                val iconX = (progress * (binding.progressBar.width - binding.iconImageView.width)).toInt()
                binding.iconImageView.translationX = iconX.toFloat()

                syncHandler.postDelayed(this, 200L)
            }
        }
        syncHandler.post(syncRunnable!!)
    }

    private fun navigateToTraining() {
        if (!isAdded || !isResumed) return

        val navController = findNavController()
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_loading, true)
            .build()
        if (index == 0) {
            navController.navigate(R.id.nav_quick_start_training, null, navOptions)
        } else if (index == 1) {
            val wModel: RBody? = arguments?.getParcelable<RBody>("model") ?: return
            navController.navigate(
                R.id.nav_workout_fragment,
                Bundle().apply { putParcelable("model", wModel) },
                navOptions
            )
        } else {
            if (arguments?.getBoolean("isPlayer2Mode") == true) {
                Player2ConnectionManager.captureWorkoutBaseline()
            }
            val bundle = Bundle()
            arguments?.getBoolean("isPlayer2Mode")?.let { bundle.putBoolean("isPlayer2Mode", it) }
            arguments?.getString("player2Device")?.let { bundle.putString("player2Device", it) }
            arguments?.getString("avatarTime")?.let { bundle.putString("avatarTime", it) }
            arguments?.getString("avatarDistance")?.let { bundle.putString("avatarDistance", it) }
            arguments?.getString("avatarPace")?.let { bundle.putString("avatarPace", it) }
            arguments?.getString("avatarTrainingId")?.let { bundle.putString("avatarTrainingId", it) }
            arguments?.getLong("trainingStartAt")?.takeIf { it > 0L }?.let {
                bundle.putLong("trainingStartAt", it)
            }
            Log.d("LoadingFragment", "Forwarding to AvatarTraining: avatarTrainingId=${bundle.getString("avatarTrainingId")}")
            navController.navigate(R.id.nav_avatartraining, bundle, navOptions)
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoadingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoadingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appBarCallback = context as AppBarCallback
    }

    fun updateAppBarTitle() {
        appBarCallback.updateAppBarTitle(getString(R.string.quick_start),false,false,false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownTimer?.cancel()
        countdownTimer = null
        syncRunnable?.let { syncHandler.removeCallbacks(it) }
        syncRunnable = null
    }
}