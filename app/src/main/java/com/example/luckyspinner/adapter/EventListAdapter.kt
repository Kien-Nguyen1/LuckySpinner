package com.example.luckyspinner.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.EventChannelItemBinding
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants

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

            tvEventNameItem.text = event.nameEvent

            var title = ""
            event.listDay.apply {
                if (contains(Constants.MONDAY)) title += "Mon"
                if (contains(Constants.TUESDAY)) title += " Tue"
                if (contains(Constants.WEDNESDAY)) title += " Wed"
                if (contains(Constants.THURSDAY)) title += " Thu"
                if (contains(Constants.FRIDAY)) title += " Fri"
                if (contains(Constants.SATURDAY)) title += " Sat"
                if (contains(Constants.SUNDAY)) title += " Sun"
            }
            if (title == "") title = "No day in week"

            tvTimeEventItem.text = "${event.hour} : ${event.minute}"


            tvTitleEventItem.isSelected = true
            tvTitleEventItem.text = title

            btnEditEventItem.setOnClickListener {
                listener.onItemClick(event.idEvent)
            }
            root.setOnClickListener {
                listener.onItemClick(event.idEvent)
            }
            btnDeleteEventItem.setOnClickListener {
                listener.onDeleteItem(event.idEvent)
            }
        }
    }
}