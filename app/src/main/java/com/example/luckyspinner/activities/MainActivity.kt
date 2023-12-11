package com.example.luckyspinner.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.luckyspinner.databinding.ActivityMainBinding
import com.example.luckyspinner.repositories.TelegramRepository
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.TelegramViewModel
import com.example.luckyspinner.viewmodels.TelegramViewModelProviderFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random


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
        val currentDateTime = LocalDateTime.now()


        // Format the date and time using a formatter
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime: String = currentDateTime.format(formatter)

        println("Here come  $formattedDateTime")

        // Specify the desired timezone (GMT+7)
        // Specify the desired timezone (GMT+7)
        val gmtPlus7 = ZoneId.of("GMT+7")

        // Create a LocalDateTime representing the date and time

        // Create a LocalDateTime representing the date and time
        val localDateTime = LocalDateTime.of(2023, 1, 1, 7, 0) // January 1, 2023, 7:00 AM


        // Combine LocalDateTime with the timezone to get ZonedDateTime

        // Combine LocalDateTime with the timezone to get ZonedDateTime
        val zonedDateTime = ZonedDateTime.of(localDateTime, gmtPlus7)

        println("ZonedDateTime: $zonedDateTime")


    }

    private fun setUpViewModel() {
        val telegramRepository = TelegramRepository(applicationContext)
        val telegramViewModelProviderFactory = TelegramViewModelProviderFactory(application, telegramRepository)
        telegramViewModel = ViewModelProvider(this, telegramViewModelProviderFactory)[TelegramViewModel::class.java]
    }
}