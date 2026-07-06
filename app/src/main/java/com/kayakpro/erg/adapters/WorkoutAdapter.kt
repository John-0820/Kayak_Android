package com.kayakpro.erg.adapters


import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemWorkoutBinding
import com.kayakpro.erg.interfaces.OnDeleteItem
import com.kayakpro.erg.model.WorkoutModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WorkoutAdapter(
    val context: Context,
    val alWorkout: ArrayList<WorkoutModel>,
    val onDelete: OnDeleteItem) :
    RecyclerView.Adapter<WorkoutAdapter.ItemView>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemWorkoutBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ItemView, position: Int) {
        if (alWorkout.size>0){
            val model=alWorkout[position]
            holder.binding.tvSerialNum.text="Segment ${position+1}"
            val timeArray = context.resources.getStringArray(com.kayakpro.erg.R.array.time_array)
            var resetTime=false
            var resetDistance=false
            var timeSet=false
            var distanceSet=false

            val adapter = ArrayAdapter(context, com.kayakpro.erg.R.layout.item_spinner_selected, timeArray)
            adapter.setDropDownViewResource(com.kayakpro.erg.R.layout.item_spinner_distance)

            holder.binding.spinnerTime.text=context.resources.getString(com.kayakpro.erg.R.string.select_time)
            holder.binding.spinnerTime.setOnClickListener {
                val dialog = Dialog(context)
                dialog.setContentView(com.kayakpro.erg.R.layout.time_picker)

                val minutesPicker: NumberPicker = dialog.findViewById(com.kayakpro.erg.R.id.minutesPicker)
                val secondsPicker: NumberPicker = dialog.findViewById(com.kayakpro.erg.R.id.secondsPicker)
                val confirmButton: Button = dialog.findViewById(com.kayakpro.erg.R.id.confirmButton)
                minutesPicker.textColor = Color.WHITE
                secondsPicker.textColor = Color.WHITE

                // Set up the NumberPickers programmatically
                minutesPicker.minValue = 0
                minutesPicker.maxValue = 59
                secondsPicker.minValue = 0
                secondsPicker.maxValue = 59

                confirmButton.setOnClickListener {
                    val minutes = minutesPicker.value
                    val seconds = secondsPicker.value
                    timeSet=true

                    holder.binding.spinnerTime.setText("${minutes}m ${seconds}s")
                    if (holder.binding.spinnerTime.text.equals("0m 0s")){
                        holder.binding.spinnerTime.text=context.resources.getString(com.kayakpro.erg.R.string.select_time)
                    }
                    else{
                        holder.binding.spinnerDistance.setSelection(0)
                        model.is_time_or_distance=true
                        model.value=holder.binding.spinnerTime.text.toString()
                    }
                    // holder.binding.etWorkoutTime.setText("${minutes}m ${seconds}s")
                    dialog.dismiss() // Close the dialog
                }
                dialog.show() // Show the dialog
            }

            val distanceArray = context.resources.getStringArray(com.kayakpro.erg.R.array.distance_array)
            val adapterD = ArrayAdapter(context, com.kayakpro.erg.R.layout.item_spinner_selected, distanceArray)
            adapterD.setDropDownViewResource(com.kayakpro.erg.R.layout.item_spinner_distance)
            holder.binding.spinnerDistance.adapter = adapterD

            val adapterR = ArrayAdapter(context, com.kayakpro.erg.R.layout.item_spinner_selected, arrayOf("train", "rest", "easy"))
            adapterR.setDropDownViewResource(com.kayakpro.erg.R.layout.item_spinner_distance)
            holder.binding.spinnerRest.adapter = adapterR

            holder.binding.spinnerRest.setSelection(model.is_rest)

            holder.binding.spinnerDistance.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    resetTime=true
                    if (position!=0){
                        distanceSet=true
                        holder.binding.spinnerTime.text=context.resources.getString(com.kayakpro.erg.R.string.select_time)
                        model.is_time_or_distance=false
                        model.value=selectedItem.replace("m","")
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Do nothing
                }
            }

            holder.binding.spinnerRest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    // Save the selected value (both "train" and "rest" are valid selections)
                    model.is_rest = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            holder.binding.ivSaveWorkout.setOnClickListener {

                if (timeSet==true || distanceSet==true){

                    if (holder.binding.tvWorkoutName.text.toString().trim().length>0) {
                        holder.binding.tvWorkoutName.isEnabled=false
                        holder.binding.tvWorkoutName.isFocusable=false
                        holder.binding.tvWorkoutName.isFocusableInTouchMode=false
                        holder.binding.spinnerTime.isClickable=false
                        holder.binding.spinnerTime.isEnabled=false
                        holder.binding.spinnerTime.isEnabled=false
                        holder.binding.spinnerRest.isEnabled=false
                        holder.binding.spinnerRest.isClickable=false
                        holder.binding.ivSaveWorkout.visibility=View.GONE
                        holder.binding.ivDeleteWorkout1.visibility=View.GONE
                        holder.binding.ivEditWorkout.visibility=View.VISIBLE
                        holder.binding.ivDeleteWorkout2.visibility=View.VISIBLE
                        model.worokoutName = holder.binding.tvWorkoutName.text.toString().trim()
                        // Explicitly save the rest value from spinner
                        val restPosition = holder.binding.spinnerRest.selectedItemPosition
                        if (restPosition >= 0) {
                            model.is_rest = restPosition
                        }
                        onDelete.clickSave(position,model)
                        holder.binding.tvOr.visibility=View.GONE
                        if (timeSet==true){
                            holder.binding.spinnerDistance.visibility=View.GONE
                            holder.binding.etWorkoutDistance.visibility=View.GONE
                            model.is_time_or_distance = true
                        }else{
                            holder.binding.spinnerTime.visibility=View.GONE
                            holder.binding.etWorkoutTime.visibility=View.GONE
                            model.is_time_or_distance = false
                        }
                    }else{
                        Toast.makeText(context,"Please enter name.",Toast.LENGTH_SHORT).show()

                    }

                }
                else{
                    Toast.makeText(context,"Please select either time or distacne first",Toast.LENGTH_SHORT).show()
                    // Toast.makeText(context,"Please select either time first",Toast.LENGTH_SHORT).show()
                }
            }
            holder.binding.ivEditWorkout.setOnClickListener {
                holder.binding.tvWorkoutName.isEnabled=true
                holder.binding.tvWorkoutName.isFocusable=true
                holder.binding.tvWorkoutName.isFocusableInTouchMode=true
                holder.binding.spinnerTime.isClickable=true
                holder.binding.spinnerTime.isEnabled=true
                holder.binding.spinnerTime.isEnabled=true
                holder.binding.spinnerRest.isEnabled=true
                holder.binding.spinnerRest.isClickable=true
                holder.binding.ivSaveWorkout.visibility=View.VISIBLE
                holder.binding.ivDeleteWorkout1.visibility=View.VISIBLE
                holder.binding.ivEditWorkout.visibility=View.GONE
                holder.binding.ivDeleteWorkout2.visibility=View.GONE
                onDelete.clickEdit(position,model)
                holder.binding.spinnerTime.visibility=View.VISIBLE
                holder.binding.etWorkoutTime.visibility=View.VISIBLE
                holder.binding.spinnerDistance.visibility=View.GONE
                holder.binding.etWorkoutDistance.visibility=View.GONE
                holder.binding.tvOr.visibility=View.GONE
            }
            holder.binding.ivDeleteWorkout1.setOnClickListener {
                onDelete.clickItem(position,model)
            }
            holder.binding.ivDeleteWorkout2.setOnClickListener {
                onDelete.clickItem(position, model)
            }
            if (model.isEdit!!){
                holder.binding.tvWorkoutName.setText(model.worokoutName)
                // holder.binding.etWorkoutTime.setText(model.value)

                if (model.is_time_or_distance) {
                    timeSet = true
                    holder.binding.spinnerTime.text = model.value
                }else{

                    val position = distanceArray.indexOf(model.value+"m")
                    if (position >= 0) {
                        holder.binding.spinnerDistance.setSelection(position)
                    }
                }
                // Restore rest spinner selection if model.rest is set
                holder.binding.spinnerRest.setSelection(model.is_rest)
                CoroutineScope(Dispatchers.Main).launch{
                    delay(200)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        // return itemList.size
        return alWorkout.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(itemView: ItemWorkoutBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView


    }



}