package com.example.toothpick.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.toothpick.R
import com.example.toothpick.model.Backpack

import toothpick.InjectConstructor

@InjectConstructor
class BackpackAdapter(private val backpack: Backpack): IBackpackAdapter() {

    class ItemHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.backpack_item, parent, false) as TextView
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.textView.text = backpack.getItem(position).name
    }

    override fun getItemCount() = backpack.getItemCount()
}
