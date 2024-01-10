package com.example.luckyspinner.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.SpinnerListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.EditDialogBinding
import com.example.luckyspinner.databinding.FragmentSpinnerListBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_SPINNER_KEY
import com.example.luckyspinner.util.Constants.SPINNER_TITLE
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.util.Function.addFabScrollListener
import com.example.luckyspinner.viewmodels.SpinnerListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar


class SpinnerListFragment : Fragment(), SpinnerListAdapter.Listener {
    private val viewModel by viewModels<SpinnerListViewModel>()
    private lateinit var binding : FragmentSpinnerListBinding
    private lateinit var spinnerAdapter : SpinnerListAdapter
    private lateinit var idChannel : String
    private lateinit var addSpinnerDiaLog : Dialog
    private lateinit var editSpinnerDiaLog : Dialog
    private lateinit var progressDialog : Dialog
    var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSpinnerListBinding.inflate(inflater , container, false)
        progressDialog = ProgressDialog(context)

        idChannel = arguments?.getString(ID_CHANNEL_KEY).toString()

        binding.appBarSpinnerList.apply {
            tvTitleAppBar.text = "Spinner List"
            btnSpinnerList.visibility = View.GONE
            btnMemberList.visibility = View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        setupObservers()

        lifecycleScope.launch(Dispatchers.Main) {
            if (isFirstLoad) {
                viewModel.getEvents(idChannel)
                viewModel.getSpinners(idChannel)
                isFirstLoad = false
            }
        }

        binding.btnAddSpinner.setOnClickListener {
            openAddSpinnerDiaLog(Gravity.CENTER)
        }

        binding.appBarSpinnerList.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.rvSpinnerList.addFabScrollListener(binding.btnAddSpinner)
    }

    fun createEditListener() {
        spinnerAdapter.onEditClickListener = object : OnEditClickListener {
            override fun onEditClick(position: Int) {
                val binding : EditDialogBinding = EditDialogBinding.inflate(layoutInflater)
                editSpinnerDiaLog = Dialog(requireContext())
                editSpinnerDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editSpinnerDiaLog.setContentView(binding.root)

                val spinner = spinnerAdapter.spinners[position]

                binding.tvNameTitleAddElement.text = "Edit Spinner"

                binding.edtEnterElement.setText(spinner.titleSpin)
                binding.edtId.isVisible = false

                binding.edtEnterElement.setSelection(binding.edtEnterElement.text.length)

                binding.btnDoneAddElement.setOnClickListener {
                    if (binding.edtEnterElement.text.toString().isEmpty()) {
                        binding.edtEnterElement.error = "Please fill this field!"
                        return@setOnClickListener
                    }
                    spinner.titleSpin = binding.edtEnterElement.text.toString()
                    viewModel.editSpinner(idChannel, spinner)
                }

                binding.btnCancelElement.setOnClickListener {
                    editSpinnerDiaLog.dismiss()
                }

                val window : Window = editSpinnerDiaLog.window!!
                window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val windowAttribute : WindowManager.LayoutParams = window.attributes
                windowAttribute.gravity = Gravity.CENTER
                window.attributes = windowAttribute

                editSpinnerDiaLog.show()
                lifecycleScope.launch {
                    delay(1)
                    Function.showKeyBoard(requireActivity(), binding.edtEnterElement)
                }
            }
        }
    }

    private fun openAddSpinnerDiaLog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addSpinnerDiaLog = Dialog(requireContext())
        addSpinnerDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addSpinnerDiaLog.setContentView(binding.root)

        binding.tvAddChannel.text = "Spinner Name"
        binding.edtEnterChannelId.visibility = View.GONE

        val window : Window = addSpinnerDiaLog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addSpinnerDiaLog.show()


        binding.btnDoneAddChannel.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                val nameSpinner = binding.edtEnterChannelName.text.toString().trim()
                if (nameSpinner == EMPTY_STRING) {
                    binding.edtEnterChannelName.error = "Please fill this field"
                    return@launch
                }
                val idSpinner = Calendar.getInstance().timeInMillis.toString()
                viewModel.addSpinner(idChannel, Spinner(idSpinner, nameSpinner))
            }
        }
        binding.btnCancelAddChannel.setOnClickListener {
            addSpinnerDiaLog.dismiss()
        }

        lifecycleScope.launch {
            delay(1)
            Function.showKeyBoard(requireActivity(), binding.edtEnterChannelName)
        }
    }

    private fun setupRecycleView() {
        binding.rvSpinnerList.apply {
            spinnerAdapter = SpinnerListAdapter(this@SpinnerListFragment)
            createEditListener()
            adapter = spinnerAdapter
            layoutManager = LinearLayoutManager(context)
            Function.addMarginToLastItem(binding.rvSpinnerList, 10)
        }
        viewModel.eventList.observe(viewLifecycleOwner) {
            binding.rvSpinnerList.apply {
                spinnerAdapter = SpinnerListAdapter(this@SpinnerListFragment, it)
                createEditListener()
                adapter = spinnerAdapter
                layoutManager = LinearLayoutManager(context)
                if (viewModel.spinnerList.isInitialized) {
                    viewModel.spinnerList.value = viewModel.spinnerList.value
                }
            }
        }
    }
    fun setupObservers() {
        viewModel.isEditingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Edit Spinner Successfully!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "Edit Spinner Fail!!", Toast.LENGTH_SHORT).show()
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.getSpinners(idChannel)
                }            }
        }

        viewModel.isDeletingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Delete Spinner Successfully!", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "Delete Spinner Fail!!", Toast.LENGTH_SHORT).show()
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.getSpinners(idChannel)
                }            }
        }

        viewModel.isAddingSpinnerSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Add Spinner Successfully!", Toast.LENGTH_SHORT).show()
                    addSpinnerDiaLog.dismiss()
                }
                else {
                    Toast.makeText(context, "Add Spinner Fail!!", Toast.LENGTH_SHORT).show()
                }
                viewModel.isAddingSpinnerSuccess.value = null
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.getSpinners(idChannel)
                }
            }
        }

        viewModel.spinnerList.observe(viewLifecycleOwner) {
            spinnerAdapter.spinners = it
            spinnerAdapter.notifyDataSetChanged()
            if (it.isEmpty()) {
                binding.rvSpinnerList.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvSpinnerList.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE
            }
        }
        viewModel.isShowProgressDialog.observe(viewLifecycleOwner) {
            if (it) progressDialog.show()
            else progressDialog.dismiss()
        }
    }

    override fun onItemClick(id: String, title : String) {
        val direction = SpinnerListFragmentDirections
            .actionSpinnerListFragmentToElementListInSpinnerFragment()
            .actionId

        val bundle = Bundle().apply {
            putString(ID_CHANNEL_KEY, idChannel)
            putString(ID_SPINNER_KEY, id)
            putString(SPINNER_TITLE, title)
        }

        findNavController().navigate(direction, bundle)
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val isDelete = DialogUtil.showYesNoDialog(context)
            if (isDelete) {
                progressDialog.show()
                viewModel.deleteSpinner(idChannel, id)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val list : MutableList<MutableLiveData<*>> = ArrayList<MutableLiveData<*>>().apply {
            add(viewModel.isEditingSuccess)
            add(viewModel.isDeletingSuccess)
            add(viewModel.isAddingSpinnerSuccess)
        }
        Function.toNull(list)
        list.add(viewModel.spinnerList)
        Function.removeObservers(list, viewLifecycleOwner)
    }

}