package com.example.luckyspinner.network

import com.example.luckyspinner.models.SendMessageResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TelegramApiService {
    @POST("sendMessage")
    @FormUrlEncoded
    suspend fun sendMessage(
        @Field("chat_id")
        chatId : String,
        @Field("text")
        text : String
    ) : Response<SendMessageResponse>
}