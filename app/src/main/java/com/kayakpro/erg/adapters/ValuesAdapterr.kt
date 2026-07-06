package com.kayakpro.erg.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemSpinnerTimeBinding
import com.kayakpro.erg.interfaces.OnClickQuickItem
import com.kayakpro.erg.model.TrainingModel


class ValuesAdapterr(
    val context: Context,
    val alValues: ArrayList<TrainingModel>,
    val onClickSpinner: OnClickQuickItem
) :
    RecyclerView.Adapter<ValuesAdapterr.ItemView>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemSpinnerTimeBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ItemView, position: Int) {

        if (alValues.size>0) {
            val model = alValues.get(position)

            if (model.shouldShow)
            holder.binding.tvItemTime.setText(model.name)
            holder.itemView.setOnClickListener {
                    onClickSpinner.clickItem(position, model.name!!,false,model) }
        }
    }

    override fun getItemCount(): Int {
      // return itemList.size
        return alValues.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(itemView: ItemSpinnerTimeBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView


    }



}