package com.kayakpro.erg.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ItemMachineBinding
import com.kayakpro.erg.model.MachineDetails


class MachinetListAdapter(
    val context: Context,
    val alMachines : ArrayList<MachineDetails>,
    val onClickItem: OnClickItem) :
    RecyclerView.Adapter<MachinetListAdapter.ItemView>() {
    private var lastSelectedPosition = -1
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemMachineBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {
        if (alMachines.size>0) {
            val model = alMachines.get(position)

            holder.binding.tvMachineName.text = "${model?.name}"
            holder.binding.ivMachine.setImageDrawable(model.id)
            holder.binding.clMachine.setOnClickListener {
                onClickItem.clickItem(position,lastSelectedPosition)
            }
            if (model.isSelected){
                lastSelectedPosition=position
                holder.binding.ivSelectedMachine.visibility=View.VISIBLE
                holder.binding.clMachine.background=context.getDrawable(R.drawable.bg_stroke_white)
                holder.binding.ivSelectedMachine.setImageDrawable(context.getDrawable(R.drawable.ic_selected_machine))

            }else{
                holder.binding.ivSelectedMachine.visibility=View.GONE
                holder.binding.clMachine.background=context.getDrawable(R.drawable.bg_machine)
            }
            if(model.locked==1){
                holder.binding.ivSelectedMachine.visibility=View.VISIBLE
                holder.binding.clMachine.isClickable=false
                holder.binding.ivSelectedMachine.setImageDrawable(context.getDrawable(R.drawable.ic_locked))
            }
        }
    }


    override fun getItemCount(): Int {
        return alMachines.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(itemView: ItemMachineBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding = itemView


    }

    interface OnClickItem {
        fun clickItem(id: Int, lastPos:Int)
    }


}