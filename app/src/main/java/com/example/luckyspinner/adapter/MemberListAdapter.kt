package com.example.luckyspinner.adapter


import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.MemberListItemBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MemberListAdapter(
    private val listener: Listener,
    private val eventList: List<Event> = ArrayList()
) : RecyclerView.Adapter<MemberListAdapter.MemberListViewHolder>() {

    lateinit var onEditClickListener: OnEditClickListener

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)
        fun onCheckBoxSelected(id: String, position: Int, isSelected: Boolean)
    }

    inner class MemberListViewHolder(val binding: MemberListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        return MemberListViewHolder(
            MemberListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MemberListViewHolder, position: Int) {
        holder.binding.apply {
            val member = members[position]
            tvMemberNameItem.text = member.nameMember
            var text = ""
            tvSubtitleMemberItem.isSelected = true

            if (eventList.isNotEmpty()) {
                val tempArray = ArrayList<String>()
                member.listEvent.forEachIndexed { index, id ->
                    val e = eventList.firstOrNull {
                        it.idEvent == id
                    }
                    e?.let {
                        tempArray.add(e.nameEvent)
                    }
                }
                if (tempArray.isEmpty()) {
                    text = "Haven't joined any events!"
                } else {
                    text = "Events joined: ${tempArray[0]}"
                    tempArray.forEachIndexed { index, s ->
                        if (index == 0) {
                            return@forEachIndexed
                        }
                        text += ", $s"
                    }
                }
            } else {
                text = "Haven't joined any events!"
            }
            tvSubtitleMemberItem.text = text


            btnDeleteMemberName.isVisible = false
            root.setOnClickListener {
                onEditClickListener.onEditClick(position)
            }
            btnDeleteMemberName.setOnClickListener {
                listener.onDeleteItem(member.idMember)
            }
            root.setOnLongClickListener {
                listener.onDeleteItem(member.idMember)
                true
            }
            root.setOnClickListener {
                onEditClickListener.onEditClick(position)
            }
        }
    }
}