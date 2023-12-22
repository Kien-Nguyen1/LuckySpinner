package com.example.luckyspinner.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.example.luckyspinner.adapter.ChannelListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.EditDialogBinding
import com.example.luckyspinner.databinding.FragmentChannelListBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants.CHANNEL_NAME
import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.example.luckyspinner.util.Constants.ID_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.ID_TELEGRAM_CHANNEL_KEY
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.viewmodels.ChannelListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ChannelListFragment : Fragment(), ChannelListAdapter.Listener {
    private lateinit var binding : FragmentChannelListBinding
    private val viewModel : ChannelListViewModel by viewModels()
    private lateinit var channelListAdapter : ChannelListAdapter
    private lateinit var addDialog : Dialog
    private lateinit var editChannelDiaLog : Dialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        println("HHere come oncreateview")
        binding = FragmentChannelListBinding.inflate(inflater, container, false)
        progressDialog = ProgressDialog(context)
        setupRecycleView()
        viewModel.channelList.observe(viewLifecycleOwner) {
            channelListAdapter.channels = it
            if (it.isEmpty()) {
                binding.rvChannelList.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvChannelList.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE

            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStateObserver()

        viewModel.message.observe(viewLifecycleOwner) {
//            Toast.makeText(requireContext(), it , Toast.LENGTH_SHORT).show()
        }
        viewModel.getChannels()


        binding.btnAddChannel.setOnClickListener {
            Log.d("kien", "click add channel")
            openAddChannelDialog(Gravity.CENTER)
        }

        channelListAdapter.onEditClickListener = object : OnEditClickListener {
            override fun onEditClick(position: Int) {
                val binding: EditDialogBinding = EditDialogBinding.inflate(layoutInflater)
                editChannelDiaLog = Dialog(requireContext())
                editChannelDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editChannelDiaLog.setContentView(binding.root)

                val channel = channelListAdapter.channels[position]

                binding.tvNameTitleAddElement.text = "Edit Channel"
                binding.edtEnterElement.setText(channel.nameChannel)

                binding.btnDoneAddElement.setOnClickListener {
                    channel.nameChannel = binding.edtEnterElement.text.toString()
                    viewModel.editChannel(channel)
                    editChannelDiaLog.dismiss()
                }
                binding.btnDeleteElement.setOnClickListener {
                    viewModel.deleteChannel(channel.idChannel)
                }

                val window: Window = editChannelDiaLog.window!!
                window.setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val windowAttribute: WindowManager.LayoutParams = window.attributes
                windowAttribute.gravity = Gravity.CENTER
                window.attributes = windowAttribute

                editChannelDiaLog.show()
            }
        }
    }

    private fun openAddChannelDialog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addDialog = Dialog(requireContext())

        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addDialog.setContentView(binding.root)

        val window : Window = addDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addDialog.show()


        binding.btnDoneAddChannel.setOnClickListener {
            val channelTelegramId = binding.edtEnterChannelId.text.toString()
            val channelName = binding.edtEnterChannelName.text.toString()
            var isValidated = true
            binding.edtEnterChannelName.apply {
                if (text.toString() == EMPTY_STRING) {
                    error = "Please fill your channel name!"
                    isValidated = false
                }
            }
            if (channelTelegramId == EMPTY_STRING) {
                binding.edtEnterChannelId.error = "Please fill your TelegramId of channel/group to receive the bot message!"
                isValidated = false
            }
            if (!isValidated) {
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                Log.d("kien", "click done add channel")
                val autoId = Calendar.getInstance().timeInMillis
                val channel = Channel(autoId.toString(), channelTelegramId, channelName)
                viewModel.addChannel(channel)
            }
        }
    }

    fun setupStateObserver() {
        viewModel.isAddingSuccess.observe(viewLifecycleOwner) {
            println("Here come adding")
            it?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Add successful!", Toast.LENGTH_SHORT).show()
                    addDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Add failed!", Toast.LENGTH_SHORT).show()
                }
                viewModel.isAddingSuccess.value = null
                viewModel.getChannels()
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
                viewModel.getChannels()
            }
        }
        viewModel.isEditingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    editChannelDiaLog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Edit failed!", Toast.LENGTH_SHORT).show()
                }
                viewModel.getChannels()
            }
        }
        viewModel.isShowProgressDialog.observe(viewLifecycleOwner) {
            if (it) progressDialog.show()
            else progressDialog.dismiss()
        }
    }
    private fun setupRecycleView() {
        val itemDecoration : RecyclerView.ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.rvChannelList.apply {
            channelListAdapter = ChannelListAdapter(this@ChannelListFragment)
            adapter = channelListAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(itemDecoration)
        }
    }

    override fun onItemClick(channel: Channel) {
        val direction = ChannelListFragmentDirections
            .actionChannelListFragmentToChannelFragment()
            .actionId

        val bundle = Bundle().apply {
            putString(ID_CHANNEL_KEY, channel.idChannel)
            putString(ID_TELEGRAM_CHANNEL_KEY, channel.idTelegramChannel)
            putString(CHANNEL_NAME, channel.nameChannel)
        }

        findNavController().navigate(direction, bundle)
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val isDelete = DialogUtil.showYesNoDialog(context)
            if (isDelete) {
                viewModel.deleteChannel(id)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.channelList.removeObservers(viewLifecycleOwner)
        viewModel.isAddingSuccess.removeObservers(viewLifecycleOwner)
        viewModel.isDeleteSuccess.removeObservers(viewLifecycleOwner)
    }

}