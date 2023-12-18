package com.example.luckyspinner.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.luckyspinner.adapter.RandomSpinnerListAdapter
import com.example.luckyspinner.databinding.ChooseRandomSpinnerListLayoutBinding
import com.example.luckyspinner.databinding.FragmentAddTimeEventBinding
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import com.example.luckyspinner.work.SendMessageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.ArrayList
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AddTimeEventFragment : Fragment(), RandomSpinnerListAdapter.Listener {
    private val viewModel by viewModels<AddTimeEventViewModel>()
    private lateinit var binding : FragmentAddTimeEventBinding
    private lateinit var bindingDialog : ChooseRandomSpinnerListLayoutBinding
    private lateinit var workManager: WorkManager
    private var channelId : String? = null
    private var eventId : String? = null
    private lateinit var chooseSpinnerDialog : Dialog
    private lateinit var randomSpinnerAdapter : RandomSpinnerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)

        channelId = arguments?.getString(Constants.ID_CHANNEL_KEY)
        eventId = arguments?.getString(Constants.ID_EVENT_KEY)

        setupChooseSpinnerDialog(Gravity.CENTER)


        lifecycleScope.launch(Dispatchers.IO) {
            if (eventId == null) {
                val timeInMillis = Calendar.getInstance().timeInMillis
                eventId = "$channelId $timeInMillis"
                viewModel.getSpinnerFromChannel(channelId, eventId)
            }
            else {
                viewModel.getSpinnerFromEvent(channelId, eventId)
            }
            viewModel.getMembers(channelId, eventId)
        }
        setUpDatePicker()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workManager = WorkManager.getInstance(requireContext())
        binding.btnDoneAddTimeEvent.setOnClickListener {
            getTimeAndDatePicker()
        }
        binding.btnChooseRandomSpinner.setOnClickListener {
            chooseSpinnerDialog.show()
        }

    }
    private fun setupChooseSpinnerDialog(gravity: Int) {
        setUpRecycleView()
        chooseSpinnerDialog = Dialog(requireContext())
        chooseSpinnerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        chooseSpinnerDialog.setContentView(bindingDialog.root)

        val window : Window = chooseSpinnerDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute
        bindingDialog.rvChooseRandomSpinnerList
    }


    private fun getTimeAndDatePicker() {
        workManager.cancelAllWork()
        val progressDialog = ProgressDialog(context)
        progressDialog.show()

        val selectedHour: Int = binding.timePickerAddTimeEvent.hour
        val selectedMinutes: Int = binding.timePickerAddTimeEvent.minute
        val typeEvent = if (binding.btnSwitchModeAddTimeEvent.isChecked) Constants.EVENT_TYPE_EVERY_DAY else Constants.EVENT_TYPE_ONCE

        viewModel.saveEvent(
            channelId,
            Event(
                eventId!!,
                typeEvent,
                selectedHour,
                selectedMinutes
                )
        )
        val timeNow = Calendar.getInstance()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinutes)

        println("so sanh ${calendar > timeNow}")
        val durationDiff = if (calendar > timeNow) {
            Duration.between(timeNow.toInstant(), calendar.toInstant())
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Duration.between(timeNow.toInstant(), calendar.toInstant())
        }

        val constraints = Constraints(requiredNetworkType = NetworkType.CONNECTED, requiresBatteryNotLow = true)
        val listSpinnerId = ArrayList<String>()
        val listMemberId = ArrayList<String>()
        val listMemberName = ArrayList<String>()
        val listSpinnerName = ArrayList<String>()

        viewModel.spinnerList.value?.forEach {
            if (it.hasSelected) {
                listSpinnerId.add(it.idSpin)
                listSpinnerName.add(it.titleSpin)
            }
        }
        viewModel.memberList.value?.forEach {
            if (it.hasSelected) {
                listMemberId.add(it.idMember)
                listMemberName.add(it.nameMember)
            }
        }

        workManager.apply {
            val data = workDataOf(
                Constants.CHAT_ID to "-1002136709675",
                Constants.ID_CHANNEL_KEY to channelId,
                Constants.ID_EVENT_KEY to eventId,
                "deviceId" to Constants.DEVICE_ID
            )
            val workRequest  = PeriodicWorkRequestBuilder<SendMessageWorker>(16, TimeUnit.MINUTES)
                .setInitialDelay(durationDiff)
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            val workRequestFake = OneTimeWorkRequestBuilder<SendMessageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()

            if (binding.btnSwitchModeAddTimeEvent.isChecked) {
                enqueueUniquePeriodicWork(
                    eventId!!,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            } else {
                enqueueUniqueWork(
                    eventId!!,
                    ExistingWorkPolicy.REPLACE,
                    workRequestFake
                )
            }
        }
        workManager.getWorkInfosForUniqueWorkLiveData(eventId!!)
            .observe(viewLifecycleOwner) {
                    workInfor ->
                if (workInfor.size != 0) {
                    workInfor[0]
                    if (workInfor[0].state == WorkInfo.State.SUCCEEDED) {
                        progressDialog.dismiss()

//                        println("Success from workInfor ${workInfor[0].outputData.getString(KEY_STRING)}")
                    }
                }
                else {
                    println("WorkInfo is null")
                }

            }
    }
    private fun setUpRecycleView() {
        bindingDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingDialog.rvChooseRandomSpinnerList.apply {
            randomSpinnerAdapter = RandomSpinnerListAdapter(this@AddTimeEventFragment)
            adapter = randomSpinnerAdapter
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.spinnerList.observe(viewLifecycleOwner) {
            randomSpinnerAdapter.spinners = it
        }
    }


    private fun setUpDatePicker() {
        val dayOfWeek = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "SunDay")

        binding.numberPickerAddTimeEvent.minValue = 0
        binding.numberPickerAddTimeEvent.maxValue = dayOfWeek.size - 1
        binding.numberPickerAddTimeEvent.displayedValues = dayOfWeek
    }

    override fun onItemClick(id: String) {
        TODO("Not yet implemented")
    }

    override fun onDeleteItem(id: String) {
        TODO("Not yet implemented")
    }

    override fun onCheckboxClick(idSpinner: String, position : Int, hasSelected : Boolean) {
        viewModel.checkBoxSpinner(channelId, eventId!!, idSpinner, hasSelected)
    }
}