package com.example.luckyspinner.models

data class Event(
    val idEvent : String,
    val typeEvent : Int,
    val hour : Int,
    val minute : Int,
    val listDay : List<Int>
)
