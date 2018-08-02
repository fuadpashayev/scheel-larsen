package com.scheellarsen

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.color_layout.view.*

class ColorAdapter(var colors:ArrayList<String>): RecyclerView.Adapter<CustomViewHolder>(){
    var colorList = colors

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cell = layoutInflater.inflate(R.layout.color_layout,parent,false)
        return CustomViewHolder(cell)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val color = colors.get(position)
        holder.view.colorArea.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.DARKEN);

    }

}

class CustomViewHolder(val view:View):RecyclerView.ViewHolder(view){

}