package com.example.luckyspinner.adapter


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.R
import com.example.luckyspinner.databinding.RandomSpinnerListItemBinding
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants

class RandomSpinnerListAdapter(private val listener: Listener, private val eventId : String) : RecyclerView.Adapter<RandomSpinnerListAdapter.SpinnerListViewHolder>() {

    private lateinit var context: Context


    interface Listener {
        fun onSpinnerClick(spinner: Spinner)
        fun onDeleteItem(id: String)
        fun onCheckboxClickSpinner(id : String, position: Int, hasSelected : Boolean)

    }

    inner class SpinnerListViewHolder(val binding: RandomSpinnerListItemBinding) : RecyclerView.ViewHolder(binding.root)

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
            if (spinner.idSpin == Constants.ID_ADD_MORE) {
                checkBoxSpinner.isVisible = false
                tvTitle.text = "+ Add More"
                linearRandomSpinnerListItem.background = ColorDrawable(Color.parseColor("#DFD5EC"))
                root.setOnClickListener {
                    listener.onSpinnerClick(spinner)
                }
            } else {
                tvTitle.text = spinner.titleSpin
                tvTitle.isSelected = true

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