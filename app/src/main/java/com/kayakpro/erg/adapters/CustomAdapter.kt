//package com.kayakpro.erg.adapters
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.TextView
//import com.kayakpro.erg.R
//
//class CustomAdapter(private val context: Context, private val items: Array<String>) : ArrayAdapter<String>(context, 0, items) {
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
//        val item = getItem(position)
//        view.findViewById<TextView>(R.id.name).text = item?.name
//        return view
//    }
//
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_dropdown_item, parent, false)
//        val item = getItem(position)
//        view.findViewById<TextView>(R.id.name).text = item?.name
//        view.findViewById<TextView>(R.id.description).text = item?.description
//        return view
//    }
//}