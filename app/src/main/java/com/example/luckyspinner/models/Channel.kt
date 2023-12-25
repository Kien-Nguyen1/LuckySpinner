package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING

data class Channel(
    var idChannel : String = EMPTY_STRING,
    var idTelegramChannel : String = EMPTY_STRING,
    var nameChannel : String = EMPTY_STRING
)