package com.example.luckyspinner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.ChannelListAdapter
import com.example.luckyspinner.adapter.SpinnerListAdapter
import com.example.luckyspinner.databinding.FragmentSpinnerListBinding
import com.example.luckyspinner.viewmodels.SpinnerListViewModel


class SpinnerListFragment : Fragment(), SpinnerListAdapter.ListFoodListener {
    private val viewModel by viewModels<SpinnerListViewModel>()
    private lateinit var binding : FragmentSpinnerListBinding
    private lateinit var spinnerAdapter : SpinnerListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSpinnerListBinding.inflate(inflater , container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()

        viewModel.spinnerList.observe(viewLifecycleOwner) {
            spinnerAdapter.spinners = it
        }


    }
    private fun setupRecycleView() {
        binding.rvSpinnerList.apply {
            spinnerAdapter = SpinnerListAdapter(this@SpinnerListFragment)
            adapter = spinnerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onItemClick(id: String) {
        findNavController().navigate(R.id.elementListInSpinnerFragment, Bundle().apply {
            putString("KEY", id)
        })
    }

}