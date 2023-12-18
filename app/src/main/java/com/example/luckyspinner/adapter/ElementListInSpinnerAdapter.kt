package com.example.luckyspinner.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.ElementInSpinnerItemBinding
import com.example.luckyspinner.models.ElementSpinner

class ElementListInSpinnerAdapter(private val listener: Listener) : RecyclerView.Adapter<ElementListInSpinnerAdapter.ElementListInSpinner>() {

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)

    }

    inner class ElementListInSpinner(val binding: ElementInSpinnerItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<ElementSpinner>() {
        override fun areItemsTheSame(oldItem: ElementSpinner, newItem: ElementSpinner): Boolean {
            return oldItem.idElement == newItem.idElement
        }

        override fun areContentsTheSame(oldItem: ElementSpinner, newItem: ElementSpinner): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var elementSpinners : List<ElementSpinner>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount() = elementSpinners.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementListInSpinner {
        return ElementListInSpinner(ElementInSpinnerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: ElementListInSpinner, position: Int) {
        holder.binding.apply {
            val spinner = elementSpinners[position]
            tvElementInSpinner.text = spinner.nameElement
            root.setOnClickListener {
                listener.onItemClick(spinner.idElement)
            }

            if (position % 2 == 0) {
                root.setBackgroundColor(Color.parseColor("#e7f0fd"))
            }
        }
    }
}