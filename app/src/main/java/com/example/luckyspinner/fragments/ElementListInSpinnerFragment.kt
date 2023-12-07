package com.example.luckyspinner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.luckyspinner.R
import com.example.luckyspinner.databinding.FragmentElementListInSpinnerBinding
import com.example.luckyspinner.viewmodels.ElementListInSpinnerViewModel


class ElementListInSpinnerFragment : Fragment() {
    private val viewModel by viewModels<ElementListInSpinnerViewModel>()
    private lateinit var binding : FragmentElementListInSpinnerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentElementListInSpinnerBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idSpinner = arguments?.getString("KEY")



    }


}