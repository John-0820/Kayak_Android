package com.kayakpro.erg.ui.login.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.kayakpro.erg.R
import com.kayakpro.erg.adapters.MachinetListAdapter
import com.kayakpro.erg.databinding.FragmentSelectMachineBinding
import com.kayakpro.erg.model.MachineDetails
import dev.bluefalcon.BluetoothPermissionException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SelectMachine.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectMachine : Fragment(),MachinetListAdapter.OnClickItem {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var alMachine=ArrayList<MachineDetails>()
    private lateinit var binding:FragmentSelectMachineBinding
     var adapter:MachinetListAdapter?=null
    private var width = 0
    private var height = 0

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

            binding = FragmentSelectMachineBinding.inflate(inflater)
            if (arguments != null) {
                // The getPrivacyPolicyLink() method will be created automatically.
            }
            init()
        }
        return binding.root
    }

    private fun init() {
        try {
            binding.rvMachines.getViewTreeObserver()
                .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        if (binding.rvMachines.getViewTreeObserver()
                                .isAlive()
                        ) binding.rvMachines.getViewTreeObserver().removeOnPreDrawListener(this)
                         width = binding.rvMachines.getWidth()  / 3
                         height = binding.rvMachines.getHeight()  / 3

                        return true
                    }
                })

            adapter= MachinetListAdapter(requireContext(),alMachine,this)
            binding.rvMachines.layoutManager= GridLayoutManager(requireContext(),3)
            binding.rvMachines.adapter=adapter
            prepareData()
        } catch (exception: BluetoothPermissionException) {
        }
    }

    private fun prepareData() {
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_kayak)!!,resources.getString(R.string.kayak)))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_canoe)!!,resources.getString(R.string.canoe)))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_dragonboat)!!,resources.getString(R.string.dragon_boat)))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_swim)!!,resources.getString(R.string.swim)))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_ski)!!,resources.getString(R.string.ski)))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_row)!!,resources.getString(R.string.row),1))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_sup)!!,resources.getString(R.string.sup),1))
        alMachine.add(MachineDetails(requireContext().getDrawable(R.drawable.ic_bike)!!,resources.getString(R.string.bike),1))
        adapter!!.notifyDataSetChanged()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SelectMachine.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SelectMachine().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun clickItem(id: Int, lastpos: Int) {
        if (lastpos==-1){
            alMachine.get(id).isSelected=true
        }
        else {
            alMachine.get(lastpos).isSelected=false
            alMachine.get(id).isSelected=true
        }
        adapter!!.notifyDataSetChanged()
    }
}