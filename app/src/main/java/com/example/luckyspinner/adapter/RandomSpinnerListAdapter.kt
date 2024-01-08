package com.example.luckyspinner.adapter


import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.luckyspinner.databinding.RandomSpinnerListItemBinding
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Function.addMarginToLastItem
import kotlin.math.roundToInt

class RandomSpinnerListAdapter(private val listener: Listener, private val eventId : String) : RecyclerView.Adapter<RandomSpinnerListAdapter.SpinnerListViewHolder>() {

    private lateinit var context: Context


    interface Listener {
        fun onItemClick(id: String)
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

    private fun dpToPx(dp: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    override fun onBindViewHolder(holder: SpinnerListViewHolder, position: Int) {
        if (position == itemCount - 1) {
            val marginLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            val marginInDp = dpToPx(10)
            marginLayoutParams.bottomMargin = marginInDp
        }

        holder.binding.apply {
            val spinner = spinners[position]
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
                listener.onItemClick(spinner.idSpin)
            }
        }
    }
}