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
import com.example.luckyspinner.viewmodels.ChannelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChannelFragment : Fragment(), EventListAdapter.Listener {
    private val viewModel by viewModels<ChannelViewModel>()
    private lateinit var binding : FragmentChannelBinding
    private var idChannel : String? = null
    private var nameChannel : String? = null
    private lateinit var eventAdapter : EventListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelBinding.inflate(inflater, container, false)
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY)
        nameChannel = arguments?.getString(CHANNEL_NAME)

        binding.tvTitleChannelFragment.text = nameChannel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        viewModel.eventList.observe(viewLifecycleOwner){
            eventAdapter.events = it
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getEvents(idChannel)
        }

        binding.btnAddEventOfChannel.setOnClickListener {
            findNavController().navigate(R.id.addTimeEventFragment, Bundle().apply {
                putString(ID_CHANNEL_KEY, idChannel)
            })
        }

        binding.btnSpinnerList.setOnClickListener {
            findNavController().navigate(R.id.spinnerListFragment, Bundle().apply {
                putString(ID_CHANNEL_KEY, idChannel)
            })
        }

        binding.btnMemberListChannel.setOnClickListener {
            findNavController().navigate(R.id.memberListFragment, Bundle().apply {
                putString(ID_CHANNEL_KEY, idChannel)
            })
        }

//        binding.btnBackChannelFragment.setOnClickListener {
//            findNavController().popBackStack()
//        }
    }

    private fun setupRecycleView() {
        val itemDecoration : RecyclerView.ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.rvEventListOfChannel.apply {
            eventAdapter = EventListAdapter(this@ChannelFragment)
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(itemDecoration)
        }
    }

    override fun onItemClick(id: String) {
        findNavController().navigate(R.id.editTimeEventFragment, Bundle().apply {
            putString(ID_CHANNEL_KEY, idChannel)
            putString(Constants.ID_EVENT_KEY, id)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteEvent(idChannel, id)
        }
    }
}