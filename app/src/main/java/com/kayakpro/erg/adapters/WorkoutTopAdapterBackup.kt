package com.kayakpro.erg.adapters


import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log

import android.view.LayoutInflater

import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemWorkoutTopBinding
import com.kayakpro.erg.interfaces.OnClickTopWorkout
import com.kayakpro.erg.model.RBody


class WorkoutTopAdapterBackup(
    val context: Context,
    val alWorkout: ArrayList<RBody.Workout>,
    val onClick: OnClickTopWorkout
) :
    RecyclerView.Adapter<WorkoutTopAdapterBackup.ItemView>() {
    var pos=0
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemWorkoutTopBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ItemView, position: Int) {
        if (alWorkout.size>0){
            val model = alWorkout[position]
            // Set the item selected listener
            Log.d("WorkoutFragment__","Model is $model")
            holder.binding.cl.setOnClickListener {
                onClick.clickItem(model,position)
            }
            holder.binding.workoutName.text=model.name

            if (position==pos){
                Log.d("WorkoutFragment__","Pos == position is  : $pos $position ")
                holder.binding.cl.performClick()
                holder.binding.cl.setCardBackgroundColor(context.resources.getColor(com.kayakpro.erg.R.color.golden_fizz,null))
            }else{
                holder.binding.cl.setCardBackgroundColor(context.resources.getColor(com.kayakpro.erg.R.color.white,null))

            }
        }
    }
    fun click(position: Int){
        Log.d("WorkoutFragment__","click on Pos is : $position ")
        this.pos=position
        notifyDataSetChanged()
    }

    fun clicked(position: Int){
        Log.d("WorkoutFragment__","clicked Pos is : $position")
        this.pos=position
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

    class ItemView(itemView: ItemWorkoutTopBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView
    }



}