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
import androidx.work.WorkManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.ChannelListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.FragmentChannelListBinding
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.CHANNEL_NAME
import com.example.luckyspinner.viewmodels.ChannelListViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChannelListFragment : Fragment(), ChannelListAdapter.Listener {
    private lateinit var binding : FragmentChannelListBinding
    private val viewModel : ChannelListViewModel by viewModels()
    private lateinit var channelListAdapter : ChannelListAdapter
    private lateinit var addDialog : Dialog
    private lateinit var deleteDialog : Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelListBinding.inflate(inflater, container, false)
        setupRecycleView()
        viewModel.channelList.observe(viewLifecycleOwner) {
            channelListAdapter.channels = it
        }
        viewModel.isAddingSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Add Channel Successfully!", Toast.LENGTH_SHORT).show()
                    addDialog.dismiss()
                }
                else
                {
                    Toast.makeText(context, "Add Channel Fail!!", Toast.LENGTH_SHORT).show()
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getChannels()
                }
            }
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isDeleteSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if(it) {
                    Snackbar.make(view, "Deleted Channel Successfully!", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "Delete Channel Fail!!", Toast.LENGTH_SHORT).show()
                }
                viewModel.isDeleteSuccess.value = null
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getChannels()
        }

        binding.btnAddChannel.setOnClickListener {
            Log.d("kien", "click add channel")
            openAddChannelDialog(Gravity.CENTER)
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

        viewModel.context = requireContext()

        binding.btnDoneAddChannel.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                Log.d("kien", "click done add channel")
                val channelId = binding.edtEnterChannelId.text.toString()
                val channelName = binding.edtEnterChannelName.text.toString()
                viewModel.addChannel(channelId, channelName)
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

    override fun onItemClick(id: String, name : String) {
        findNavController().navigate(R.id.channelFragment, Bundle().apply {
            putString(Constants.ID_CHANNEL_KEY, id)
            putString(CHANNEL_NAME, name)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteChannel(id)
        }
    }
}