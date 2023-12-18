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
import com.example.luckyspinner.adapter.ElementListInSpinnerAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.FragmentElementListInSpinnerBinding
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_SPINNER_KEY
import com.example.luckyspinner.util.Constants.SPINNER_TITLE
import com.example.luckyspinner.viewmodels.ElementListInSpinnerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ElementListInSpinnerFragment : Fragment(), ElementListInSpinnerAdapter.Listener {
    private val viewModel by viewModels<ElementListInSpinnerViewModel>()
    private lateinit var binding : FragmentElementListInSpinnerBinding
    private lateinit var elementAdapter : ElementListInSpinnerAdapter
    private var idSpinner : String? = null
    private var titleSpinner : String? = null
    private var idChannel : String? = null
    private lateinit var addElementInSpinnerDiaLog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentElementListInSpinnerBinding.inflate(inflater, container, false)
        idChannel = arguments?.getString(ID_CHANNEL_KEY)
        idSpinner = arguments?.getString(ID_SPINNER_KEY)
        titleSpinner = arguments?.getString(SPINNER_TITLE)

        binding.toolBarElementListSpinner.title = titleSpinner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        viewModel.elementList.observe(viewLifecycleOwner) {
            elementAdapter.elementSpinners = it
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getElement(idChannel, idSpinner)
        }

        binding.toolBarElementListSpinner.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddElementListInSpinner.setOnClickListener {
            openAddElementInSpinnerDiaLog(Gravity.CENTER)
        }
    }

    private fun openAddElementInSpinnerDiaLog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addElementInSpinnerDiaLog = Dialog(requireContext())
        addElementInSpinnerDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addElementInSpinnerDiaLog.setContentView(binding.root)

        binding.tvAddChannel.text = "Tên phần tử"

        val window : Window = addElementInSpinnerDiaLog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addElementInSpinnerDiaLog.show()
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