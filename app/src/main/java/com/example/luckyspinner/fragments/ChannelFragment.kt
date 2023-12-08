package com.example.luckyspinner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.EventListAdapter
import com.example.luckyspinner.databinding.FragmentChannelBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.ChannelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChannelFragment : Fragment(), EventListAdapter.Listener {
    private val viewModel by viewModels<ChannelViewModel>()
    private lateinit var binding : FragmentChannelBinding
    private var idChannel : String? = null
    private lateinit var eventAdapter : EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelBinding.inflate(inflater, container, false)
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        viewModel.channelList.observe(viewLifecycleOwner){
            eventAdapter.events = it
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getEvents(idChannel)
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
        findNavController().navigate(R.id.memberListFragment, Bundle().apply {
            putString(Constants.ID_CHANNEL_KEY, idChannel)
            putString(Constants.ID_EVENT_KEY, id)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteChannel(idChannel, id)
        }
    }


}