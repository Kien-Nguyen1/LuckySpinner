package com.example.luckyspinner.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.luckyspinner.repositories.TelegramRepository

class TelegramViewModelProviderFactory(
    private val application: Application,
    private val telegramRepository: TelegramRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TelegramViewModel(application, telegramRepository) as T
    }
}