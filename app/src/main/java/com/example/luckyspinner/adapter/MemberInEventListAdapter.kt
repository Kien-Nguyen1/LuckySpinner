package com.example.luckyspinner.adapter


import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.MemberListItemInEventBinding
import com.example.luckyspinner.models.Member
import kotlin.math.roundToInt

class MemberInEventListAdapter(private val listener: Listener, private val eventId : String) : RecyclerView.Adapter<MemberInEventListAdapter.MemberListViewHolder>() {

    private lateinit var context : Context

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)
        fun onCheckboxClickMember(id : String, position: Int, hasSelected : Boolean)

    }

    inner class MemberListViewHolder(val binding: MemberListItemInEventBinding) : RecyclerView.ViewHolder(binding.root)

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
            notifyDataSetChanged()
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

    private fun dpToPx(dp: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        if (position == itemCount - 1) {
            val marginLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            val marginInDp = dpToPx(10)
            marginLayoutParams.bottomMargin = marginInDp
        }

        holder.binding.apply {
            val member = members[position]
            tvTitle.text = member.nameMember
            tvTitle.isSelected = true

            checkBoxSpinner.isChecked = member.listEvent.contains(eventId)

            checkBoxSpinner.setOnClickListener {
                listener.onCheckboxClickMember(member.idMember, position, member.hasSelected)
            }
            root.setOnClickListener {
                listener.onItemClick(member.idMember)
            }
        }
    }

}