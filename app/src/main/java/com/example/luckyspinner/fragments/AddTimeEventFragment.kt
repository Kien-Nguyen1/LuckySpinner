package com.example.luckyspinner.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.luckyspinner.databinding.FragmentAddTimeEventBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import com.example.luckyspinner.work.SendMessageWorker
import java.time.Duration
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AddTimeEventFragment : Fragment() {
    private val viewModel by viewModels<AddTimeEventViewModel>()
    private lateinit var binding : FragmentAddTimeEventBinding
    private lateinit var workManager: WorkManager
    private var channelId : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)
        channelId = arguments?.getString(Constants.ID_CHANNEL_KEY)
        setUpDatePicker()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workManager = WorkManager.getInstance(requireContext())

        binding.btnDoneAddTimeEvent.setOnClickListener {
            getTimeAndDatePicker()
        }
    }

    private fun getTimeAndDatePicker() {

        val selectedHour: Int = binding.timePickerAddTimeEvent.hour
        val selectedMinutes: Int = binding.timePickerAddTimeEvent.minute

        val selectedDayOfWeek = binding.numberPickerAddTimeEvent.value
        println("Here day come $selectedDayOfWeek")

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinutes)
        calendar.set(Calendar.DAY_OF_WEEK, selectedDayOfWeek)

        val timeNow = Calendar.getInstance()
        val timeDiff = if (calendar < timeNow) {
            Duration.between(timeNow.toInstant(), calendar.toInstant())
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Duration.between(calendar.toInstant(), timeNow.toInstant())
        }

        workManager.cancelAllWork()

        println("Here come calendar:  $calendar")


        //thoi gian va ngay da duoc chon
        val inputData = Data.Builder()
            .putLong("selected_time", calendar.timeInMillis)
            .build()

        val constraints = Constraints(requiredNetworkType = NetworkType.CONNECTED, requiresBatteryNotLow = true)


        val workRequest  = PeriodicWorkRequestBuilder<SendMessageWorker>(16, TimeUnit.MINUTES)
            .setInitialDelay(timeDiff)
            .setInputData(
                workDataOf(
                    Constants.CHAT_ID to "-1002136709675",
                    Constants.MESSAGE to "asdasdasdasd"
                )
            )
//            .addTag(WORK_TAG)
            .setConstraints(constraints)
            .build()
        val timeInMillis = Calendar.getInstance().timeInMillis
        val eventId = "$channelId $timeInMillis"
        workManager.also {
            it.enqueueUniquePeriodicWork(
                eventId,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
        println("Here the work come!")
        workManager.getWorkInfosForUniqueWorkLiveData(eventId)
            .observe(viewLifecycleOwner) {
                    workInfor ->
                if (workInfor.size != 0) {
                    workInfor[0]
                    if (workInfor[0].state == WorkInfo.State.SUCCEEDED) {
//                        println("Success from workInfor ${workInfor[0].outputData.getString(KEY_STRING)}")
                    }
                }
                else {
                    println("WorkInfo is null")
                }

            }

    }

    private fun setUpDatePicker() {
        val dayOfWeek = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "SunDay")

        binding.numberPickerAddTimeEvent.minValue = 0
        binding.numberPickerAddTimeEvent.maxValue = dayOfWeek.size - 1
        binding.numberPickerAddTimeEvent.displayedValues = dayOfWeek
    }
}