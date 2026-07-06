package com.kayakpro.erg.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.databinding.ItemProgramNameBinding
import com.kayakpro.erg.model.ProgramModel


class AvatarProgramAdapter(
    val context: Context,
    val alProgram : ArrayList<ProgramModel>,
    ) :
    RecyclerView.Adapter<AvatarProgramAdapter.ItemView>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemProgramNameBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    override fun onBindViewHolder(holder: ItemView, position: Int) {

    }

    override fun getItemCount(): Int {
      // return itemList.size
        return 2
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