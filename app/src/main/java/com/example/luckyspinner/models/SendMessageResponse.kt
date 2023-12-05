package com.example.luckyspinner.models

import com.google.gson.annotations.SerializedName

data class SendMessageResponse(
    @SerializedName("status")
    val status : Boolean,
    @SerializedName("result")
    val result : Message
)
