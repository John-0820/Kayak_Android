package com.kayakpro.erg.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemTrainingBinding
import com.kayakpro.erg.interfaces.OnClickDelete
import com.kayakpro.erg.model.RBody

class TrainingAdapter(
    val context: Context,
    val alProgram : ArrayList<RBody>,
    val onClickDelete: OnClickDelete

    ) :
    RecyclerView.Adapter<TrainingAdapter.ItemView>() {
    var totalMinutes = 0
    var totalSeconds = 0
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemTrainingBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        if (alProgram.size>0) {
            val model = alProgram[position]

            holder.binding.tvName.text = model.name
            var distance = 0
            totalMinutes =0
            totalSeconds = 0
            for (item in model.workouts) {
                if (!item.is_time_or_distance) {
                    distance += Integer.parseInt(item.value)
                    holder.binding.tvDistance.text = "${distance}m"
                } else {
                    if (item.value.length>0) {
                        totalTime(item.value)
                        holder.binding.tvTime.text = String.format("%02d : %02d", totalMinutes, totalSeconds)
                    }
                }
            }
            holder.binding.btnStart.setOnClickListener { onClickDelete.clickSava(model) }
            holder.binding.ivDeleteTraining.setOnClickListener { onClickDelete.clickDelete(model) }
            holder.binding.ivEdit.setOnClickListener { onClickDelete.clickEdit(model) }
        }
    }
    fun totalTime(timeStrings: String): String {
            val parts = timeStrings.split(" ")
            for (part in parts) {
                when {
                    part.endsWith("m") -> {
                        val minutes = part.substringBefore("m").toInt()
                        totalMinutes += minutes
                    }
                    part.endsWith("s") -> {
                        val seconds = part.substringBefore("s").toInt()
                        totalSeconds += seconds
                    }
                }
        }

        totalMinutes += totalSeconds / 60
        totalSeconds = totalSeconds % 60

        return "${totalMinutes}m ${totalSeconds}s"
    }

    override fun getItemCount(): Int {
      // return itemList.size
        return alProgram.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(itemView: ItemTrainingBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView


    }

}