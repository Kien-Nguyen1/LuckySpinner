package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING

data class Member(
    var idMember : String = EMPTY_STRING,
    var nameMember : String = EMPTY_STRING,
    var hasSelected : Boolean = true
)
