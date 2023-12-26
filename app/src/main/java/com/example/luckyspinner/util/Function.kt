package com.example.luckyspinner.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import java.util.Calendar

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
    fun changeTheNumberOfDay(position : Int) : Int {
        if (position == 6) return Constants.SUNDAY
        return position + 2
    }
    fun changeDayToPosition(day : Int) : Int {
        if (day == Calendar.SUNDAY) return  6
        return day - 2
    }
}