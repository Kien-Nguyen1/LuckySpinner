package com.example.luckyspinner.fragments

import android.app.Dialog
import android.app.ProgressDialog
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
import com.example.luckyspinner.adapter.MemberListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.FragmentMemberListBinding
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.viewmodels.MemberListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MemberListFragment : Fragment(), MemberListAdapter.Listener {
    private val viewModel by viewModels<MemberListViewModel>()
    private lateinit var binding : FragmentMemberListBinding
    private lateinit var memberAdapter : MemberListAdapter
    private lateinit var idChannel : String
    private lateinit var addMemberDialog : Dialog
    private lateinit var progressDialog : ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMemberListBinding.inflate(inflater , container, false)
        progressDialog = ProgressDialog(requireContext())
        viewModel.progressDialog = progressDialog
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY).toString()

        viewModel.memberList.observe(viewLifecycleOwner) {
            memberAdapter.members = it
            progressDialog.dismiss()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getMembers(idChannel)
        }

        binding.btnAddMemberList.setOnClickListener {
            openAddMemberDiaLog(Gravity.CENTER)
        }

        binding.btnBackMemberList.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.tvChooseAllMember.setOnClickListener {
            handleChooseAllMember()
        }


        viewModel.isAddingMemberSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    Toast.makeText(context, "Add Spinner Successfully!", Toast.LENGTH_SHORT).show()
                    addMemberDialog.dismiss()
                }
                else
                {
                    Toast.makeText(context, "Add Spinner Fail!!", Toast.LENGTH_SHORT).show()
                }
                viewModel.isAddingMemberSuccess.value = null
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.getMembers(idChannel)
                }
            }
        }
    }

    private fun openAddMemberDiaLog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addMemberDialog = Dialog(requireContext())
        addMemberDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addMemberDialog.setContentView(binding.root)

        binding.tvAddChannel.text = "Member Name"

        val window : Window = addMemberDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addMemberDialog.show()

        binding.btnDoneAddChannel.setOnClickListener {
            val memberName = binding.edtEnterChannelName.text.toString()
            val memberId = binding.edtEnterChannelId.text.toString()
            progressDialog.show()
            viewModel.addMember(idChannel, memberId, memberName)
        }
    }
    private fun handleChooseAllMember() {
        progressDialog.show()
        val list = viewModel.memberList.value ?: return
        var isAllSelected = true
        run breaking@{
            list.forEach { member ->
                if (!member.hasSelected) {
                    isAllSelected = false
                    return@breaking
                }
            }
        }
        lifecycleScope.launch {
            viewModel.updateForChooseAllMember(idChannel, list, !isAllSelected)
        }
    }

    private fun setupRecycleView() {
        val itemDecoration : RecyclerView.ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.rvMemberList.apply {
            memberAdapter = MemberListAdapter(this@MemberListFragment)
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(itemDecoration)
        }
    }

    override fun onItemClick(id: String) {
        //
    }

    override fun onDeleteItem(id: String) {
        TODO("Not yet implemented")
    }

    override fun onCheckBoxSelected(id: String, position: Int, isSelected : Boolean) {
        lifecycleScope.launch {
            viewModel.updateCheckBoxForMember(idChannel, id, isSelected)
        }
    }
}
