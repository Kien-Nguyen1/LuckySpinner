package com.example.luckyspinner.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.luckyspinner.network.TelegramApiClient
import com.example.luckyspinner.util.Constants.CHAT_ID
import com.example.luckyspinner.util.Constants.MESSAGE
import com.example.luckyspinner.util.makeStatusNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendMessageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private lateinit var outputData : Data
    override suspend fun doWork(): Result {
        val chatId = inputData.getString(CHAT_ID)
        val message = inputData.getString(MESSAGE)

        return try {
            withContext(Dispatchers.IO) {
                TelegramApiClient.telegramApi.sendMessage(chatId!!, message!!)
                outputData = workDataOf(CHAT_ID to chatId, MESSAGE to message)
                makeStatusNotification("Send message successfully!", applicationContext)
                return@withContext Result.success(outputData)
            }
        } catch (e: Exception) {
            print("${Log.e("TAG", e.message.toString())}")
            return Result.failure()
        }
    }
}