package com.example.luckyspinner.fragments

import android.annotation.SuppressLint
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
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
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
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.util.Function.addMarginToLastItem
import com.example.luckyspinner.util.Function.addMarginToLastItemHorizontal
import com.example.luckyspinner.util.Function.changeTheNumberOfDay
import com.example.luckyspinner.util.Function.numberToMinuteForm
import com.example.luckyspinner.viewmodels.AddTimeEventViewModel
import com.example.luckyspinner.work.SendMessageWorker
import com.google.android.material.button.MaterialButton
import com.google.android.material.search.SearchView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
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
    private var isFirstLoad = true
    private var hasSetNameAndTime = false
    private var isAdd = false
    private lateinit var chooseSpinnerDialog : Dialog
    private lateinit var chooseMemberDialog : Dialog
    private lateinit var dateDialog : Dialog
    private lateinit var randomSpinnerAdapter : RandomSpinnerListAdapter
    private lateinit var memberInEventAdapter : MemberInEventListAdapter
    private lateinit var dateAdapter : DateListAdapter
    private lateinit var progressDialog : ProgressDialog

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        println("addtime HHere come oncreateview")
        binding = FragmentAddTimeEventBinding.inflate(inflater, container, false)

        progressDialog =  ProgressDialog(context)

        hasSetNameAndTime = false

        channelId = arguments?.getString(Constants.ID_CHANNEL_KEY)!!

        if (isFirstLoad) {
            eventId = arguments?.getString(Constants.ID_EVENT_KEY)
        }

        telegramChannelId = arguments?.getString(Constants.ID_TELEGRAM_CHANNEL_KEY)!!

        binding.appBarAddTimeEvent.apply {
            btnSpinnerList.visibility = View.GONE
            btnMemberList.visibility = View.GONE
        }

        binding.edtTime.onFocusChangeListener = View.OnFocusChangeListener { view, hasForcus ->
            if (hasForcus) {
                val timeNow = Calendar.getInstance()
                var hour = timeNow.get(Calendar.HOUR)
                var minute = timeNow.get(Calendar.MINUTE)
                viewModel.event.value?.hour?.let {
                    hour = it
                    minute = viewModel.event.value?.minute!!
                }
                val picker = MaterialTimePicker.Builder()
                    .setInputMode(INPUT_MODE_CLOCK)
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hour)
                    .setMinute(minute)
                    .setTitleText("Select Time")
                    .build()

                picker.show(parentFragmentManager, "timepicker")

                picker.addOnPositiveButtonClickListener {
                    val hour = picker.hour
                    val minutes = picker.minute

                    println("Here come $hour : $minutes")

                    viewModel.event.value = viewModel.event.value?.apply {
                        this.hour = hour
                        this.minute = minutes
                    }
                }

                picker.addOnNegativeButtonClickListener {
                    picker.dismiss()
                }

                binding.edtTime.isFocusable = false
                binding.edtTime.isFocusableInTouchMode = true
            }
        }

        workManager = WorkManager.getInstance(requireContext())
        if (eventId == null) {
            isAdd = true
        }

        if (isAdd) {
            binding.appBarAddTimeEvent.tvTitleAppBar.text = "Add Time Event"
        } else {
            binding.appBarAddTimeEvent.tvTitleAppBar.text = "Edit Time Event"
        }

        binding.edtEventName.doAfterTextChanged {
            if (hasSetNameAndTime) {
                viewModel.event.value = viewModel.event.value?.apply {
                    nameEvent = it.toString()
                }
            }
        }
//        binding.timePickerAddTimeEvent.setOnTimeChangedListener { view, hourOfDay, minute ->
//            if (hasSetNameAndTime) {
//                 viewModel.event.value = viewModel.event.value?.apply {
//                     this.hour = hourOfDay
//                     this.minute = minute
//                 }
//            }
//        }

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
//        binding.btnChooseRandomSpinner.setOnClickListener {
//            chooseSpinnerDialog.show()
//        }
        binding.btnSpinnerNow.setOnClickListener {
            handleTestNow()
        }
        binding.edtDate.onFocusChangeListener = View.OnFocusChangeListener { view, hasForcus ->
            if (hasForcus){
                dateDialog.show()
                binding.edtDate.isFocusable = false
                binding.edtDate.isFocusableInTouchMode = true
            }
        }
//        binding.btnMemberList2.setOnClickListener {
//            println("Let go member")
//            chooseMemberDialog.show()
//        }

        binding.appBarAddTimeEvent.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        binding.searchViewSpinner.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterSpinner(newText)
                return false
            }
        })
        binding.searchViewMember.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMember(newText)
                return false
            }
        })
    }


    private fun getTimeAndDatePicker() {
        if (!isEventValidate()) {
            return
        }
        progressDialog.show()

        val selectedHour = 0
        val selectedMinutes = 0

        val typeEvent = if (getListDay() == Event().listDay) Constants.ONCE else Constants.EVERY_WEEK

        viewModel.saveEvent(
            channelId,
            viewModel.event.value ?: return
//            Event(
//                eventId!!,
//                typeEvent,
//                selectedHour,
//                selectedMinutes,
//                getListDay(),
//                binding.edtEventName.text.toString()
//            )
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

    private fun filterSpinner(text : String) {
        if (text == "") {
            viewModel.spinnerList.value = viewModel.spinnerList.value
            return
        }
        val list : MutableList<com.example.luckyspinner.models.Spinner> = ArrayList()

        viewModel.spinnerList.value?.forEach {
            if (it.titleSpin.contains(text, true)) {
                list.add(it)
            }
        }
            randomSpinnerAdapter.spinners = list
    }
    private fun filterMember(text: String) {
        if (text == "") {
            viewModel.memberList.value = viewModel.memberList.value
            return
        }
        val list : MutableList<Member> = ArrayList()

        viewModel.memberList.value?.forEach {
            if (it.nameMember.contains(text, true)) {
                list.add(it)
            }
        }
        memberInEventAdapter.members = list
    }
    private fun setUpRecycleView() {
        bindingRandomDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        binding.rvSpinnerList.apply {
            randomSpinnerAdapter = RandomSpinnerListAdapter(this@AddTimeEventFragment, eventId!!)
            adapter = randomSpinnerAdapter
            layoutManager = GridLayoutManager(context, 3 , GridLayoutManager.HORIZONTAL, false)
            addMarginToLastItemHorizontal(binding.rvSpinnerList, 5)
        }

        bindingMemberDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)


        binding.rvMemberList.apply {
            memberInEventAdapter = MemberInEventListAdapter(this@AddTimeEventFragment, eventId!!)
            adapter = memberInEventAdapter
            layoutManager = LinearLayoutManager(context)
            addMarginToLastItem(binding.rvMemberList, 5)
        }

//        binding.viewPagerList.adapter = object : FragmentStateAdapter(requireActivity()) {
//            override fun getItemCount(): Int {
//                return  2
//            }
//            override fun createFragment(position: Int): Fragment {
//                return when (position) {
//                    0 -> SpinnerViewpagerFragment(randomSpinnerAdapter)
//                    1 -> MemberViewpagerFragment(memberInEventAdapter)
//                    else -> SpinnerViewpagerFragment(randomSpinnerAdapter)
//                }
//            }
//        }
//
//        TabLayoutMediator(binding.tabLayoutAddTimeEvent, binding.viewPagerList, object : TabLayoutMediator.TabConfigurationStrategy{
//            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
//                 when(position) {
//                    0 -> {
//                        tab.text = "Spinner"
//                        tab.setIcon(R.drawable.ic_spinner_list)
//                    }
//                    1 -> {
//                        tab.text = "Member"
//                        tab.setIcon(R.drawable.ic_member_list)
//                    }
//                    else -> {}
//                }
//            }
//        }).attach()
    }


    private fun setUpDatePicker() {
        bindingDateDialog = ChooseRandomSpinnerListLayoutBinding.inflate(layoutInflater)
        bindingDateDialog.tvTitleChooseRandomSpinnerList.visibility = View.GONE
        bindingDateDialog.btnBack.visibility = View.GONE
        bindingDateDialog.rvChooseRandomSpinnerList.apply {
            dateAdapter = DateListAdapter(this@AddTimeEventFragment)
            adapter = dateAdapter
            layoutManager = LinearLayoutManager(context)
        }
        bindingDateDialog.btnBack.setOnClickListener {
            dateDialog.dismiss()
        }

        bindingDateDialog.checkBoxAll.setOnClickListener {
            viewModel.event.value = viewModel.event.value?.apply {
                listDay = if (bindingDateDialog.checkBoxAll.isChecked) Constants.LIST_DAY_ALL_WEEK else Constants.LIST_DAY_EMPTY
                println("Here come listday $listDay")
            }
        }
        bindingDateDialog.btnAddElement.isVisible = false

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

        addMarginToLastItem(bindingMemberDialog.rvChooseRandomSpinnerList, 10)

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
//        binding.dayOfWeek.apply {
//            btnMonday.setOnClickListener {
//                viewModel.handleClickDay(Constants.MONDAY_POSITION)
//            }
//
//            btnTuesday.setOnClickListener {
//                viewModel.handleClickDay(Constants.TUESDAY_POSITION)
//            }
//
//            btnWednesday.setOnClickListener {
//                viewModel.handleClickDay(Constants.WEDNESDAY_POSITION)
//            }
//
//            btnThursday.setOnClickListener {
//                viewModel.handleClickDay(Constants.THURSDAY_POSITION)
//            }
//
//            btnFriday.setOnClickListener {
//                viewModel.handleClickDay(Constants.FRIDAY_POSITION)
//            }
//
//            btnSaturday.setOnClickListener {
//                viewModel.handleClickDay(Constants.SATURDAY_POSITION)
//            }
//
//            btnSunday.setOnClickListener {
//                viewModel.handleClickDay(Constants.SUNDAY_POSITION)
//            }
//        }
    }
    fun handleDayOfWeek(event : Event) {
        bindingDateDialog.checkBoxAll.isChecked = event.listDay == Constants.LIST_DAY_ALL_WEEK
        var title = ""
        event.listDay.apply {
            if (contains(Constants.MONDAY)) title += "M "
            if (contains(Constants.TUESDAY)) title += "T "
            if (contains(Constants.WEDNESDAY)) title += "W "
            if (contains(Constants.THURSDAY)) title += "Th "
            if (contains(Constants.FRIDAY)) title += "F "
            if (contains(Constants.SATURDAY)) title += "Sat "
            if (contains(Constants.SUNDAY)) title += "Sun "

            if (this.containsAll(arrayListOf(1, 2, 3, 4, 5, 6, 7))) {
                title = "All week"
            }
        }
        if (title == "") title = "Once time"
        binding.edtDate.hint = title
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
                null,
                null,
                getListDay()
            ),
            true
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
            if (!hasSetNameAndTime) {
                binding.edtEventName.setText(it.nameEvent)
                hasSetNameAndTime = true
            }
            it.hour?.let { eventHour ->
                binding.edtTime.hint = "${numberToMinuteForm(eventHour)} : ${numberToMinuteForm(it.minute!!)}"
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
            if (it.listEvent.contains(eventId)) ++memberCount
        }
        if (memberCount < 2) {
            isValidated = false
            Toast.makeText(context, "You must select at least 2 members",Toast.LENGTH_LONG).show()
        }
        viewModel.spinnerList.value?.forEach {
            if (it.listEvent.contains(eventId)) ++spinnerCount
        }
        if (spinnerCount < 1) {
            isValidated = false
            Toast.makeText(context, "You must select at least 1 spinner",Toast.LENGTH_LONG).show()
        }
        binding.edtEventName.apply {
            if (text.toString().trim() == "") {
                error = "Please fill this fields!"
                isValidated = false
            }
        }
        viewModel.event.value?.hour ?: {
            isValidated = false
            Toast.makeText(context, "You must choose a time!", Toast.LENGTH_LONG).show()
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
        println("Here come daynumber $dayNumber")
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