package com.kayakpro.erg.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemWorkoutTopBinding
import com.kayakpro.erg.interfaces.OnClickTopWorkout
import com.kayakpro.erg.model.RBody

class WorkoutTopAdapter(
    private val context: Context,
    private val alWorkout: ArrayList<RBody.Workout>,
    private val onClick: OnClickTopWorkout
) : RecyclerView.Adapter<WorkoutTopAdapter.ItemView>() {

    private var selectedPos: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
        val v = ItemWorkoutTopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemView(v)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        val model = alWorkout[position]
        holder.binding.workoutName.text = model.name

        holder.binding.cl.setOnClickListener {
            // update highlight immediately
            val prev = selectedPos
            selectedPos = position
            if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
            notifyItemChanged(selectedPos)

            onClick.clickItem(model, position)
        }

        val isSelected = position == selectedPos
        val colorRes = if (isSelected) com.kayakpro.erg.R.color.golden_fizz else com.kayakpro.erg.R.color.white
        holder.binding.cl.setCardBackgroundColor(context.resources.getColor(colorRes, null))
    }

    fun highlight(position: Int) {
        val prev = selectedPos
        selectedPos = position
        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
        if (selectedPos != RecyclerView.NO_POSITION) notifyItemChanged(selectedPos)
    }

    override fun getItemCount(): Int = alWorkout.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    class ItemView(itemView: ItemWorkoutTopBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
    }
}
