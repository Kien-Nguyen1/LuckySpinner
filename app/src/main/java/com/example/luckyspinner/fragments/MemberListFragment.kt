package com.example.luckyspinner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.adapter.MemberListAdapter
import com.example.luckyspinner.adapter.SpinnerListAdapter
import com.example.luckyspinner.databinding.FragmentMemberListBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.MemberListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MemberListFragment : Fragment(), MemberListAdapter.Listener {
    private val viewModel by viewModels<MemberListViewModel>()
    private lateinit var binding : FragmentMemberListBinding
    private lateinit var memberAdapter : MemberListAdapter
    private lateinit var idChannel : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMemberListBinding.inflate(inflater , container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY).toString()

        viewModel.memberList.observe(viewLifecycleOwner) {
            memberAdapter.members = it
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getMembers(idChannel)
        }

    }
    private fun setupRecycleView() {
        binding.rvMemberList.apply {
            memberAdapter = MemberListAdapter(this@MemberListFragment)
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onItemClick(id: String) {
        TODO("Not yet implemented")
    }

    override fun onDeleteItem(id: String) {
        TODO("Not yet implemented")
    }
}
