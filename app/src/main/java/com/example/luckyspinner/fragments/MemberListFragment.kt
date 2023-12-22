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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.adapter.MemberListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.EditDialogBinding
import com.example.luckyspinner.databinding.FragmentMemberListBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.viewmodels.MemberListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar


class MemberListFragment : Fragment(), MemberListAdapter.Listener {
    private val viewModel by viewModels<MemberListViewModel>()
    private lateinit var binding : FragmentMemberListBinding
    private lateinit var memberAdapter : MemberListAdapter
    private lateinit var idChannel : String
    private lateinit var addMemberDialog : Dialog
    private lateinit var progressDialog : ProgressDialog
    private lateinit var editMemberDiaLog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMemberListBinding.inflate(inflater , container, false)
        progressDialog = ProgressDialog(requireContext())
        binding.ckbChooseAllMember.isVisible = false

        binding.appBarMemberList.apply {
            toolBar.title = "List Member"
            toolBar.menu.findItem(R.id.spinnerListFragment)?.isVisible = false
            toolBar.menu.findItem(R.id.memberListFragment)?.isVisible = false
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY).toString()
        setupObserver()

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getMembers(idChannel)
        }

        binding.btnAddMemberList.setOnClickListener {
            openAddMemberDiaLog(Gravity.CENTER)
        }

        binding.appBarMemberList.apply {
            toolBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }

        binding.ckbChooseAllMember.setOnClickListener {
            handleChooseAllMember()
        }

        memberAdapter.onEditClickListener = object : OnEditClickListener{
            override fun onEditClick(position: Int) {
                val binding : EditDialogBinding = EditDialogBinding.inflate(layoutInflater)
                editMemberDiaLog = Dialog(requireContext())
                editMemberDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editMemberDiaLog.setContentView(binding.root)

                val member = memberAdapter.members[position]

                binding.tvNameTitleAddElement.text = "Edit Member"

                binding.edtEnterElement.setText(member.nameMember)

                binding.btnCancelElement.setOnClickListener {
                    editMemberDiaLog.dismiss()
                }

                binding.btnDoneAddElement.setOnClickListener {
                    member.nameMember = binding.edtEnterElement.text.toString()
                    viewModel.editMember(
                        idChannel,
                        member
                    )
                }

                val window : Window = editMemberDiaLog.window!!
                window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val windowAttribute : WindowManager.LayoutParams = window.attributes
                windowAttribute.gravity = Gravity.CENTER
                window.attributes = windowAttribute

                editMemberDiaLog.show()
            }
        }


    }

    private fun openAddMemberDiaLog(gravity: Int) {
        val binding : AddChannelLayoutBinding = AddChannelLayoutBinding.inflate(layoutInflater)
        addMemberDialog = Dialog(requireContext())
        addMemberDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addMemberDialog.setContentView(binding.root)

        binding.tvAddChannel.text = "Member Name"
        binding.edtEnterChannelId.isVisible = false

        val window : Window = addMemberDialog.window!!
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val windowAttribute : WindowManager.LayoutParams = window.attributes
        windowAttribute.gravity = gravity
        window.attributes = windowAttribute

        addMemberDialog.show()

        binding.btnDoneAddChannel.setOnClickListener {
            val edt = binding.edtEnterChannelName
            if (edt.text.toString() == Constants.EMPTY_STRING) {
                edt.error = "Please fill this field"
                return@setOnClickListener
            }
            val memberName = binding.edtEnterChannelName.text.toString()
            progressDialog.show()
            viewModel.addMember(idChannel, Member(Calendar.getInstance().timeInMillis.toString(), memberName))
        }
    }
    private fun handleChooseAllMember() {
        val list = viewModel.memberList.value ?: return
        progressDialog.show()
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
        val decorationItem : RecyclerView.ItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.rvMemberList.apply {
            memberAdapter = MemberListAdapter(this@MemberListFragment)
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(decorationItem)
        }
    }

    fun setupObserver() {
        viewModel.memberList.observe(viewLifecycleOwner) {
            memberAdapter.members = it
            binding.rvMemberList.isVisible = it.isNotEmpty()
            binding.imgEmptyList.isVisible = it.isEmpty()
//            var isAllSelected = true
//            run breaking@{
//                it.forEach { member ->
//                    if (!member.hasSelected) {
//                        isAllSelected = false
//                        return@breaking
//                    }
//                }
//            }
//            binding.ckbChooseAllMember.isChecked = isAllSelected
//            progressDialog.dismiss()
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
        viewModel.isDeletingMemberSuccess.observe(viewLifecycleOwner) {
            println("Here the observer delete come")
            it?.let {
                if(it) {
                    Toast.makeText(context, "Deleted Channel Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Delete Channel Fail!!", Toast.LENGTH_SHORT).show()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getMembers(idChannel)
                }
            }
        }
        viewModel.isEdtingMemberSuccess.observe(viewLifecycleOwner) {
            it?.let {
                if (it) {
                    editMemberDiaLog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Edit failed!", Toast.LENGTH_SHORT).show()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getMembers(idChannel)
                }
            }
        }
        viewModel.isShowProgressDialog.observe(viewLifecycleOwner) {
            if (it) progressDialog.show()
            else progressDialog.dismiss()
        }
    }

    override fun onItemClick(id: String) {
        //
    }

    override fun onDeleteItem(id: String) {
        lifecycleScope.launch {
            val isDelete = DialogUtil.showYesNoDialog(context)
            if (isDelete) {
                progressDialog.show()
                viewModel.deleteMember(idChannel, id)
            }
        }
    }

    override fun onCheckBoxSelected(id: String, position: Int, isSelected : Boolean) {
        lifecycleScope.launch {
            viewModel.updateCheckBoxForMember(idChannel, id, isSelected)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        val list : MutableList<MutableLiveData<*>> = ArrayList<MutableLiveData<*>>().apply {
            add(viewModel.isEdtingMemberSuccess)
            add(viewModel.isDeletingMemberSuccess)
            add(viewModel.isAddingMemberSuccess)
        }
        Function.toNull(list)
        list.add(viewModel.memberList)
        Function.removeObservers(list, viewLifecycleOwner)
    }
}
