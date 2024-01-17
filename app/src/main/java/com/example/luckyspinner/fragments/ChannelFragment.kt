package com.example.luckyspinner.fragments

import android.app.ActionBar
import android.app.ProgressDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.example.luckyspinner.adapter.EventListAdapter
import com.example.luckyspinner.databinding.FragmentChannelBinding
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.CHANNEL_NAME
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_TELEGRAM_CHANNEL_KEY
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.util.Function.addFabScrollListener
import com.example.luckyspinner.util.Function.hideKeyBoard
import com.example.luckyspinner.util.Function.showKeyBoard
import com.example.luckyspinner.viewmodels.ChannelViewModel
import com.example.luckyspinner.work.SendMessageWorker
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Calendar
import java.util.concurrent.TimeUnit


class ChannelFragment : Fragment(), EventListAdapter.Listener {
    private val viewModel by viewModels<ChannelViewModel>()
    private lateinit var binding: FragmentChannelBinding
    private lateinit var idChannel: String
    private var nameChannel: String? = null
    private var idTelegramChannel: String? = null
    private lateinit var eventAdapter: EventListAdapter
    var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelBinding.inflate(inflater, container, false)

        idChannel = arguments?.getString(ID_CHANNEL_KEY)!!
        nameChannel = arguments?.getString(CHANNEL_NAME)

        idTelegramChannel = arguments?.getString(ID_TELEGRAM_CHANNEL_KEY)

        setupRecycleView()

        setupObserver()

        viewModel.getEvents(idChannel)

        if (isFirstLoad) {
            isFirstLoad = false
        }

        binding.appBarChannel.apply {
            tvTitleAppBar.text = nameChannel
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bundle = Bundle().apply {
            putString(ID_CHANNEL_KEY, idChannel)
        }

        binding.btnAddEventOfChannel.setOnClickListener {
            val direction = ChannelFragmentDirections
                .actionChannelFragmentToAddTimeEventFragment()
                .actionId

            findNavController().navigate(direction, bundle.apply {
                putString(Constants.ID_TELEGRAM_CHANNEL_KEY, idTelegramChannel)
            })
        }

        binding.appBarChannel.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.appBarChannel.apply {
            btnSpinnerList.setOnClickListener {
                val direction = ChannelFragmentDirections
                    .actionChannelFragmentToSpinnerListFragment()
                    .actionId

                findNavController().navigate(direction, bundle)
            }

            btnMemberList.setOnClickListener {
                val direction = ChannelFragmentDirections
                    .actionChannelFragmentToMemberListFragment()
                    .actionId

                findNavController().navigate(direction, bundle)
            }
        }
        handleSearch()

        binding.rvEventListOfChannel.addFabScrollListener(binding.btnAddEventOfChannel)
    }

    fun handleSearch() {
        fun isShowMenu(isShow: Boolean) {
            binding.appBarChannel.apply {
                btnSearch.isVisible = isShow
                tvTitleAppBar.isVisible = isShow
                btnBack.isVisible = isShow
                btnMemberList.isVisible = isShow
                btnSpinnerList.isVisible = isShow
            }
        }

        val searchView = binding.appBarChannel.searchView

        searchView.isVisible = false

        searchView.setOnCloseListener {
            searchView.isVisible = false
            isShowMenu(true)

            false
        }

        searchView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                searchView.isVisible = false
                isShowMenu(true)
            }
        }

        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterEvent(newText)
                return false
            }

        })

        binding.appBarChannel.btnSearch.setOnClickListener {
            searchView.showContextMenu()
            searchView.isVisible = true
            searchView.setIconifiedByDefault(true);
            searchView.queryHint = "Search Event..."

            searchView.isFocusable = true;
            searchView.isIconified = false;
            isShowMenu(false)
        }
    }

    fun filterEvent(text: String) {
        if (!viewModel.eventList.isInitialized) return
        val list = viewModel.eventList.value!!.filter {
            it.nameEvent.contains(text)
        }
        eventAdapter.events = list
    }

    private fun setupRecycleView() {
        binding.rvEventListOfChannel.apply {
            eventAdapter = EventListAdapter(this@ChannelFragment)
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
            Function.addMarginToLastItem(binding.rvEventListOfChannel, 10)
        }
    }

    fun setupObserver() {
        viewModel.eventList.observe(viewLifecycleOwner) { eventList ->
            eventAdapter.events = eventList
            if (eventList.isEmpty()) {
                binding.rvEventListOfChannel.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvEventListOfChannel.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE
            }
        }
        viewModel.isDeleteEventSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Delete success!", Toast.LENGTH_SHORT).show()
                    viewModel.getEvents(idChannel)
                } else {
                    Toast.makeText(context, "Delete failed!", Toast.LENGTH_LONG).show()
                }
            }
        }

        WorkManager.getInstance().getWorkInfosByTagLiveData(idChannel)
            .observe(viewLifecycleOwner) {
                viewModel.getEvents(idChannel)
            }
    }

    override fun onItemClick(id: String) {
        val direction = ChannelFragmentDirections
            .actionChannelFragmentToAddTimeEventFragment()
            .actionId

        findNavController().navigate(direction, Bundle().apply {
            putString(ID_CHANNEL_KEY, idChannel)
            putString(ID_TELEGRAM_CHANNEL_KEY, idTelegramChannel)
            putString(Constants.ID_EVENT_KEY, id)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch {
            val isDelete = DialogUtil.showYesNoDialog(context)
            if (isDelete) {
                viewModel.deleteEvent(idChannel, id)
            }
        }
    }

    override fun onSwitchClick(id: String, event: Event) {
        val workManager = WorkManager.getInstance()

        val timeNow = Calendar.getInstance()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, event.hour!!)
        calendar.set(Calendar.MINUTE, event.minute!!)


        val durationDiff =
            if (calendar.get(Calendar.HOUR_OF_DAY) == timeNow.get(Calendar.HOUR_OF_DAY) && timeNow.get(
                    Calendar.MINUTE
                ) == calendar.get(Calendar.MINUTE)
            ) {
                Duration.ZERO
            } else if (calendar > timeNow) {
                Duration.between(timeNow.toInstant(), calendar.toInstant())
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                Duration.between(timeNow.toInstant(), calendar.toInstant())
            }

        if (event.isTurnOn) { // on at this moment
            workManager.cancelUniqueWork(id)
        } else {
            val constraints = Constraints(
                requiredNetworkType = NetworkType.CONNECTED,
                requiresBatteryNotLow = true
            )

            workManager.apply {
                val data = workDataOf(
                    ID_TELEGRAM_CHANNEL_KEY to idTelegramChannel,
                    ID_CHANNEL_KEY to idChannel,
                    Constants.ID_EVENT_KEY to id,
                    Constants.DEVICE_ID_KEY to Constants.DEVICE_ID
                )
                if (event.typeEvent == Constants.EVERY_WEEK) {
                    val workRequest =
                        PeriodicWorkRequestBuilder<SendMessageWorker>(1, TimeUnit.DAYS)
                            .setInitialDelay(durationDiff)
                            .setInputData(data)
                            .setConstraints(constraints)
                            .addTag(idChannel)
                            .build()

                    enqueueUniquePeriodicWork(
                        id,
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        workRequest
                    )
                } else if (event.typeEvent == Constants.ONCE) {
                    val workRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
                        .setInitialDelay(durationDiff)
                        .setInputData(data)
                        .setConstraints(constraints)
                        .addTag(idChannel)
                        .build()

                    enqueueUniqueWork(
                        id,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }

            }
        }
        viewModel.saveEvent(idChannel, event.apply {
            isTurnOn = !isTurnOn
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        val list: MutableList<MutableLiveData<*>> = ArrayList<MutableLiveData<*>>().apply {
            add(viewModel.isDeleteEventSuccess)
        }
        Function.toNull(list)
        list.add(viewModel.eventList)
        Function.removeObservers(list, viewLifecycleOwner)
    }
}