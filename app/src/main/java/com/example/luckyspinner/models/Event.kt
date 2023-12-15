package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants

data class Event (
    val idEvent : String = Constants.EMPTY_STRING,
    val typeEvent : Int? = null,
    val hour : Int? = null,
    val minute : Int? = null,
    val listDay : List<Int> = ArrayList()
)
