package com.example.luckyspinner.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.TitleSpinnerOrChannelItemBinding
import com.example.luckyspinner.models.Channel

class ChannelListAdapter(private val listener : Listener) : RecyclerView.Adapter<ChannelListAdapter.ChannelListViewHolder>() {
    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)
    }

    inner class ChannelListViewHolder(val binding: TitleSpinnerOrChannelItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem.idChannel == newItem.idChannel
        }

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)
    var channels: List<Channel>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount() = channels.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelListViewHolder {
        return ChannelListViewHolder(TitleSpinnerOrChannelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ChannelListViewHolder, position: Int) {
        holder.binding.apply {
            val channel = channels[position]
            tvTitleListOrChannelItem.text = channel.nameChannel
            root.setOnClickListener {
                listener.onItemClick(channel.idChannel)
            }
        }
    }
}