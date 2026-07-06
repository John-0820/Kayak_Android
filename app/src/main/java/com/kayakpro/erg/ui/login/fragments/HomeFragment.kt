package com.kayakpro.erg.ui.login.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.biocube.bioaccess.session.SesssionManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.HistoryAdapter
import com.kayakpro.erg.databinding.FragmentHomeBinding
import com.kayakpro.erg.interfaces.OnClickExport
import com.kayakpro.erg.model.HistoryResponseModel
import com.kayakpro.erg.model.RBody
import com.kayakpro.erg.viewmodels.HistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(), OnClickExport {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HistoryViewModel by viewModels()
    private var alLastProgram = ArrayList<RBody>()
    private var alProgram = ArrayList<HistoryResponseModel.RBody>()
    private var adapter: HistoryAdapter? = null
    private var pDialog: Dialog? = null
    private val sp = SesssionManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        pDialog = Dialog(requireContext())
        pDialog?.setContentView(R.layout.progress_bar_layout)
        pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        pDialog!!.setCancelable(true)
        initUI()
        observeData()
        return binding.root
    }

    private fun initUI() {
        binding.btnProgrammableTraining.setOnClickListener {
            findNavController().navigate(R.id.nav_pt_without)
        }
        binding.btnQuickStart.setOnClickListener {
            findNavController().navigate(R.id.nav_quick_start)
        }
        binding.btnAvatarTraining.setOnClickListener {
            findNavController().navigate(R.id.nav_avatar_without)
        }
        binding.btnLpt.setOnClickListener {
            if (alLastProgram.isNotEmpty()) {
                val bundle = Bundle()
                bundle.putInt("index",1)
                bundle.putParcelable("model", alLastProgram[0])
                findNavController().navigate(R.id.nav_loading,bundle)
            }
        }

        binding.rvProgramHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.btnCheckVideo.setOnClickListener {
            findNavController().navigate(R.id.nav_provideos)
        }
    }

    override fun onResume() {
        super.onResume()
        getHistory()
        getTrainings()
    }

    private fun getHistory() {
        sp?.getSelectedMachine()?.let { machine ->
            sp?.getAToken()?.let { token ->
                viewModel.getHistory(machine, "Bearer $token")
            }
        }
    }

    private fun getTrainings() {
        sp?.getSelectedMachine()?.let { machine ->
            sp?.getAToken()?.let { token ->
                viewModel.getTraining(machine, "Bearer $token")
            }
        }
    }

    private fun observeData() {
        viewModel.historyResponse.observe(viewLifecycleOwner) { response ->
            response?.body?.let { list ->
                alProgram.clear()
                alProgram.addAll(list.take(2)) // Take max 2 items
                adapter = HistoryAdapter(requireContext(), alProgram, this)
                binding.rvProgramHistory.adapter = adapter
            }
        }

        // Last Program / Training Response
        viewModel.trainingResponse.observe(viewLifecycleOwner) { response ->
            response?.body?.let { list ->
                alLastProgram.clear()
                alLastProgram.addAll(list)
            }
        }

        // Training Data Response (for play again)
        viewModel.trainingDataResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                val bundle = Bundle()
                bundle.putInt("index", 1)
                bundle.putParcelable("model", it)
                sp?.setPlayAgain(2)
                findNavController().navigate(R.id.nav_loading, bundle)
            }
        }

        // Download status
        viewModel.isDownloaded.observe(viewLifecycleOwner) {
            if (it == true) showDialog("Saved in download folder.")
        }

        // Error messages
        viewModel.error.observe(viewLifecycleOwner) {
            it?.let { msg -> showDialog(msg) }
        }
    }

    private fun showDialog(message: String) {
        try {
            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create()
            val view = layoutInflater.inflate(R.layout.custom_error_dialog, null)
            val tvMessage = view.findViewById<TextView>(R.id.tv_message)
            val close = view.findViewById<ImageView>(R.id.iv_close_dialog)
            builder.setView(view)
            tvMessage.text = message
            close.setOnClickListener { builder.dismiss() }
            view.findViewById<android.widget.Button>(R.id.btn_okay).setOnClickListener { builder.dismiss() }
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        } catch (_: Exception) {
        }
    }

    override fun clickItem(id: Int) {
        sp?.getAToken()?.let { token ->
            viewModel.getFitFile(id, "Bearer $token")
        }
    }

    override fun clickPlayAgain(id: String) {
        sp?.getSelectedMachine()?.let { machine ->
            sp?.getAToken()?.let { token ->
                viewModel.getTrainingData(id, machine, "Bearer $token")
            }
        }
    }

    override fun clickDelete(id: String) {
        sp?.getAToken()?.let { token ->
            viewModel.deleteTraining(id, "Bearer $token")
        }
    }
}
