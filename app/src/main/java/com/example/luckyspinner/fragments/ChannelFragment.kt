package com.example.luckyspinner.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.EventListAdapter
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.databinding.FragmentChannelBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.CHANNEL_NAME
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_TELEGRAM_CHANNEL_KEY
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.viewmodels.ChannelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChannelFragment : Fragment(), EventListAdapter.Listener {
    private val viewModel by viewModels<ChannelViewModel>()
    private lateinit var binding: FragmentChannelBinding
    private lateinit var idChannel: String
    private var nameChannel: String? = null
    private var idTelegramChannel: String? = null
    private lateinit var eventAdapter : EventListAdapter
    private lateinit var progressDialog : ProgressDialog
    var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        println("channelss oncreateview")
        idChannel = arguments?.getString(ID_CHANNEL_KEY)!!
        nameChannel = arguments?.getString(CHANNEL_NAME)


        binding = FragmentChannelBinding.inflate(inflater, container, false)
        progressDialog = ProgressDialog(context)
        idTelegramChannel = arguments?.getString(ID_TELEGRAM_CHANNEL_KEY)
        setupRecycleView()

        setupObserver()

        if (isFirstLoad) {
            viewModel.getEvents(idChannel)
            isFirstLoad = false
        }

        binding.appBarChannel.apply {
            toolBar.title = nameChannel
        }

        println("channelss oncreateview")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("channelss onviewcreated")


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

        binding.appBarChannel.apply {
            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.appBarChannel.apply {
            toolBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.spinnerListFragment -> {
                        val direction = ChannelFragmentDirections
                            .actionChannelFragmentToSpinnerListFragment()
                            .actionId

                        findNavController().navigate(direction, bundle)
                        true
                    }

                    R.id.memberListFragment -> {
                        val direction = ChannelFragmentDirections
                            .actionChannelFragmentToMemberListFragment()
                            .actionId

                        findNavController().navigate(direction, bundle)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupRecycleView() {
        binding.rvEventListOfChannel.apply {
            eventAdapter = EventListAdapter(this@ChannelFragment)
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    fun setupObserver() {
        viewModel.channelList.observe(viewLifecycleOwner) {
            eventAdapter.events = it
            eventAdapter.notifyDataSetChanged()
            if (it.isEmpty()) {
                binding.rvEventListOfChannel.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvEventListOfChannel.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE
            }
        }
        viewModel.isShowProgressDialog.observe(viewLifecycleOwner) {
            if (it) progressDialog.show()
            else progressDialog.dismiss()
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

    override fun onDestroyView() {
        super.onDestroyView()
        val list : MutableList<MutableLiveData<*>> = ArrayList<MutableLiveData<*>>().apply {
            add(viewModel.isDeleteEventSuccess)
        }
        Function.toNull(list)
        list.add(viewModel.channelList)
        Function.removeObservers(list, viewLifecycleOwner)
    }
}