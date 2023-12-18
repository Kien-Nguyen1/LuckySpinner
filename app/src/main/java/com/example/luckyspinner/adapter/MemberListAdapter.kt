package com.example.luckyspinner.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.MemberListItemBinding
import com.example.luckyspinner.models.Member

class MemberListAdapter(private val listener: Listener) : RecyclerView.Adapter<MemberListAdapter.MemberListViewHolder>() {

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)
        fun onCheckBoxSelected(id: String, position: Int, isSelected : Boolean)

    }

    inner class MemberListViewHolder(val binding: MemberListItemBinding) : RecyclerView.ViewHolder(binding.root)

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
        return MemberListViewHolder(MemberListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        holder.binding.apply {
            val member = members[position]
            tvMemberNameItem.text = member.nameMember
            checkBoxMemberListItem.isChecked = member.hasSelected
            checkBoxMemberListItem.setOnClickListener {
                listener.onCheckBoxSelected(member.idMember, position, it.isSelected)
            }
            root.setOnClickListener {
                listener.onItemClick(member.idMember)
            }
        }
    }
}