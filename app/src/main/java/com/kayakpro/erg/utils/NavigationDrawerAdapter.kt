package com.kayakpro.erg.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kayakpro.erg.R

class NavigationDrawerAdapter(private val menuItems: List<MenuItem>) :
    RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.navigation_drawer_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.iconImageView.setImageResource(menuItem.icon)
        holder.titleTextView.text = menuItem.title
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.icon_image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
    }

    data class MenuItem(val icon: Int, val title: String)
}