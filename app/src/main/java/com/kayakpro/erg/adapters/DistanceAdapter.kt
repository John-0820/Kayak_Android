package com.kayakpro.erg.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemSpinnerDistanceBinding
import com.kayakpro.erg.interfaces.OnClickSpinner


class DistanceAdapter(
    val context: Context,
    val alDistance: Array<String>,
    val onClickSpinner: OnClickSpinner) :
    RecyclerView.Adapter<DistanceAdapter.ItemView>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemSpinnerDistanceBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ItemView, position: Int) {
        if (alDistance.size>0) {
            val time = alDistance.get(position)

            holder.binding.tvItemTime.setText(time)
            holder.itemView.setOnClickListener {
                    onClickSpinner.clickItem(position,time,1) }
        }
    }


    override fun getItemCount(): Int {
      // return itemList.size
        return alDistance.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(itemView: ItemSpinnerDistanceBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView


    }



}