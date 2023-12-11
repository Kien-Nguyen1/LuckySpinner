package com.example.luckyspinner.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.MemberListItemBinding
import com.example.luckyspinner.databinding.TitleSpinnerOrChannelItemBinding
import com.example.luckyspinner.models.Member

class MemberListAdapter(private val listener: Listener) : RecyclerView.Adapter<MemberListAdapter.MemberListViewHolder>() {

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)

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
            val spinner = members[position]
            tvMemberNameItem.text = spinner.nameMember
            root.setOnClickListener {
                listener.onItemClick(spinner.idMember)
            }
        }
    }
}