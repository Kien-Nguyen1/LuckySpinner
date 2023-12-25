package com.example.luckyspinner.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

object Function {
    fun removeObservers(listItem : List<MutableLiveData<*>>, owner: LifecycleOwner) {
        listItem.forEach { item ->
            item.removeObservers(owner)
        }

    }

    fun toNull(listItem: List<MutableLiveData<*>>) {
        listItem.forEach { item ->
            item.value = null
        }
    }
}