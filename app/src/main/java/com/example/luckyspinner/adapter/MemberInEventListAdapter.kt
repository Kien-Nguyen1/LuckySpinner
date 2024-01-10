package com.example.luckyspinner.adapter


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.MemberListItemInEventBinding
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.util.Constants

class MemberInEventListAdapter(private val listener: Listener, private val eventId : String) : RecyclerView.Adapter<MemberInEventListAdapter.MemberListViewHolder>() {

    private lateinit var context : Context

    interface Listener {
        fun onMemberItemClick(member: Member)
        fun onDeleteItem(id: String)
        fun onCheckboxClickMember(id : String, position: Int, hasSelected : Boolean)

    }

    inner class MemberListViewHolder(val binding: MemberListItemInEventBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem.idMember == newItem.idMember
        }

        override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var members: List<Member>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount() = members.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberListViewHolder {
        context = parent.context
        return MemberListViewHolder(MemberListItemInEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        holder.binding.apply {
            val member = members[position]
            if (member.idMember == Constants.ID_ADD_MORE) {
                checkBoxSpinner.isVisible = false
                tvTitle.text = "+ Add More"
                linearMemberInEvent.background = ColorDrawable(Color.parseColor("#DFD5EC"))
                root.setOnClickListener {
                    listener.onMemberItemClick(member)
                }
            } else {
                tvTitle.text = member.nameMember
                tvTitle.isSelected = true
                checkBoxSpinner.isVisible = true

                checkBoxSpinner.isChecked = member.listEvent.contains(eventId)
                linearMemberInEvent.background = ColorDrawable(Color.WHITE)

                checkBoxSpinner.setOnClickListener {
                    listener.onCheckboxClickMember(member.idMember, position, member.hasSelected)
                }
                root.setOnClickListener {
                    checkBoxSpinner.isChecked = !checkBoxSpinner.isChecked
                    listener.onCheckboxClickMember(member.idMember, position, member.hasSelected)
                }
            }

        }
    }

}