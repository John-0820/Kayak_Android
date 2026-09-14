package com.kayakpro.erg.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemSpinnerValuesBinding
import com.kayakpro.erg.interfaces.OnClickSpinner


class PaceTimeAdapter(
    val context: Context,
    val alTime: Array<String>,
    val onClickSpinner: OnClickSpinner) :
    RecyclerView.Adapter<PaceTimeAdapter.ItemView>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemSpinnerValuesBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ItemView, position: Int) {
        if (alTime.size>0) {
            val time = alTime.get(position)

            holder.binding.tvItemTime.setText(time)
            holder.itemView.setOnClickListener {
                    onClickSpinner.clickItem(position,time,2) }
        }
    }


    override fun getItemCount(): Int {
        return alTime.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(itemView: ItemSpinnerValuesBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView


    }



}