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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.ElementListInSpinnerAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.EditDialogBinding
import com.example.luckyspinner.databinding.FragmentElementListInSpinnerBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.ElementSpinner
import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_SPINNER_KEY
import com.example.luckyspinner.util.Constants.SPINNER_TITLE
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.util.Function.addFabScrollListener
import com.example.luckyspinner.viewmodels.ElementListInSpinnerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar


class ElementListInSpinnerFragment : Fragment(), ElementListInSpinnerAdapter.Listener {
    private val viewModel by viewModels<ElementListInSpinnerViewModel>()
    private lateinit var binding : FragmentElementListInSpinnerBinding
    private lateinit var elementAdapter : ElementListInSpinnerAdapter
    private lateinit var idSpinner : String
    private var titleSpinner : String? = null
    private lateinit var idChannel : String
    private lateinit var addElementInSpinnerDiaLog : Dialog
    private lateinit var editElementDialog : Dialog
    private lateinit var progressDialog : Dialog
    var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentElementListInSpinnerBinding.inflate(inflater, container, false)
        progressDialog = ProgressDialog(context)

        idChannel = arguments?.getString(ID_CHANNEL_KEY)!!
        idSpinner = arguments?.getString(ID_SPINNER_KEY)!!
        titleSpinner = arguments?.getString(SPINNER_TITLE)

        binding.appBarElementListSpinner.apply {
            tvTitleAppBar.text = titleSpinner
            btnSpinnerList.visibility = View.GONE
            btnMemberList.visibility = View.GONE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setupRecycleView()

        elementAdapter.onEditClickListener = object : OnEditClickListener {
            override fun onEditClick(position: Int) {
                val binding : EditDialogBinding = EditDialogBinding.inflate(layoutInflater)

                editElementDialog = Dialog(requireContext())
                editElementDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editElementDialog.setContentView(binding.root)

                val element = elementAdapter.elementSpinners[position]

                binding.tvNameTitleAddElement.text = "Edit Element"
                binding.edtId.isVisible = false
                binding.edtEnterElement.setText(element.nameElement)

                binding.edtEnterElement.setSelection(binding.edtEnterElement.text.length)


                binding.btnDoneAddElement.setOnClickListener {
                    if (binding.edtEnterElement.text.toString().trim().isEmpty()) {
                        binding.edtEnterElement.error = "Please fill this field"
                        return@setOnClickListener
                    }
                    element.nameElement = binding.edtEnterElement.text.toString()
                    viewModel.editElement(idChannel, idSpinner, element)
                }
                binding.btnCancelElement.setOnClickListener {
                    editElementDialog.dismiss()
                }

                val window : Window = editElementDialog.window!!
                window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val windowAttribute : WindowManager.LayoutParams = window.attributes
                windowAttribute.gravity = Gravity.CENTER
                window.attributes = windowAttribute

                editElementDialog.show()
                lifecycleScope.launch {
                    delay(1)
                    Function.showKeyBoard(context, binding.edtEnterElement)
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (isFirstLoad) {
                viewModel.getElement(idChannel, idSpinner)
                isFirstLoad = false
            }
        }

        binding.appBarElementListSpinner.apply {
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.btnAddElementListInSpinner.setOnClickListener {
            openAddElementInSpinnerDiaLog(Gravity.CENTER)
        }

        binding.rvElementListInSpinner.addFabScrollListener(binding.btnAddElementListInSpinner)
    }

    private fun openAddElementInSpinnerDiaLog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addElementInSpinnerDiaLog = Dialog(requireContext())
        addElementInSpinnerDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addElementInSpinnerDiaLog.setContentView(binding.root)

        binding.tvAddChannel.text = "Tên phần tử"
        binding.edtEnterChannelId.isVisible = false

        val window : Window = addElementInSpinnerDiaLog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addElementInSpinnerDiaLog.show()
        binding.btnDoneAddChannel.setOnClickListener {
            if (binding.edtEnterChannelName.text.toString().trim() == EMPTY_STRING) {
                binding.edtEnterChannelName.error = "Please fill this field"
                return@setOnClickListener
            }
            viewModel.addElement(idChannel,
                idSpinner,
                ElementSpinner(
                Calendar.getInstance().timeInMillis.toString(),
                binding.edtEnterChannelName.text.toString()
            ))
        }
        binding.btnCancelAddChannel.setOnClickListener {
            addElementInSpinnerDiaLog.dismiss()
        }
        lifecycleScope.launch {
            delay(1)
            Function.showKeyBoard(context, binding.edtEnterChannelId)
        }
    }

    private fun setupRecycleView() {
        binding.rvElementListInSpinner.apply {
            elementAdapter = ElementListInSpinnerAdapter(this@ElementListInSpinnerFragment)
            adapter = elementAdapter
            layoutManager = LinearLayoutManager(context)
            Function.addMarginToLastItem(binding.rvElementListInSpinner, 16)
        }
    }

    fun setupObserver() {
        viewModel.elementList.observe(viewLifecycleOwner) {
            elementAdapter.elementSpinners = it
            elementAdapter.notifyDataSetChanged()
            if (it.isEmpty()) {
                binding.rvElementListInSpinner.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvElementListInSpinner.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE
            }
        }
        viewModel.isDeleteSuccess.observe(viewLifecycleOwner) {
            println("Here the observer delete come")
            it?.let {
                if(it) {
                    Toast.makeText(context, "Deleted Channel Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Delete Channel Fail!!", Toast.LENGTH_SHORT).show()
                }
                viewModel.isDeleteSuccess.value = null
                viewModel.getElement(idChannel, idSpinner)
            }
        }
        viewModel.isEditingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    editElementDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Edit failed!", Toast.LENGTH_SHORT).show()
                }
                viewModel.getElement(idChannel, idSpinner)
            }
        }
        viewModel.isAddingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Add successful!", Toast.LENGTH_SHORT).show()
                    addElementInSpinnerDiaLog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Add failed!", Toast.LENGTH_SHORT).show()
                }
                viewModel.getElement(idChannel, idSpinner)
            }
        }
        viewModel.isShowProgressDialog.observe(viewLifecycleOwner) {
            if (it) progressDialog.show()
            else progressDialog.dismiss()
        }
    }

    override fun onItemClick(id: String) {
//        TODO("Not yet implemented")
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val isDelete = DialogUtil.showYesNoDialog(context)
            if (isDelete) {
                viewModel.deleteElement(idChannel, idSpinner, id)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val list : MutableList<MutableLiveData<*>> = ArrayList<MutableLiveData<*>>().apply {
            add(viewModel.isAddingSuccess)
            add(viewModel.isEditingSuccess)
            add(viewModel.isDeleteSuccess)
        }
        Function.toNull(list)
        list.add(viewModel.elementList)
        Function.removeObservers(list, viewLifecycleOwner)
        }
}