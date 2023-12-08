package com.example.luckyspinner.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.ElementListInSpinnerAdapter
import com.example.luckyspinner.adapter.SpinnerListAdapter
import com.example.luckyspinner.databinding.FragmentElementListInSpinnerBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.example.luckyspinner.viewmodels.ElementListInSpinnerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ElementListInSpinnerFragment : Fragment(), ElementListInSpinnerAdapter.Listener {
    private val viewModel by viewModels<ElementListInSpinnerViewModel>()
    private lateinit var binding : FragmentElementListInSpinnerBinding
    private lateinit var elementAdapter : ElementListInSpinnerAdapter
    private var idSpinner : String? = null
    private var idChannel : String? = null


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
        idSpinner = arguments?.getString(Constants.ID_SPINNER_KEY)
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY)
        setupRecycleView()
        viewModel.elementList.observe(viewLifecycleOwner) {
            elementAdapter.elementSpinners = it
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getElement(idChannel, idSpinner)
        }


    }

    private fun setupRecycleView() {
        binding.rvElementListInSpinner.apply {
            elementAdapter = ElementListInSpinnerAdapter(this@ElementListInSpinnerFragment)
            adapter = elementAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onItemClick(id: String) {
//        TODO("Not yet implemented")
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteElement(idChannel, idSpinner, id)
        }
    }


}