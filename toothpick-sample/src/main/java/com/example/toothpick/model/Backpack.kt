package com.example.toothpick.model

import toothpick.InjectConstructor

@InjectConstructor
class Backpack {

    private val items = mutableListOf<BackpackItem>()

    fun addItem(title: String) {
        items.add(BackpackItem(title))
    }

    fun getItem(position: Int) = items[position]

    fun getItemCount() = items.size
}
