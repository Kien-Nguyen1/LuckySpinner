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
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.SpinnerListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.FragmentSpinnerListBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.SPINNER_TITLE
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

        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY).toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()

        viewModel.isAddingSpinnerSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Add Spinner Successfully!", Toast.LENGTH_SHORT).show()
                    addSpinnerDiaLog.dismiss()
                }
                else
                {
                    Toast.makeText(context, "Add Spinner Fail!!", Toast.LENGTH_SHORT).show()
                }
                viewModel.isAddingSpinnerSuccess.value = null
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getSpinners(idChannel)
                }
            }
        }

        viewModel.spinnerList.observe(viewLifecycleOwner) {
            spinnerAdapter.spinners = it
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getSpinners(idChannel)
        }

        binding.btnAddSpinner.setOnClickListener {
            openAddSpinnerDiaLog(Gravity.CENTER)
        }

        binding.btnBackSpinnerList.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun openAddSpinnerDiaLog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addSpinnerDiaLog = Dialog(requireContext())
        addSpinnerDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addSpinnerDiaLog.setContentView(binding.root)

        binding.tvAddChannel.text = "Spinner Name"

        val window : Window = addSpinnerDiaLog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addSpinnerDiaLog.show()

        viewModel.contextSpinnerList = requireContext()

        binding.btnDoneAddChannel.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val nameSpinner = binding.edtEnterChannelName.text.toString()
                val idSpinner = binding.edtEnterChannelId.text.toString()
                viewModel.addSpinner(idChannel, idSpinner, nameSpinner)
            }
        }
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

    override fun onItemClick(id: String, title : String) {
        findNavController().navigate(R.id.elementListInSpinnerFragment, Bundle().apply {
            putString(Constants.ID_CHANNEL_KEY, idChannel)
            putString(Constants.ID_SPINNER_KEY, id)
            putString(SPINNER_TITLE, title)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteSpinner(idChannel, id)
        }
    }

}