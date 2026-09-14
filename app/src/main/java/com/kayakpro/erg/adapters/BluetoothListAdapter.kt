package com.kayakpro.erg.adapters


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.R
import com.kayakpro.erg.databinding.ItemBluetoothBinding
import com.kayakpro.erg.interfaces.OnClickBluetooth
import com.kayakpro.erg.model.BtRow


class BluetoothListAdapter(
    private val items: MutableList<BtRow>,
    private val onClickBluetooth: OnClickBluetooth) :
    RecyclerView.Adapter<BluetoothListAdapter.ItemView>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemView {
        val v = ItemBluetoothBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        return ItemView(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ItemView, position: Int) {
        val row = items[position]
        holder.binding.tvBluetoothName.text = row.device.name ?: "Unknown device"
        if(row.isConnected) {
            holder.binding.tvBluetoothStatusDot.setImageResource(R.drawable.ic_dot_connected)
            holder.binding.tvBluetoothStatusButton.setImageResource(R.drawable.ic_connected)
        }
        else {
            holder.binding.tvBluetoothStatusDot.setImageResource(R.drawable.ic_dot_empty)
            holder.binding.tvBluetoothStatusButton.setImageResource(R.drawable.ic_empty_slot)
        }

        holder.itemView.setOnClickListener {
            onClickBluetooth.clickItem(position, row.device)
        }
    }


    override fun getItemCount(): Int {
        return items.size
    }

    fun setConnectedDevice(address: String?) {
        items.forEach {
            it.isConnected = it.device.address == address
        }
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ItemView(val binding: ItemBluetoothBinding) :
        RecyclerView.ViewHolder(binding.root)

}