package com.example.luckyspinner.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.databinding.TitleSpinnerOrChannelItemBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Channel

class ChannelListAdapter(private val listener : Listener) : RecyclerView.Adapter<ChannelListAdapter.ChannelListViewHolder>() {
    interface Listener {
        fun onItemClick(channel: Channel)
        fun onDeleteItem(id: String)
    }

    lateinit var onEditClickListener: OnEditClickListener

    inner class ChannelListViewHolder(val binding: TitleSpinnerOrChannelItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return false
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
            btnEditSpinnerOrChannel.setOnClickListener {
                onEditClickListener.onEditClick(position)
            }
            root.setOnClickListener {
                listener.onItemClick(channel)
            }
            btnDeleteSpinnerOrChannel.setOnClickListener {
                listener.onDeleteItem(channel.idChannel)
            }
            if (position % 2 == 0) {
                titleSpinnerOrChannelLayout.setBackgroundColor(Color.parseColor("#e7f0fd"))
            }
        }
    }
}