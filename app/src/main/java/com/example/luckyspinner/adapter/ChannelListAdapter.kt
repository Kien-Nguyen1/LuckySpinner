package com.example.luckyspinner.adapter


import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.databinding.TitleSpinnerOrChannelItemBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Channel
import kotlin.math.roundToInt
import kotlin.math.sign

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
        val channel = channels[position]

        holder.binding.apply {
            tvTitleListOrChannelItem.text = channel.nameChannel

            tvTitleListOrChannelItem.isSelected = true

            tvSubTitle.text = "Telegram ID Channel: ${channel.idTelegramChannel}"

            btnEditSpinnerOrChannel.setOnClickListener {
                onEditClickListener.onEditClick(position)
            }

            btnDeleteSpinnerOrChannel.isVisible = false
            root.setOnClickListener {
                listener.onItemClick(channel)
            }
            root.setOnLongClickListener {
                listener.onDeleteItem(channel.idChannel)
                true
            }
            btnDeleteSpinnerOrChannel.setOnClickListener {
                listener.onDeleteItem(channel.idChannel)
            }
        }
    }
}