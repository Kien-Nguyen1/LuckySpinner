package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING

data class Member(
    val idMember : String = EMPTY_STRING,
    val nameMember : String = EMPTY_STRING,
    val isSelect : Boolean = true
)
