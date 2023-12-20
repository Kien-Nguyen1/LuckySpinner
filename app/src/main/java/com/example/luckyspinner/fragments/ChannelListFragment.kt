package com.example.luckyspinner.fragments

import android.app.Dialog
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
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.example.luckyspinner.viewmodels.ChannelListViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ChannelListFragment : Fragment(), ChannelListAdapter.Listener {
    private lateinit var binding : FragmentChannelListBinding
    private val viewModel : ChannelListViewModel by viewModels()
    private lateinit var channelListAdapter : ChannelListAdapter
    private lateinit var addDialog : Dialog
    private lateinit var editChannelDiaLog : Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelListBinding.inflate(inflater, container, false)
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
        viewModel.isAddingSuccess.observe(viewLifecycleOwner) {
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
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        println("Here come onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        addDialog = Dialog(requireContext())

        viewModel.message.observe(viewLifecycleOwner) {
//            Toast.makeText(requireContext(), it , Toast.LENGTH_SHORT).show()
        }
        viewModel.isDeleteSuccess.observe(viewLifecycleOwner) {
            println("Here the observer delete come")
            it?.let {
                if(it) {
                    Toast.makeText(context, "Deleted Channel Successfully!", Toast.LENGTH_SHORT).show()
                    editChannelDiaLog.dismiss()
                } else {
                    Toast.makeText(context, "Delete Channel Fail!!", Toast.LENGTH_SHORT).show()
                }
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

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getChannels()
        }

        binding.btnAddChannel.setOnClickListener {
            Log.d("kien", "click add channel")
            openAddChannelDialog(Gravity.CENTER)
        }

        channelListAdapter.onEditClickListener = object  : OnEditClickListener{
            override fun onEditClick(position: Int) {
                val binding : EditDialogBinding = EditDialogBinding.inflate(layoutInflater)
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

                val window : Window = editChannelDiaLog.window!!
                window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val windowAttribute : WindowManager.LayoutParams = window.attributes
                windowAttribute.gravity = Gravity.CENTER
                window.attributes = windowAttribute

                editChannelDiaLog.show()

            }
        }

        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val channel = channelListAdapter.differ.currentList[position]
                viewModel.deleteChannel(channel.idChannel)
                val channels : MutableList<Channel> = viewModel.channelList.value!!.toMutableList()
                channels.removeAt(position)
                channelListAdapter.channels = channels
                viewModel.channelList.value = channels
            }
        }

        ItemTouchHelper(itemTouchHelperCallBack).apply {
            attachToRecyclerView(binding.rvChannelList)
        }
    }
    private fun openAddChannelDialog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
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
            if (channelTelegramId == EMPTY_STRING) {
                binding.edtEnterChannelId.error = "Please fill your TelegramId of channel/group to receive the bot message!"
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
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteChannel(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Here come onCreate ${this.javaClass.name}")
    }


    override fun onStop() {
        super.onStop()
        println("Here come onStop ${this.javaClass.name} ")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("Here come onDestroyView ${this.javaClass.name}")
        viewModel.channelList.removeObservers(viewLifecycleOwner)
        viewModel.isAddingSuccess.removeObservers(viewLifecycleOwner)
        viewModel.isDeleteSuccess.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Here come onDestroy ${this.javaClass.name}")

    }
}