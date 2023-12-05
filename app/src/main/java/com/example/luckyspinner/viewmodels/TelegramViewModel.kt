package com.example.luckyspinner.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.luckyspinner.repositories.TelegramRepository

class TelegramViewModel(
    private val application: Application,
    private val telegramRepository: TelegramRepository
) : AndroidViewModel(application) {
}