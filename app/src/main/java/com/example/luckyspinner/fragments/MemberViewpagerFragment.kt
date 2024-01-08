package com.example.luckyspinner.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.adapter.MemberInEventListAdapter
import com.example.luckyspinner.adapter.RandomSpinnerListAdapter
import com.example.luckyspinner.databinding.FragmentListItemViewpagerBinding

class MemberViewpagerFragment(var adapter : MemberInEventListAdapter) : Fragment(){
    lateinit var binding : FragmentListItemViewpagerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListItemViewpagerBinding.inflate(layoutInflater, container , false)

        binding.rvList.adapter = adapter
        binding.rvList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        println("Here come spinner pager")


        return binding.root
    }
}