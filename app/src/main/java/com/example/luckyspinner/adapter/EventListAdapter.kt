package com.example.luckyspinner.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.EventChannelItemBinding
import com.example.luckyspinner.models.Event

class EventListAdapter(private val listener: Listener) : RecyclerView.Adapter<EventListAdapter.EventListViewHolder>() {

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)

    }

    inner class EventListViewHolder(val binding: EventChannelItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return false
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var events: List<Event>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount() = events.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventListViewHolder {
        return EventListViewHolder(
            EventChannelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: EventListViewHolder, position: Int) {
        holder.binding.apply {
            val event = events[position]
            tvTitleEventItem.text = event.typeEvent.toString() + event.hour.toString()
            btnEditEventItem.setOnClickListener {
                listener.onItemClick(event.idEvent)
            }
        }
    }
}