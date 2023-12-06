package com.example.luckyspinner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.luckyspinner.R
import com.example.luckyspinner.viewmodels.EditTimeEventViewModel


class EditTimeEventFragment : Fragment() {
    private val viewModel by viewModels<EditTimeEventViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_time_event, container, false)
    }

}