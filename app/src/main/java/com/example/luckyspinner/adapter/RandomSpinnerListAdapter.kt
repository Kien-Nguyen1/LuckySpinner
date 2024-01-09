package com.example.luckyspinner.adapter


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.RandomSpinnerListItemBinding
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.EMPTY_STRING


class RandomSpinnerListAdapter(private val listener: Listener, private val eventId : String) : RecyclerView.Adapter<RandomSpinnerListAdapter.SpinnerListViewHolder>() {

    private lateinit var context: Context
    var isSearch = false
    var oldList : List<Spinner> = ArrayList()

    interface Listener {
        fun onSpinnerClick(spinner: Spinner)
        fun onDeleteItem(id: String)
        fun onCheckboxClickSpinner(id : String, position: Int, hasSelected : Boolean)

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
//            notifyDataSetChanged()
        }

    fun updateList(newList: List<Spinner>, isSearch : Boolean) {
        // Use DiffUtil to calculate the difference
        // Update the data list
        val diffResult = DiffUtil.calculateDiff(MyDiffCallback(spinners, newList))

        // Update the data list

        // Update the data list
        spinners = newList

        // Apply the diff result to the adapter

        // Apply the diff result to the adapter
//        diffResult.dispatchUpdatesTo(this)
        // Apply the diff result to the adapter
    }

    override fun getItemCount() = spinners.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpinnerListViewHolder {
        context = parent.context
        return SpinnerListViewHolder(RandomSpinnerListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: SpinnerListViewHolder, position: Int) {
        holder.binding.apply {
            val spinner = spinners[position]

            if (spinner.idSpin == Constants.ID_ADD_MORE && spinner.titleSpin == EMPTY_STRING && !isSearch) {
                checkBoxSpinner.isVisible = false
                tvTitle.text = "+ Add More"
                linearRandomSpinnerListItem.background = ColorDrawable(Color.parseColor("#DFD5EC"))
                root.setOnClickListener {
                    listener.onSpinnerClick(spinner)
                }
            } else {
                println("Here come spinner ${spinner.idSpin}")
                checkBoxSpinner.isVisible = true

                tvTitle.text = spinner.titleSpin
                tvTitle.isSelected = true
                linearRandomSpinnerListItem.background = ColorDrawable(Color.WHITE)

//            var isCheck = false
//            spinner.listEvent.forEach {
//                if (it == eventId) isCheck = true
//            }

                checkBoxSpinner.isChecked = spinner.listEvent.contains(eventId)

                checkBoxSpinner.setOnClickListener {
                    listener.onCheckboxClickSpinner(spinner.idSpin, position, spinner.hasSelected)
                }
                root.setOnClickListener {
                    listener.onSpinnerClick(spinner)
                }
            }

            }

    }
}

class MyDiffCallback(oldList: List<Spinner>, newList: List<Spinner>) :
    DiffUtil.Callback() {
    private val oldList: List<Spinner>
    private val newList: List<Spinner>

    init {
        this.oldList = oldList
        this.newList = newList
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Implement logic to check if items are the same (e.g., based on ID)
        return oldList[oldItemPosition].idSpin === newList[newItemPosition].idSpin
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Implement logic to check if item contents are the same
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}