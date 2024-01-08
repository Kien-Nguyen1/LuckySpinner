package com.example.luckyspinner.adapter



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.DateListItemBinding
import com.example.luckyspinner.util.Constants

class DateListAdapter(private val listener: Listener) : RecyclerView.Adapter<DateListAdapter.DateListViewHolder>() {

    interface Listener {
        fun onDateClick(position: Int, isChecked: Boolean)
    }

    inner class DateListViewHolder(val binding: DateListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return true
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return false
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var dayList: List<Int>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
            notifyDataSetChanged()
        }

    val dayOfWeek =
        arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    override fun getItemCount() = dayOfWeek.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateListViewHolder {
        return DateListViewHolder(
            DateListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DateListViewHolder, position: Int) {
        holder.binding.apply {
            root.setPadding(10)
            tvTitle.text = dayOfWeek[position]
            tvTitle.isSelected = true
            checkBoxSpinner.isChecked = dayList.contains(changeTheNumberOfDay(position))
            checkBoxSpinner.setOnClickListener {
                listener.onDateClick(position, checkBoxSpinner.isChecked)
                println("Here come ischeck ${checkBoxSpinner.isChecked}")
            }
        }
    }

    fun changeTheNumberOfDay(position: Int): Int {
        if (position == 6) return Constants.SUNDAY
        return position + 2
    }
}