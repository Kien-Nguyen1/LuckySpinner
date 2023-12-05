package com.example.luckyspinner.models

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message_id")
    val messageId : Int
)