package com.example.luckyspinner.work

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.luckyspinner.network.TelegramApiClient
import com.example.luckyspinner.util.Constants.Companion.CHAT_ID
import com.example.luckyspinner.util.Constants.Companion.MESSAGE
import com.example.luckyspinner.util.makeStatusNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendMessageWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private lateinit var outputData : Data
    override fun doWork(): Result {
        val chatId = inputData.getString(CHAT_ID)
        val message = inputData.getString(MESSAGE)

        return try {
            CoroutineScope(Dispatchers.IO).launch {
                TelegramApiClient.telegramApi.sendMessage(chatId!!, message!!)
                outputData = workDataOf(CHAT_ID to chatId, MESSAGE to message)
                makeStatusNotification("Send message successfully!", applicationContext)
                return@launch
            }
            return Result.success(outputData)
        } catch (e: Exception) {
            print("${Log.e("TAG", e.message.toString())}")
            return Result.failure()
        }
    }
}