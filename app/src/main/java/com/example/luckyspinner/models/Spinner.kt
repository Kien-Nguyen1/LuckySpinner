package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING

data class Spinner (
    var idSpin : String = EMPTY_STRING,
    var titleSpin : String = EMPTY_STRING,
    var hasSelected : Boolean = true
)