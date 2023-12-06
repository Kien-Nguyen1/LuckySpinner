package com.example.luckyspinner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.ChannelListAdapter
import com.example.luckyspinner.databinding.FragmentChannelListBinding
import com.example.luckyspinner.viewmodels.ChannelListViewModel

class ChannelListFragment : Fragment() {
    private lateinit var binding : FragmentChannelListBinding
    private val viewModel : ChannelListViewModel by viewModels()
    private lateinit var channelAdapter : ChannelListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelListBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        viewModel.channelList.observe(viewLifecycleOwner) {
            channelAdapter.channels = it
        }


    }

    private fun setupRecycleView() {
        binding.rvChannel.apply {
            channelAdapter = ChannelListAdapter()
            adapter = channelAdapter
            layoutManager = LinearLayoutManager(context)
        }

    }
}