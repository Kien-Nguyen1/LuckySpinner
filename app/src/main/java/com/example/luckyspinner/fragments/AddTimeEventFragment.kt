package com.example.luckyspinner.fragments

import android.app.Dialog
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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.RandomSpinnerListAdapter
import com.example.luckyspinner.databinding.ChooseRandomSpinnerListLayoutBinding
import com.example.luckyspinner.databinding.FragmentAddTimeEventBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import com.example.luckyspinner.work.SendMessageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
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
        }

        binding.appBarAddTimeEvent.apply {
            toolBar.title = "Add Time Event"
            toolBar.menu.findItem(R.id.spinnerListFragment)?.isVisible = false
            toolBar.menu.findItem(R.id.memberListFragment)?.isVisible = false
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

        binding.appBarAddTimeEvent.apply {
            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
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

        val selectedHour: Int = binding.timePickerAddTimeEvent.hour
        val selectedMinutes: Int = binding.timePickerAddTimeEvent.minute

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinutes)

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
                    Constants.MESSAGE to "asdasdasdasd",
//                    Constants.LIST_MEMBER to
                )
            )
//            .addTag(WORK_TAG)
            .setConstraints(constraints)
            .build()
        workManager.also {
            it.enqueueUniquePeriodicWork(
                eventId!!,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
        println("Here the work come!")
        workManager.getWorkInfosForUniqueWorkLiveData(eventId!!)
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

    override fun onItemClick(id: String) {
        //
    }

    override fun onDeleteItem(id: String) {
        TODO("Not yet implemented")
    }

    override fun onCheckboxClick(id: String, position : Int) {
        viewModel.checkBoxSpinner(channelId, eventId!!, id)
    }
}