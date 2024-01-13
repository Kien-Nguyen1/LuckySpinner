package com.example.luckyspinner.fragments

import android.app.Dialog
import android.app.Notification.Action
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemServiceName
import androidx.core.view.MenuItemCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
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
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.util.Function.addFabScrollListener
import com.example.luckyspinner.viewmodels.ChannelListViewModel
import com.google.android.material.search.SearchView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar


class ChannelListFragment : Fragment(), ChannelListAdapter.Listener {
    private lateinit var binding : FragmentChannelListBinding
    private val viewModel : ChannelListViewModel by viewModels()
    private lateinit var channelListAdapter : ChannelListAdapter
    private lateinit var addDialog : Dialog
    private lateinit var editChannelDiaLog : Dialog
    private lateinit var progressDialog: ProgressDialog
    var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("listchannel HHere come oncreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        println("listchannel HHere come oncreateview")
        binding = FragmentChannelListBinding.inflate(inflater, container, false)
        progressDialog = ProgressDialog(context)
        setupStateObserver()

        setupRecycleView()

        if (isFirstLoad) {
            viewModel.getChannels()
            isFirstLoad = false
            println("listchannel Here come get channels")
        }

        binding.toolBarChannelList.menu.apply {
            findItem(R.id.spinnerListFragment).isVisible = false
            findItem(R.id.memberListFragment).isVisible = false
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("listchannel HHere come onviewcreated")


        viewModel.message.observe(viewLifecycleOwner) {
//            Toast.makeText(requireContext(), it , Toast.LENGTH_SHORT).show()
        }


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
                binding.edtId.setText(channel.idTelegramChannel)

                binding.edtId.setSelection(channel.idTelegramChannel.length)
                binding.edtEnterElement.setSelection(binding.edtEnterElement.selectionEnd)


                binding.btnDoneAddElement.setOnClickListener {
                    if (binding.edtEnterElement.text.toString().trim() == EMPTY_STRING) {
                        binding.edtEnterElement.error = " Please fill this filed!"
                        return@setOnClickListener
                    }
                    if (binding.edtId.text.toString().trim() == EMPTY_STRING) {
                        binding.edtId.error = " Please fill this filed!"
                        return@setOnClickListener
                    }
                    channel.nameChannel = binding.edtEnterElement.text.toString()
                    channel.idTelegramChannel = binding.edtId.text.toString()
                    viewModel.editChannel(channel)
                    editChannelDiaLog.dismiss()
                }


                binding.btnCancelElement.setOnClickListener {
                    editChannelDiaLog.dismiss()
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

                lifecycleScope.launch {
                    delay(1)
                    Function.showKeyBoard(requireActivity(), binding.edtId)
                }
            }
        }

        binding.rvChannelList.addFabScrollListener(binding.btnAddChannel)

//        binding.toolBarChannelList.setOnClickListener {
//            false
//        }
//
//        binding.toolBarChannelList.menu.getItem(R.id.search).setOnMenuItemClickListener {
//
//            false
//        }

        binding.toolBarChannelList.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.search -> {
                    val searchView : androidx.appcompat.widget.SearchView = menuItem.actionView as androidx.appcompat.widget.SearchView
//                    searchView.show
                    searchView.queryHint = "Search Channel..."
                    searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            filterChannel(newText)
                            return false
                        }
                    })
                    false
                }

                else -> false
            }
        }
    }

    fun filterChannel(text : String) {
        if (!viewModel.channelList.isInitialized) return
        val  list = viewModel.channelList.value!!.filter {
            it.nameChannel.contains(text)
        }
        channelListAdapter.channels = list
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
                if (text.toString().trim() == EMPTY_STRING) {
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
        binding.btnCancelAddChannel.setOnClickListener {
            addDialog.dismiss()
        }
        lifecycleScope.launch {
            delay(1)
            Function.showKeyBoard(requireActivity(), binding.edtEnterChannelId)
        }
    }

    fun setupStateObserver() {
        viewModel.channelList.observe(viewLifecycleOwner) {
            channelListAdapter.channels = it
            channelListAdapter.notifyDataSetChanged()
            if (it.isEmpty()) {
                binding.rvChannelList.visibility = View.GONE
                binding.imgEmptyList.visibility = View.VISIBLE
            } else {
                binding.rvChannelList.visibility = View.VISIBLE
                binding.imgEmptyList.visibility = View.GONE
            }
        }
        viewModel.isAddingSuccess.observe(viewLifecycleOwner) {
            println("Here come adding")
            it?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Add successful!", Toast.LENGTH_SHORT).show()
                    addDialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Add failed!", Toast.LENGTH_SHORT).show()
                }
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
                viewModel.getChannels()
            }
        }
        viewModel.isEditingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    editChannelDiaLog.dismiss()
                    Toast.makeText(context, "Edit Channel Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Edit failed!", Toast.LENGTH_SHORT).show()
                }
                viewModel.getChannels()
            }
        }
    }
    private fun setupRecycleView() {
        binding.rvChannelList.apply {
            channelListAdapter = ChannelListAdapter(this@ChannelListFragment)
            adapter = channelListAdapter
            layoutManager = LinearLayoutManager(context)
            Function.addMarginToLastItem(binding.rvChannelList, 10)
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
        val list : MutableList<MutableLiveData<*>> = ArrayList()
        list.add(viewModel.isEditingSuccess)
        list.add(viewModel.isAddingSuccess)
        list.add(viewModel.isDeleteSuccess)

        Function.toNull(list)

        list.add(viewModel.channelList)
        Function.removeObservers(list, viewLifecycleOwner)

    }

}