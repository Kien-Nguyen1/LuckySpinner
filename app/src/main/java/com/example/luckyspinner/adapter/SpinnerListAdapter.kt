package com.example.luckyspinner.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.TitleSpinnerOrChannelItemBinding
import com.example.luckyspinner.interfaces.OnEditClickListener
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Spinner

class SpinnerListAdapter(private val listener: Listener, private val eventList : List<Event> = ArrayList()) : RecyclerView.Adapter<SpinnerListAdapter.SpinnerListViewHolder>() {

    lateinit var onEditClickListener: OnEditClickListener

    interface Listener {
        fun onItemClick(id: String, title : String)
        fun onDeleteItem(id: String)

    }

    inner class SpinnerListViewHolder(val binding: TitleSpinnerOrChannelItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Spinner>() {
        override fun areItemsTheSame(oldItem: Spinner, newItem: Spinner): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: Spinner, newItem: Spinner): Boolean {
            return false
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var spinners: List<Spinner>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount() = spinners.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerListViewHolder {
        return SpinnerListViewHolder(TitleSpinnerOrChannelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: SpinnerListViewHolder, position: Int) {
        holder.binding.apply {
            val spinner = spinners[position]
            tvTitleListOrChannelItem.text = spinner.titleSpin
            tvTitleListOrChannelItem.isSelected = true

            if (eventList.isNotEmpty()) {
                var text = ""
                spinner.listEvent.forEach {id ->
                    val e = eventList.firstOrNull {
                        it.idEvent  == id
                    }
                    e?.let {text += e.idEvent  }
                }
                tvTitleListOrChannelItem.text = text
            }

            btnEditSpinnerOrChannel.setOnClickListener {
                onEditClickListener.onEditClick(position)
            }
            btnDeleteSpinnerOrChannel.setOnClickListener {
                listener.onDeleteItem(spinner.idSpin)
            }
            root.setOnClickListener {
                listener.onItemClick(spinner.idSpin, spinner.titleSpin)
            }
            if (position % 2 != 0) {
                root.setBackgroundColor(Color.parseColor("#DFD5EC"))
            }
        }
    }
}