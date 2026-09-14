package com.kayakpro.erg.adapters





import android.content.Context

import android.util.Log

import android.view.LayoutInflater

import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import com.kayakpro.erg.R

import com.kayakpro.erg.databinding.ItemProgramNameBinding

import com.kayakpro.erg.helper.HistoryTypeCache
import com.kayakpro.erg.helper.Utils

import com.kayakpro.erg.interfaces.OnClickExport

import com.kayakpro.erg.model.HistoryResponseModel





class HistoryAdapter(

    val context: Context,

    val alProgram : ArrayList<HistoryResponseModel.RBody>,

    val onClickExport: OnClickExport

) :

    RecyclerView.Adapter<HistoryAdapter.ItemView>() {



    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {

        val v = ItemProgramNameBinding.inflate(

            LayoutInflater.from(viewGroup.context),

            viewGroup,

            false

        )

        return ItemView(v)

    }



    override fun onBindViewHolder(holder: ItemView, position: Int) {

        val model = alProgram.get(position)

        Log.d("HistoryAdapter", "item[$position]: id=${model.id}, type=${model.type}, name=${model.name}, training_id=${model.training_id}, distance=${model.distance}, speed=${model.speed}, calories=${model.calories}, heart_rate=${model.heart_rate}, stroke_rate=${model.stroke_rate}, watts=${model.watts}, time=${model.time}, time_elapsed=${model.time_elapsed}, pace_per_distance=${model.pace_per_distance}, created_date=${model.created_date}")

        val cachedType = model.id?.let { HistoryTypeCache.getCachedType(it) }
        val resolvedType = cachedType ?: model.type
        val name = model.name?.trim()?.takeIf { it.isNotEmpty() }

        val displayName = when (resolvedType?.lowercase()?.replace(" ", "")) {
            "quickstart" -> context.getString(R.string.quick_start_training)
            "avatar" -> context.getString(R.string.avatar_training)
            "program" -> name ?: context.getString(R.string.last_program)
            else -> when {
                name?.contains("quick", ignoreCase = true) == true -> context.getString(R.string.quick_start_training)
                name?.contains("avatar", ignoreCase = true) == true -> context.getString(R.string.avatar_training)
                name?.contains("program", ignoreCase = true) == true -> context.getString(R.string.last_program)
                model.training_id.isNullOrEmpty() || model.training_id == "0" -> context.getString(R.string.quick_start_training)
                else -> name ?: context.getString(R.string.avatar_training)
            }
        }

        holder.binding.tvPName.text = displayName

        holder.binding.tvBpm.text = (model.heart_rate ?: "0").plus(" BPM")

        holder.binding.tvCal.text = (model.calories ?: "0").plus(" KCAL")

        holder.binding.tvSpeed.text= String.format("%.1f KPH", (model.speed?.toFloatOrNull() ?: 0f) * 3.6)

        holder.binding.tvDistance.text=String.format("%.0f M", (model.distance?.toFloatOrNull() ?: 0f))

        holder.binding.tvDate.text= model.created_date?.let { Utils.convertDate(it,"yyyy-MM-dd'T'HH:mm:ss.SSSXXX","MMM d, yyyy | HH:mm" )}

        holder.binding.btnStartAgain.setOnClickListener { onClickExport.clickPlayAgain((model.training_id?:model.id).toString()) }

        holder.binding.btnDelete.setOnClickListener {
            val deleteId = model.id
            Log.d("HistoryAdapter", "delete clicked: position=$position, id=$deleteId, name=${model.name}, type=${model.type}")
            if (deleteId.isNullOrEmpty()) {
                Log.w("HistoryAdapter", "Cannot delete: id is null or empty for item at position $position")
            } else {
                onClickExport.clickDelete(deleteId)
            }
        }

    }





    override fun getItemCount(): Int {

        return alProgram.size

    }



    override fun getItemId(position: Int): Long {

        return position.toLong()

    }



    override fun getItemViewType(position: Int): Int {

        return position

    }



    class ItemView(itemView: ItemProgramNameBinding) :

        RecyclerView.ViewHolder(itemView.root) {

        var binding = itemView





    }













}