package com.example.luckyspinner.fragments

import android.annotation.SuppressLint
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
import com.example.luckyspinner.adapter.MemberListAdapter
import com.example.luckyspinner.databinding.AddChannelLayoutBinding
import com.example.luckyspinner.databinding.EditDialogBinding
import com.example.luckyspinner.databinding.FragmentMemberListBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.DialogUtil
import com.example.luckyspinner.util.Function
import com.example.luckyspinner.util.Function.addFabScrollListener
import com.example.luckyspinner.viewmodels.MemberListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    var isFirstLoad = true
    var countDownTimer : CountDownTimer ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMemberListBinding.inflate(inflater , container, false)
        progressDialog = ProgressDialog(requireContext())

        binding.appBarMemberList.apply {
            tvTitleAppBar.text = "List Member"
            btnSpinnerList.visibility = View.GONE
            btnMemberList.visibility = View.GONE
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        idChannel = arguments?.getString(Constants.ID_CHANNEL_KEY)!!
        setupObserver()

        lifecycleScope.launch(Dispatchers.IO) {
            if (isFirstLoad) {
                viewModel.getEvents(idChannel)
                viewModel.getMembers(idChannel)
                isFirstLoad = false
            }
        }

        binding.btnAddMemberList.setOnClickListener {
            openAddMemberDiaLog(Gravity.CENTER)
        }

        binding.appBarMemberList.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.appBarMemberList.apply {
            btnSearch.setOnClickListener {
                //
            }
        }

        binding.appBarMemberList.apply {
            btnSpinnerList.isVisible = false
            btnMemberList.isVisible = false
        }

        handleSearch()
        binding.rvMemberList.addFabScrollListener(binding.btnAddMemberList)
    }
    fun handleSearch() {
        if (!viewModel.memberList.isInitialized) return
        fun isShowMenu(isShow : Boolean) {
            binding.appBarMemberList.apply {
                btnSearch.isVisible = isShow
                tvTitleAppBar.isVisible = isShow
                btnBack.isVisible = isShow
            }
        }
        val searchView = binding.appBarMemberList.searchView

        searchView.isVisible = false

        searchView.setOnCloseListener {
            searchView.isVisible = false
            isShowMenu(true)

            false
        }

        searchView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                Function.hideKeyBoard(context, v)
                searchView.isVisible = false
                isShowMenu(true)
            }
        }

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMember(newText)
                return false
            }

        })
        binding.appBarMemberList.btnSearch.setOnClickListener {
            searchView.showContextMenu()
            searchView.isVisible = true
            searchView.setIconifiedByDefault(true);

            searchView.isFocusable = true;
            searchView.isIconified = false;
            searchView.requestFocusFromTouch();
            searchView.clearFocus()
            isShowMenu(false)
        }
    }

    fun filterMember(text: String) {
        if (text == "") {
            viewModel.memberList.value = viewModel.memberList.value
            println("Let go")
            return
        }
        val list: MutableList<Member> = ArrayList()

        viewModel.memberList.value?.forEach {
            if (it.nameMember.contains(text, true)) {
                list.add(it)
            }
        }
        memberAdapter.members = list
    }
    fun createEditListener() {
        memberAdapter.onEditClickListener = object : OnEditClickListener{
            override fun onEditClick(position: Int) {
                val binding : EditDialogBinding = EditDialogBinding.inflate(layoutInflater)
                editMemberDiaLog = Dialog(requireContext())
                editMemberDiaLog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                editMemberDiaLog.setContentView(binding.root)

                val member = memberAdapter.members[position]

                binding.tvNameTitleAddElement.text = "Edit Member"
                binding.edtId.isVisible = false

                binding.edtEnterElement.setText(member.nameMember)

                binding.edtEnterElement.setSelection(binding.edtEnterElement.text.length)

                binding.btnCancelElement.setOnClickListener {
                    editMemberDiaLog.dismiss()
                }

                binding.btnDoneAddElement.setOnClickListener {
                    if (binding.edtEnterElement.text.toString().isEmpty()) {
                        binding.edtEnterElement.error = "Please fill this filed"
                        return@setOnClickListener
                    }
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
                lifecycleScope.launch {
                    delay(1)
                    Function.showKeyBoard(requireActivity(), binding.edtEnterElement)
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
            if (edt.text.toString().trim() == Constants.EMPTY_STRING) {
                edt.error = "Please fill this field"
                return@setOnClickListener
            }
            val memberName = binding.edtEnterChannelName.text.toString()
            viewModel.addMember(idChannel, Member(Calendar.getInstance().timeInMillis.toString(), memberName))
        }
        binding.btnCancelAddChannel.setOnClickListener {
            addMemberDialog.dismiss()
        }
        lifecycleScope.launch {
            delay(1)
            Function.showKeyBoard(requireActivity(), binding.edtEnterChannelName)
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
        binding.rvMemberList.apply {
            memberAdapter = MemberListAdapter(this@MemberListFragment)
            createEditListener()
            adapter = memberAdapter
            layoutManager = LinearLayoutManager(context)
            Function.addMarginToLastItem(binding.rvMemberList, 10)
        }
        viewModel.eventList.observe(viewLifecycleOwner) {
            binding.rvMemberList.apply {
                memberAdapter = MemberListAdapter(this@MemberListFragment, it)
                createEditListener()
                adapter = memberAdapter
                layoutManager = LinearLayoutManager(context)
                if (viewModel.memberList.isInitialized) {
                    viewModel.memberList.value = viewModel.memberList.value
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setupObserver() {
        viewModel.memberList.observe(viewLifecycleOwner) {
            memberAdapter.members = it
            memberAdapter.notifyDataSetChanged()
            binding.rvMemberList.isVisible = it.isNotEmpty()
            binding.imgEmptyList.isVisible = it.isEmpty()
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
                    Toast.makeText(requireContext(), "Edit successful!", Toast.LENGTH_SHORT).show()
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
