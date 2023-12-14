package com.example.luckyspinner.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.RandomSpinnerListItemBinding
import com.example.luckyspinner.models.Spinner

class RandomSpinnerListAdapter(private val listener: Listener) : RecyclerView.Adapter<RandomSpinnerListAdapter.SpinnerListViewHolder>() {

    interface Listener {
        fun onItemClick(id: String)
        fun onDeleteItem(id: String)
        fun onCheckboxClick(id : String, position: Int)

    }

    inner class SpinnerListViewHolder(val binding: RandomSpinnerListItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Spinner>() {
        override fun areItemsTheSame(oldItem: Spinner, newItem: Spinner): Boolean {
            return oldItem.idSpin == newItem.idSpin
        }

        override fun areContentsTheSame(oldItem: Spinner, newItem: Spinner): Boolean {
            return oldItem == newItem
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
        return SpinnerListViewHolder(RandomSpinnerListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: SpinnerListViewHolder, position: Int) {
        holder.binding.apply {
            val spinner = spinners[position]
            tvTitle.text = spinner.titleSpin
            checkBoxSpinner.isChecked = spinner.hasSelected
            checkBoxSpinner.setOnClickListener {
                listener.onCheckboxClick(spinner.idSpin, position)
            }
            root.setOnClickListener {
                listener.onItemClick(spinner.idSpin)
            }
        }
    }
}