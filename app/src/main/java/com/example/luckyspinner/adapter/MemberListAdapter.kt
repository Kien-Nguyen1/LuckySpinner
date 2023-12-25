package com.example.luckyspinner.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.MemberListItemBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member

class MemberListAdapter(private val listener: Listener, private val eventList : List<Event>? = null) : RecyclerView.Adapter<MemberListAdapter.MemberListViewHolder>() {

    lateinit var onEditClickListener: OnEditClickListener

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)
        fun onCheckBoxSelected(id: String, position: Int, isSelected : Boolean)
    }

    inner class MemberListViewHolder(val binding: MemberListItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(oldItem: Member, newItem: Member): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Member, newItem: Member): Boolean {
            return false
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
            btnEditMemberName.setOnClickListener {
                onEditClickListener.onEditClick(position)
            }
            btnDeleteMemberName.setOnClickListener {
                listener.onDeleteItem(member.idMember)
            }
            root.setOnClickListener {
                listener.onItemClick(member.idMember)
            }
            if (position % 2 == 0) {
                root.setBackgroundColor(Color.parseColor("#e7f0fd"))
            }
        }
    }
}