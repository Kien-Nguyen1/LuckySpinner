package com.example.luckyspinner.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.EventChannelItemBinding
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants

class EventListAdapter(private val listener: Listener) :
    RecyclerView.Adapter<EventListAdapter.EventListViewHolder>() {

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)

        fun onSwitchClick(id: String, position: Int)

    }

    inner class EventListViewHolder(val binding: EventChannelItemBinding) :
        RecyclerView.ViewHolder(binding.root)

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
            )
        )
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

                if ( this.containsAll(arrayListOf(1,2,3,4,5,6,7))) {
                    title = "All week"
                }
            }
            if (title == "") title = "No day in week"

            tvTimeEventItem.text = "${event.hour} : ${event.minute}"

            btnSwitch.isChecked = event.isTurnOn
            btnSwitch.setOnClickListener {
                listener.onSwitchClick(event.idEvent, position)
            }

            tvTitleEventItem.isSelected = true
            tvEventNameItem.isSelected = true
            tvTitleEventItem.isSelected = true
            tvTitleEventItem.text = title

            btnDeleteEventItem.isVisible = false
            btnEditEventItem.isVisible = false

            btnEditEventItem.setOnClickListener {
                listener.onItemClick(event.idEvent)
            }
            root.setOnClickListener {
                listener.onItemClick(event.idEvent)
            }
            root.setOnLongClickListener {
                listener.onDeleteItem(event.idEvent)
                true
            }

            btnDeleteEventItem.setOnClickListener {
                listener.onDeleteItem(event.idEvent)
            }

            if (position % 2 == 0) {
                root.setBackgroundColor(Color.WHITE)
            }
        }
    }
}