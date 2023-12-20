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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.DateListAdapter
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
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AddTimeEventFragment : Fragment(), RandomSpinnerListAdapter.Listener, DateListAdapter.Listener {
    private val viewModel by viewModels<AddTimeEventViewModel>()
    private lateinit var binding : FragmentAddTimeEventBinding
    private lateinit var bindingRandomDialog : ChooseRandomSpinnerListLayoutBinding
    private lateinit var bindingDateDialog: ChooseRandomSpinnerListLayoutBinding
    private lateinit var workManager: WorkManager
    private var channelId : String? = null
    private var eventId : String? = null
    private var telegramChannelId : String? = null
    private var isLoadedFirstTime = false
    private lateinit var chooseSpinnerDialog : Dialog
    private lateinit var dateDialog : Dialog
    private lateinit var randomSpinnerAdapter : RandomSpinnerListAdapter
    private lateinit var dateAdapter : DateListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)

        channelId = arguments?.getString(Constants.ID_CHANNEL_KEY)
        eventId = arguments?.getString(Constants.ID_EVENT_KEY)
        telegramChannelId = arguments?.getString(Constants.ID_TELEGRAM_CHANNEL_KEY)

        if (eventId == null) binding.btnDeleteEvent.visibility = View.GONE

        setUpDatePicker()

        viewModel.getEvent(channelId, eventId)

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

        binding.appBarAddTimeEvent.apply {
            toolBar.title = "Add Time Event"
            toolBar.menu.findItem(R.id.memberListFragment)?.isVisible = false
            toolBar.menu.findItem(R.id.spinnerListFragment)?.isVisible = false
        }

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
        binding.btnDeleteEvent.setOnClickListener {
            viewModel.deleteEvent(channelId, eventId)
            findNavController().popBackStack()
        }

        binding.appBarAddTimeEvent.apply {
            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }


    private fun getTimeAndDatePicker() {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()

        val selectedHour: Int = binding.timePickerAddTimeEvent.hour
        val selectedMinutes: Int = binding.timePickerAddTimeEvent.minute
//        val typeEvent = if (binding.btnSwitchModeAddTimeEvent.isChecked) Constants.EVENT_TYPE_EVERY_DAY else Constants.EVENT_TYPE_ONCE



        viewModel.saveEvent(
            channelId,
            Event(
                eventId!!,
                typeEvent = null,
                selectedHour,
                selectedMinutes,
                getListDay()
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

        workManager.apply {
            val data = workDataOf(
                Constants.ID_TELEGRAM_CHANNEL_KEY to "-1002136709675",
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

            if (false) {
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
                        findNavController().popBackStack()
                        println("Success from workInfor ${workInfor[0].outputData.getString("")}")
                    }
                    if (workInfor[0].state == WorkInfo.State.FAILED) {
                        progressDialog.dismiss()
                        val message = workInfor[0].outputData.getString(Constants.MESSAGE)
                        message?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    println("WorkInfo is null")
                }

            }
    }
    private fun setUpRecycleView() {
        bindingRandomDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingRandomDialog.rvChooseRandomSpinnerList.apply {
            randomSpinnerAdapter = RandomSpinnerListAdapter(this@AddTimeEventFragment)
            adapter = randomSpinnerAdapter
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.spinnerList.observe(viewLifecycleOwner) {
            randomSpinnerAdapter.spinners = it
        }
    }


    private fun setUpDatePicker() {
        binding.btnEventSchedule.setOnClickListener {
            dateDialog.show()
        }

        bindingDateDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingDateDialog.tvTitleChooseRandomSpinnerList.visibility = View.GONE
        bindingDateDialog.rvChooseRandomSpinnerList.apply {
            dateAdapter = DateListAdapter(this@AddTimeEventFragment)
            adapter = dateAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.event.observe(viewLifecycleOwner) {
            if (!isLoadedFirstTime) {
                it.hour?.let { eventHour ->
                    binding.timePickerAddTimeEvent.apply {
                        hour = eventHour
                        minute = it.minute!!
                    }
                }
                isLoadedFirstTime = true
            }
            dateAdapter.dayList = it.listDay
        }
        setupDatePickerDialog()
    }

    fun setupDatePickerDialog() {
        dateDialog = Dialog(requireContext())
        dateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dateDialog.setContentView(bindingDateDialog.root)

        val window : Window = dateDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = Gravity.CENTER
        window.attributes = windowAttribute
    }
    private fun setupChooseSpinnerDialog(gravity: Int) {
        setUpRecycleView()
        chooseSpinnerDialog = Dialog(requireContext())
        chooseSpinnerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        chooseSpinnerDialog.setContentView(bindingRandomDialog.root)

        val window : Window = chooseSpinnerDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute
    }
    fun isEventValidate() : Boolean {
        var isValidated = true
        var memberCount = 0
        var spinnerCount = 0
        viewModel.memberList.value!!.forEach {
            if (it.hasSelected) ++memberCount
        }
        if (memberCount < 2) {
            isValidated = false
            Toast.makeText(context, "You must select at least 2 members",Toast.LENGTH_LONG).show()
        }
        viewModel.spinnerList.value!!.forEach {
            if (it.hasSelected) ++spinnerCount
        }
        if (spinnerCount < 1) {
            isValidated = false
            Toast.makeText(context, "You must select at least 1 spinner",Toast.LENGTH_LONG).show()
        }
        return isValidated

    }

    fun getListDay() : List<Int>{
        val list = ArrayList<Int>().toMutableList()
        viewModel.event.value!!.listDay.forEach {
            list.add(it)
        }
        return list
    }


    override fun onItemClick(id: String) {
    }

    override fun onDeleteItem(id: String) {
    }

    override fun onCheckboxClick(idSpinner: String, position : Int, hasSelected : Boolean) {
        viewModel.checkBoxSpinner(channelId, eventId!!, idSpinner, hasSelected)
    }

    override fun onDateClick(position: Int, isChecked : Boolean) {
        val dayNumber = if (isChecked) changeTheNumberOfDay(position) else 0
        viewModel.event.value = viewModel.event.value?.apply {
            val tempList = listDay.toMutableList()
            tempList[position] = dayNumber
            listDay = tempList
        }
    }

    fun changeTheNumberOfDay(position : Int) : Int {
        if (position == 6) return Constants.SUNDAY
        return position + 2
    }
}