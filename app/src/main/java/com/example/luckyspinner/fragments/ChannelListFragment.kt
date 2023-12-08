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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.ChannelListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.FragmentChannelListBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.ChannelListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChannelListFragment : Fragment(), ChannelListAdapter.Listener {
    private lateinit var binding : FragmentChannelListBinding
    private val viewModel : ChannelListViewModel by viewModels()
    private lateinit var channelAdapter : ChannelListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChannelListBinding.inflate(inflater, container, false)
        setupRecycleView()
        viewModel.channelList.observe(viewLifecycleOwner) {
            channelAdapter.channels = it
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getChannels()
        }

        binding.btnAddChannel.setOnClickListener {
            Log.d("kien", "click add channel")
            openAddChannelDialog(Gravity.CENTER)
        }
    }
    private fun openAddChannelDialog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        val window : Window = dialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        dialog.show()

        viewModel.context = requireContext()

        binding.btnDoneAddChannel.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                Log.d("kien", "click done add channel")
                val channelId = binding.edtEnterChannelId.text.toString()
                viewModel.addChannel(channelId, channelId)
            }
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Add Channel Successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    viewModel.isSuccess.value = null
                }
                else
                {
                    Toast.makeText(context, "Add Channel Fail!!", Toast.LENGTH_SHORT).show()
                    viewModel.isSuccess.value = null
                }
            }
        }
    }

    private fun setupRecycleView() {
        binding.rvChannelList.apply {
            channelAdapter = ChannelListAdapter(this@ChannelListFragment)
            adapter = channelAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onItemClick(id: String) {
        findNavController().navigate(R.id.spinnerListFragment, Bundle().apply {
            putString(Constants.ID_CHANNEL_KEY, id)
        })
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.deleteChannel(id)
        }
    }
}