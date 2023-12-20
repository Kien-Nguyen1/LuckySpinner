package com.example.luckyspinner.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.EventListAdapter
import com.example.luckyspinner.databinding.FragmentChannelBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.CHANNEL_NAME
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_TELEGRAM_CHANNEL_KEY
import com.example.luckyspinner.viewmodels.ChannelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChannelFragment : Fragment(), EventListAdapter.Listener {
    private val viewModel by viewModels<ChannelViewModel>()
    private lateinit var binding: FragmentChannelBinding
    private var idChannel: String? = null
    private var nameChannel: String? = null
    private var idTelegramChannel: String? = null
    private lateinit var eventAdapter: EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelBinding.inflate(inflater, container, false)
        idChannel = arguments?.getString(ID_CHANNEL_KEY)
        nameChannel = arguments?.getString(CHANNEL_NAME)
        idTelegramChannel = arguments?.getString(ID_TELEGRAM_CHANNEL_KEY)

        binding.appBarChannel.apply {
            toolBar.title = nameChannel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        viewModel.channelList.observe(viewLifecycleOwner) {
            eventAdapter.events = it
            if (it.isEmpty()) {
                binding.rvEventListOfChannel.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvEventListOfChannel.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getEvents(idChannel)
        }

        val bundle = Bundle().apply {
            putString(ID_CHANNEL_KEY, idChannel)
        }

        binding.btnAddEventOfChannel.setOnClickListener {
            val direction = ChannelFragmentDirections
                .actionChannelFragmentToAddTimeEventFragment()
                .actionId

            findNavController().navigate(direction, bundle)
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

    override fun onItemClick(id: String) {
        val direction = ChannelFragmentDirections
            .actionChannelFragmentToAddTimeEventFragment()
            .actionId

        findNavController().navigate(direction, Bundle().apply {
            putString(ID_CHANNEL_KEY, idChannel)
            putString(ID_TELEGRAM_CHANNEL_KEY, nameChannel)
            putString(Constants.ID_EVENT_KEY, id)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteChannel(idChannel, id)
        }
    }
}