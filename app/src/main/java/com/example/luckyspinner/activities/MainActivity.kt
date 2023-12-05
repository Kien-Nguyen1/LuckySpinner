package com.example.luckyspinner.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.luckyspinner.R
import com.example.luckyspinner.databinding.ActivityMainBinding
import com.example.luckyspinner.repositories.TelegramRepository
import com.example.luckyspinner.viewmodels.TelegramViewModel
import com.example.luckyspinner.viewmodels.TelegramViewModelProviderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var telegramViewModel: TelegramViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViewModel()
        setUpBottomNavMenu()
    }
    private fun setUpBottomNavMenu() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavMenu.setupWithNavController(navController)
    }

    private fun setUpViewModel() {
        val telegramRepository = TelegramRepository(applicationContext)
        val telegramViewModelProviderFactory = TelegramViewModelProviderFactory(application, telegramRepository)
        telegramViewModel = ViewModelProvider(this, telegramViewModelProviderFactory)[TelegramViewModel::class.java]
    }
}