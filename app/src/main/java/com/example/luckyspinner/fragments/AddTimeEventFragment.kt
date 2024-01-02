package com.example.luckyspinner.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
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
import com.example.luckyspinner.util.Function.changeTheNumberOfDay
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import com.example.luckyspinner.work.SendMessageWorker
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private var isFirstLoad = true
    private var isAdd = false
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
        println("addtime HHere come oncreateview")
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)
        progressDialog =  ProgressDialog(context)

        channelId = arguments?.getString(Constants.ID_CHANNEL_KEY)!!

        if (isFirstLoad) {
            eventId = arguments?.getString(Constants.ID_EVENT_KEY)
        }

        telegramChannelId = arguments?.getString(Constants.ID_TELEGRAM_CHANNEL_KEY)!!

        binding.appBarAddTimeEvent.apply {
            toolBar.menu.findItem(R.id.memberListFragment)?.isVisible = false
            toolBar.menu.findItem(R.id.spinnerListFragment)?.isVisible = false
        }
        workManager = WorkManager.getInstance(requireContext())
        if (eventId == null) {
            isAdd = true
        }

        if (isAdd) {
            binding.appBarAddTimeEvent.toolBar.title = "Add Time Event"
        } else {
            binding.appBarAddTimeEvent.toolBar.title = "Edit Time Event"
        }

        binding.textFieldEventName.editText?.doAfterTextChanged {
            viewModel.event.value = viewModel.event.value?.apply {
                nameEvent = binding.edtEventName.toString()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            if (isFirstLoad) {
                if (eventId == null) {
                    val timeInMillis = Calendar.getInstance().timeInMillis
                    eventId = "$channelId $timeInMillis"
                    viewModel.getMembers(channelId, isAdd)
                    viewModel.getSpinnerFromChannel(channelId, isAdd)
                    viewModel.getEvent(channelId, null, newEventId = eventId!!)
                } else {
                    viewModel.getEvent(channelId, eventId)
                    viewModel.getMembers(channelId, isAdd)
                    viewModel.getSpinnerFromChannel(channelId, isAdd)
                }
                isFirstLoad = false
            } else {
                viewModel.getMembers(channelId, isAdd)
                viewModel.getSpinnerFromChannel(channelId, isAdd)
            }

        }

        setUpRecycleView()

        setupObservers()



        setUpDatePicker()

        setupChooseSpinnerDialog()
        setupMemberDialog()

        dayOfWeekClickEvent()



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

        val typeEvent = if (getListDay() == Event().listDay) Constants.ONCE else Constants.EVERY_WEEK


        viewModel.saveEvent(
            channelId,
            Event(
                eventId!!,
                typeEvent,
                selectedHour,
                selectedMinutes,
                getListDay(),
                binding.edtEventName.text.toString()
            )
        )
        eventId?.let {
            viewModel.saveListSpinner(channelId, it)
            viewModel.saveListMember(channelId, it)
        }


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
            if (typeEvent == Constants.EVERY_WEEK) {
                val workRequest  = PeriodicWorkRequestBuilder<SendMessageWorker>(1, TimeUnit.DAYS)
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
            } else {
                val workRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
                    .setInitialDelay(durationDiff)
                    .setInputData(data)
                    .setConstraints(constraints)
                    .addTag(channelId)
                    .build()

                enqueueUniqueWork(
                    eventId!!,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
        viewModel.isSaveEventSuccess.observe(viewLifecycleOwner) {
            it?.let {
                isSave ->
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
                        println("navigate 1")
                    } else {
                        observe(viewLifecycleOwner) {
                            if (it) {
                                navigate()
                                println("navigate 2")
                            } else {
                                Toast.makeText(context, "Something wrong. Try again!" , Toast.LENGTH_LONG).show()
                            }
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

        bindingRandomDialog.btnBack.setOnClickListener {
            chooseSpinnerDialog.dismiss()
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

        bindingMemberDialog.btnBack.setOnClickListener {
            chooseMemberDialog.dismiss()
        }

        val window : Window = chooseMemberDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = Gravity.CENTER
        window.attributes = windowAttribute
    }


    private fun dayOfWeekClickEvent() {
        binding.dayOfWeek.apply {
            btnMonday.setOnClickListener {
                viewModel.handleClickDay(Constants.MONDAY_POSITION)
            }

            btnTuesday.setOnClickListener {
                viewModel.handleClickDay(Constants.TUESDAY_POSITION)
            }

            btnWednesday.setOnClickListener {
                viewModel.handleClickDay(Constants.WEDNESDAY_POSITION)
            }

            btnThursday.setOnClickListener {
                viewModel.handleClickDay(Constants.THURSDAY_POSITION)
            }

            btnFriday.setOnClickListener {
                viewModel.handleClickDay(Constants.FRIDAY_POSITION)
            }

            btnSaturday.setOnClickListener {
                viewModel.handleClickDay(Constants.SATURDAY_POSITION)
            }

            btnSunday.setOnClickListener {
                viewModel.handleClickDay(Constants.SUNDAY_POSITION)
            }
        }
    }
    fun handleDayOfWeek(event : Event) {
        fun customButton(button : MaterialButton, isActive : Boolean) {
            fun getColorBackGround(isActive : Boolean) : Int{
                return if (isActive)  Color.parseColor("#6750A4") else Color.WHITE
            }

            button.setBackgroundColor(getColorBackGround(isActive))
            button.setTextColor(getColorBackGround(!isActive))
        }
        binding.dayOfWeek.apply {
            event.listDay.apply {
                customButton(btnMonday, contains(Calendar.MONDAY))
                customButton(btnTuesday, contains(Calendar.TUESDAY))
                customButton(btnWednesday, contains(Calendar.WEDNESDAY))
                customButton(btnThursday, contains(Calendar.THURSDAY))
                customButton(btnFriday, contains(Calendar.FRIDAY))
                customButton(btnSaturday, contains(Calendar.SATURDAY))
                customButton(btnSunday, contains(Calendar.SUNDAY))
            }

        }
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
            var isAllSelected = true
            run breaking@{
                it.forEach { spinner ->
                    if (!spinner.listEvent.contains(eventId)) {
                        isAllSelected = false
                        return@breaking
                    }
                }
            }
            bindingRandomDialog.checkBoxAll.isChecked = isAllSelected
            bindingRandomDialog.checkBoxAll.setOnClickListener {
                viewModel.allCheckboxSpinner(!bindingRandomDialog.checkBoxAll.isChecked)
            }
        }
        viewModel.memberList.observe(viewLifecycleOwner) {
            memberInEventAdapter.members = it
            var isAllSelected = true
            run breaking@{
                it.forEach { member ->
                    if (!member.listEvent.contains(eventId)) {
                        isAllSelected = false
                        return@breaking
                    }
                }
            }
            bindingMemberDialog.checkBoxAll.isChecked = isAllSelected
            println("Here come is $isAllSelected")
            bindingMemberDialog.checkBoxAll.setOnClickListener {
                viewModel.allCheckboxMember(!bindingMemberDialog.checkBoxAll.isChecked)
            }
        }
        viewModel.event.observe(viewLifecycleOwner) {
                it.hour?.let { eventHour ->
                    binding.timePickerAddTimeEvent.apply {
                        hour = eventHour
                        minute = it.minute!!
//                        binding.timePickerAddTimeEvent.setOnTimeChangedListener { view, hourOfDay, minute ->
//                            if (viewModel.event.isInitialized) {
//                                val event = viewModel.event.value!!
//                                event.hour = hourOfDay
//                                event.minute = minute
//                                viewModel.event.value = event
//                            }
//                        }
                    }
                }
            if (!isAdd) {
                binding.edtEventName.setText(it.nameEvent)
            }

            handleDayOfWeek(it)
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
        binding.edtEventName.apply {
            if (text.toString() == "") {
                error = "Please fill this fields!"
                isValidated = false
            }
        }

        return isValidated
    }

    fun getListDay() : MutableList<Int> {
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

    override fun onDestroyView() {
        super.onDestroyView()
        val list : MutableList<MutableLiveData<*>> = ArrayList()
        list.add(viewModel.isGettingSpinnerSuccess)
        list.add(viewModel.isGettingEventSuccess)
        list.add(viewModel.isSaveEventSuccess)

        Function.toNull(list)

        Function.removeObservers(list, viewLifecycleOwner)
    }
}