package com.example.luckyspinner.network

import com.example.luckyspinner.util.Constants.Companion.BASE_URL
import com.example.luckyspinner.util.Constants.Companion.TOKEN
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TelegramApiClient {
    companion object {
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL + TOKEN)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val telegramApi by lazy {
            retrofit.create(TelegramApiService::class.java)
        }
    }
}