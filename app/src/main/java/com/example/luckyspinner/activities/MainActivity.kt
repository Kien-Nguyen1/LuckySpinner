package com.example.luckyspinner.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.ViewModelProvider
import com.example.luckyspinner.databinding.ActivityMainBinding
import com.example.luckyspinner.repositories.TelegramRepository
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.TelegramViewModel
import com.example.luckyspinner.viewmodels.TelegramViewModelProviderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var telegramViewModel: TelegramViewModel

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewModel()
        Constants.DEVICE_ID = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun setUpViewModel() {
        val telegramRepository = TelegramRepository(applicationContext)
        val telegramViewModelProviderFactory = TelegramViewModelProviderFactory(application, telegramRepository)
        telegramViewModel = ViewModelProvider(this, telegramViewModelProviderFactory)[TelegramViewModel::class.java]
    }
}