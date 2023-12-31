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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.DateListAdapter
import com.example.luckyspinner.adapter.MemberInEventListAdapter
import com.example.luckyspinner.adapter.RandomSpinnerListAdapter
import com.example.luckyspinner.databinding.ChooseRandomSpinnerListLayoutBinding
import com.example.luckyspinner.databinding.FragmentAddTimeEventBinding
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import com.example.luckyspinner.work.SendMessageWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AddTimeEventFragment : Fragment(), RandomSpinnerListAdapter.Listener, DateListAdapter.Listener, MemberInEventListAdapter.Listener {
    private val viewModel by viewModels<AddTimeEventViewModel>()
    private lateinit var binding : FragmentAddTimeEventBinding
    private lateinit var bindingRandomDialog : ChooseRandomSpinnerListLayoutBinding
    private lateinit var bindingMemberDialog : ChooseRandomSpinnerListLayoutBinding
    private lateinit var bindingDateDialog: ChooseRandomSpinnerListLayoutBinding
    private lateinit var workManager: WorkManager
    private var channelId : String = EMPTY_STRING
    private var eventId : String? = null
    private lateinit var telegramChannelId : String
    private var isLoadedFirstTime = false
    private lateinit var chooseSpinnerDialog : Dialog
    private lateinit var chooseMemberDialog : Dialog
    private lateinit var dateDialog : Dialog
    private lateinit var randomSpinnerAdapter : RandomSpinnerListAdapter
    private lateinit var memberInEventAdapter : MemberInEventListAdapter
    private lateinit var dateAdapter : DateListAdapter
    private lateinit var progressDialog : ProgressDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)
        progressDialog =  ProgressDialog(context)

        channelId = arguments?.getString(Constants.ID_CHANNEL_KEY)!!
        eventId = arguments?.getString(Constants.ID_EVENT_KEY)

        telegramChannelId = arguments?.getString(Constants.ID_TELEGRAM_CHANNEL_KEY)!!
        println("Here come teleid $telegramChannelId")
        binding.appBarAddTimeEvent.apply {
            toolBar.menu.findItem(R.id.memberListFragment)?.isVisible = false
            toolBar.menu.findItem(R.id.spinnerListFragment)?.isVisible = false
        }
        workManager = WorkManager.getInstance(requireContext())

        if (eventId == null) {
            binding.appBarAddTimeEvent.toolBar.title = "Add Time Event"
        } else {
            binding.appBarAddTimeEvent.toolBar.title = "Edit Time Event"
        }


        lifecycleScope.launch(Dispatchers.IO) {
            if (eventId == null) {
                val timeInMillis = Calendar.getInstance().timeInMillis
                eventId = "$channelId $timeInMillis"
                viewModel.getEvent(channelId, null, newEventId = eventId)

                viewModel.getMembers(channelId, true)
                viewModel.getSpinnerFromChannel(channelId, true)
            } else {
                viewModel.getEvent(channelId, eventId)
                viewModel.getMembers(channelId, false)
                viewModel.getSpinnerFromChannel(channelId, false)
//                viewModel.getSpinnerFromEvent(channelId, eventId!!)
//                viewModel.getMemberFromEvent(channelId, eventId!!)
            }
        }

        setUpRecycleView()

        setupObservers()

        setUpDatePicker()

        setupChooseSpinnerDialog()
        setupMemberDialog()


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnDoneAddTimeEvent.setOnClickListener {
            getTimeAndDatePicker()
        }
        binding.btnChooseRandomSpinner.setOnClickListener {
            chooseSpinnerDialog.show()
        }
        binding.btnSpinnerNow.setOnClickListener {
            handleTestNow()
        }
        binding.btnMemberList.setOnClickListener {
            chooseMemberDialog.show()
        }

        binding.appBarAddTimeEvent.apply {
            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }


    private fun getTimeAndDatePicker() {
        if (!isEventValidate()) {
            return
        }
//        workManager.cancelAllWork()
        progressDialog.show()

        val selectedHour: Int = binding.timePickerAddTimeEvent.hour
        val selectedMinutes: Int = binding.timePickerAddTimeEvent.minute

        eventId?.let { viewModel.saveListSpinner(channelId, it) }

        viewModel.saveEvent(
            channelId,
            Event(
                eventId!!,
                typeEvent = Constants.EVERY_WEEK,
                selectedHour,
                selectedMinutes,
                getListDay(),
                binding.edtEventName.text.toString()
            )
        )
        viewModel.saveListMember(channelId, eventId!!)

        val timeNow = Calendar.getInstance()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinutes)


        val durationDiff = if (calendar.get(Calendar.HOUR_OF_DAY) == timeNow.get(Calendar.HOUR_OF_DAY) && timeNow.get(Calendar.MINUTE) == calendar.get(Calendar.MINUTE))
        {
            Duration.ZERO
        }
                else if (calendar > timeNow) {
            Duration.between(timeNow.toInstant(), calendar.toInstant())
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            Duration.between(timeNow.toInstant(), calendar.toInstant())
        }

        val constraints = Constraints(requiredNetworkType = NetworkType.CONNECTED, requiresBatteryNotLow = true)

        workManager.apply {
            val data = workDataOf(
                Constants.ID_TELEGRAM_CHANNEL_KEY to telegramChannelId,
                Constants.ID_CHANNEL_KEY to channelId,
                Constants.ID_EVENT_KEY to eventId,
                Constants.DEVICE_ID_KEY to Constants.DEVICE_ID
            )
            val workRequest  = PeriodicWorkRequestBuilder<SendMessageWorker>(16, TimeUnit.MINUTES)
                .setInitialDelay(durationDiff)
                .setInputData(data)
                .setConstraints(constraints)
                .addTag(channelId)
                .build()

            enqueueUniquePeriodicWork(
                eventId!!,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }
        viewModel.isSaveEventSuccess.observe(viewLifecycleOwner) { isSave ->
            if (isSave) {
                    fun navigate() {
                        println("Here come navigate")
                        progressDialog.dismiss()
                        Toast.makeText(context, "Saving successful" , Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    viewModel.isSaveListSpinnerSuccess.apply {
                        if (value == true) {
                            navigate()
                        } else {
                            observe(viewLifecycleOwner) {
                                if (it) {
                                    navigate()
                                } else {
                                    Toast.makeText(context, "Something wrong. Try again!" , Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
        }
    }
    private fun setUpRecycleView() {
        bindingRandomDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingRandomDialog.rvChooseRandomSpinnerList.apply {
            randomSpinnerAdapter = RandomSpinnerListAdapter(this@AddTimeEventFragment, eventId!!)
            adapter = randomSpinnerAdapter
            layoutManager = LinearLayoutManager(context)
        }

        bindingMemberDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingMemberDialog.rvChooseRandomSpinnerList.apply {
            memberInEventAdapter = MemberInEventListAdapter(this@AddTimeEventFragment, eventId!!)
            adapter = memberInEventAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }


    private fun setUpDatePicker() {
        bindingDateDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingDateDialog.tvTitleChooseRandomSpinnerList.visibility = View.GONE
        bindingDateDialog.rvChooseRandomSpinnerList.apply {
            dateAdapter = DateListAdapter(this@AddTimeEventFragment)
            adapter = dateAdapter
            layoutManager = LinearLayoutManager(context)
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
    private fun setupChooseSpinnerDialog() {
        chooseSpinnerDialog = Dialog(requireContext())
        chooseSpinnerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        chooseSpinnerDialog.setContentView(bindingRandomDialog.root)

        bindingRandomDialog.btnAddElement.setOnClickListener {
            chooseSpinnerDialog.dismiss()
            findNavController().navigate(R.id.spinnerListFragment, Bundle().apply {
                putString(Constants.ID_CHANNEL_KEY, channelId)
            })
        }

        val window : Window = chooseSpinnerDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = Gravity.CENTER
        window.attributes = windowAttribute
    }

    private fun setupMemberDialog() {
        chooseMemberDialog = Dialog(requireContext())
        chooseMemberDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        chooseMemberDialog.setContentView(bindingMemberDialog.root)
        bindingMemberDialog.btnAddElement.setOnClickListener {
            chooseMemberDialog.dismiss()
            findNavController().navigate(R.id.memberListFragment, Bundle().apply {
                putString(Constants.ID_CHANNEL_KEY, channelId)
            })
        }
        bindingMemberDialog.tvTitleChooseRandomSpinnerList.text = "Choose Members"

        val window : Window = chooseMemberDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = Gravity.CENTER
        window.attributes = windowAttribute
    }

    fun handleTestNow() {
        if (!isEventValidate()) {
            return
        }
        progressDialog.show()
        val testId = Calendar.getInstance().timeInMillis.toString()
        val constraints = Constraints(requiredNetworkType = NetworkType.CONNECTED, requiresBatteryNotLow = true)


        viewModel.saveEvent(
            channelId,
            Event(
                testId,
                typeEvent = null,
                0,
                0,
                getListDay()
            )
        )

        workManager.apply {
            val data = workDataOf(
                Constants.ID_TELEGRAM_CHANNEL_KEY to telegramChannelId,
                Constants.ID_CHANNEL_KEY to channelId,
                Constants.ID_EVENT_KEY to testId,
                Constants.DEVICE_ID_KEY to Constants.DEVICE_ID
            )
            val workRequestTest = OneTimeWorkRequestBuilder<SendMessageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()

            enqueueUniqueWork(
                testId,
                ExistingWorkPolicy.REPLACE,
                workRequestTest
            )
        }
        workManager.getWorkInfosForUniqueWorkLiveData(testId)
            .observe(viewLifecycleOwner) {
                if (it.size != 0) {
                    if (it[0].state == WorkInfo.State.SUCCEEDED) {
                        progressDialog.dismiss()
                        println("Success from workInfor ${it[0].outputData.getString("")}")
                    }
                    if (it[0].state == WorkInfo.State.FAILED) {
                        progressDialog.dismiss()
                        val message = it[0].outputData.getString(Constants.MESSAGE)
                        message?.let {
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    println("WorkInfo is null")
                }

            }
    }

    fun handleMemberList() {

    }
    fun setupObservers() {
        viewModel.spinnerList.observe(viewLifecycleOwner) {
            randomSpinnerAdapter.spinners = it
        }
        viewModel.memberList.observe(viewLifecycleOwner) {
            memberInEventAdapter.members = it
            println(it.size)
        }
        viewModel.event.observe(viewLifecycleOwner) {
                it.hour?.let { eventHour ->
                    binding.timePickerAddTimeEvent.apply {
                        hour = eventHour
                        minute = it.minute!!
                    }
                }
            dateAdapter.dayList = it.listDay
        }
        viewModel.isShowProgressDialog.observe(viewLifecycleOwner) {
            if (it) progressDialog.show()
            else progressDialog.dismiss()
        }
    }
    fun isEventValidate() : Boolean {
        var isValidated = true
        var memberCount = 0
        var spinnerCount = 0
        viewModel.memberList.value?.forEach {
            if (it.hasSelected) ++memberCount
        }
        if (memberCount < 2) {
            isValidated = false
            Toast.makeText(context, "You must select at least 2 members",Toast.LENGTH_LONG).show()
        }
        viewModel.spinnerList.value?.forEach {
            if (it.hasSelected) ++spinnerCount
        }
        if (spinnerCount < 1) {
            isValidated = false
            Toast.makeText(context, "You must select at least 1 spinner",Toast.LENGTH_LONG).show()
        }

        return isValidated
    }

    fun getListDay() : List<Int> {
//        val list = ArrayList<Int>().toMutableList()
//        viewModel.event.value?.listDay?.forEach {
//            list.add(it)
//        }
//        return list
        return viewModel.event.value?.listDay ?: ArrayList()
    }


    override fun onItemClick(id: String) {
    }

    override fun onDeleteItem(id: String) {
    }

    override fun onCheckboxClickSpinner(id: String, position : Int, hasSelected : Boolean) {
        viewModel.checkBoxSpinner(position, hasSelected)
    }

    override fun onCheckboxClickMember(id : String, position : Int, hasSelected : Boolean) {
        viewModel.checkBoxMember(position, hasSelected)
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

    override fun onDestroyView() {
        super.onDestroyView()
        val list : MutableList<MutableLiveData<*>> = ArrayList()
        list.add(viewModel.isGettingSpinnerSuccess)
        list.add(viewModel.isGettingEventSuccess)

        Function.toNull(list)

        Function.removeObservers(list, viewLifecycleOwner)
    }
}