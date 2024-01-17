package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants

data class Event(
    val idEvent : String = Constants.EMPTY_STRING,
    val typeEvent : Int? = null,
    var hour : Int? = null,
    var minute : Int? = null,
    var listDay : MutableList<Int> = arrayListOf(0,0,0,0,0,0,0),
    var nameEvent : String = Constants.EMPTY_STRING,
    var isTurnOn : Boolean = true
)
