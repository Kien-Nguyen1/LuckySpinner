package com.example.luckyspinner.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.luckyspinner.R
import com.example.luckyspinner.databinding.FragmentAddChannelBinding
import com.example.luckyspinner.viewmodels.AddChannelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddChannelFragment : Fragment(R.layout.fragment_add_channel) {
    private val  viewModel : AddChannelViewModel by viewModels()
    private lateinit var binding : FragmentAddChannelBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddChannelBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.context = requireContext()

        binding.btnDoneAddChannel.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val channelId = binding.edtEnterChannelId.text.toString()
                viewModel.addChannel(channelId, channelId)
            }
        }

        binding.btnCancelAddChannel.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                findNavController().popBackStack(R.id.channelListFragment, true)
            }
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Add Channel Successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.channelListFragment, true)
                }
                else
                {
                    Toast.makeText(context, "Add Channel Fail!!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}