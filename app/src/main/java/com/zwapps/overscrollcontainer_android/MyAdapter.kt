package com.zwapps.overscrollcontainer_android

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(val colors: List<Int>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250)
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            textSize = 20F
        }

        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount() = colors.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as? TextView)?.apply {
            setBackgroundColor(colors[position])
            text = "RecyclerView - $position"
            setOnClickListener {
                Toast.makeText(context, "RecyclerView click - $position", Toast.LENGTH_SHORT).show()
            }
        }
    }

}