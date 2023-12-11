package com.example.luckyspinner.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.luckyspinner.databinding.FragmentAddTimeEventBinding
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import java.util.Calendar


class AddTimeEventFragment : Fragment() {
    private val viewModel by viewModels<AddTimeEventViewModel>()
    private lateinit var binding : FragmentAddTimeEventBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)
        setUpDatePicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTimeAndDatePicker()
    }

    private fun getTimeAndDatePicker() {
        val selectedHour: Int
        val selectedMinutes: Int
        val is24HourFormat: Boolean

        if (Build.VERSION.SDK_INT >= 23) {
            selectedHour = binding.timePickerAddTimeEvent.hour
            selectedMinutes = binding.timePickerAddTimeEvent.minute
            is24HourFormat = binding.timePickerAddTimeEvent.is24HourView
        } else {
            selectedHour = binding.timePickerAddTimeEvent.currentHour
            selectedMinutes = binding.timePickerAddTimeEvent.currentMinute
            is24HourFormat = binding.timePickerAddTimeEvent.is24HourView
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinutes)
    }

    private fun setUpDatePicker() {
        val dayOfWeek = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "SunDay")

        binding.numberPickerAddTimeEvent.minValue = 0
        binding.numberPickerAddTimeEvent.maxValue = dayOfWeek.size - 1
        binding.numberPickerAddTimeEvent.displayedValues = dayOfWeek
    }
}