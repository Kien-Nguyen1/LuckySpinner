package com.example.luckyspinner.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.SpinnerListAdapter
import com.example.luckyspinner.databinding.AddElementLayoutBinding
import com.example.luckyspinner.databinding.FragmentSpinnerListBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.SpinnerListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SpinnerListFragment : Fragment(), SpinnerListAdapter.Listener {
    private val viewModel by viewModels<SpinnerListViewModel>()
    private lateinit var binding : FragmentSpinnerListBinding
    private lateinit var spinnerAdapter : SpinnerListAdapter
    private lateinit var idChannel : String
    private lateinit var addSpinnerDiaLog : Dialog

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
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY).toString()

        viewModel.spinnerList.observe(viewLifecycleOwner) {
            spinnerAdapter.spinners = it
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getSpinners(idChannel)
        }

        binding.btnAddSpinner.setOnClickListener {
            openAddSpinnerDiaLog(Gravity.CENTER)
        }
    }

    private fun openAddSpinnerDiaLog(gravity: Int) {
        val binding : AddElementLayoutBinding = AddElementLayoutBinding.inflate(layoutInflater)
        addSpinnerDiaLog = Dialog(requireContext())
        addSpinnerDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addSpinnerDiaLog.setContentView(binding.root)

        binding.tvNameTitleAddElement.text = "Spinner Name"

        val window : Window = addSpinnerDiaLog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addSpinnerDiaLog.show()
    }

    private fun setupRecycleView() {
        val itemDecoration : RecyclerView.ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.rvSpinnerList.apply {
            spinnerAdapter = SpinnerListAdapter(this@SpinnerListFragment)
            adapter = spinnerAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(itemDecoration)
        }
    }

    override fun onItemClick(id: String) {
        findNavController().navigate(R.id.elementListInSpinnerFragment, Bundle().apply {
            putString(Constants.ID_CHANNEL_KEY, idChannel)
            putString(Constants.ID_SPINNER_KEY, id)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteSpinner(idChannel, id)
        }
    }

}