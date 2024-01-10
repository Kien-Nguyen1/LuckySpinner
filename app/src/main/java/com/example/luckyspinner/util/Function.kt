package com.example.luckyspinner.util

import android.content.Context
import android.graphics.Rect
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import kotlin.math.roundToInt

object Function {
    fun removeObservers(listItem : List<MutableLiveData<*>>, owner: LifecycleOwner) {
        listItem.forEach { item ->
            item.removeObservers(owner)
        }

    }

    fun toNull(listItem: List<MutableLiveData<*>>) {
        listItem.forEach { item ->
            item.value = null
        }
    }
    fun changeTheNumberOfDay(position : Int) : Int {
        if (position == 6) return Constants.SUNDAY
        return position + 2
    }
    fun changeDayToPosition(day : Int) : Int {
        if (day == Calendar.SUNDAY) return  6
        return day - 2
    }

    fun showKeyBoard(context: Context?, editText : EditText) {
        editText.requestFocus()

        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyBoard(context: Context?, view : android.view.View ) {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun RecyclerView.addFabScrollListener(fab : FloatingActionButton) {
        val countDownTimer : CountDownTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                //
            }

            override fun onFinish() {
                if (!fab.isShown) {
                    fab.show()
                }
            }
        }

        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0 && !fab.isShown) {
                    fab.show()
                } else if (dy != 0 && fab.isShown) {
                    fab.hide()
                    countDownTimer.cancel()
                    countDownTimer.start()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    fun addMarginToLastItem(recyclerView: RecyclerView, marginInDp: Int) {
        recyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: android.view.View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    if (position == parent.adapter?.itemCount?.minus(1)) {
                        val marginInPx = dpToPx(view.context, marginInDp)
                        (view.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = marginInPx
                    }
                }
            }
        )
    }

//    fun addMarginToLastItemHorizontal(recyclerView: RecyclerView, marginInDp: Int) {
//        recyclerView.addItemDecoration(
//            object : RecyclerView.ItemDecoration() {
//                override fun getItemOffsets(
//                    outRect: Rect,
//                    view: android.view.View,
//                    parent: RecyclerView,
//                    state: RecyclerView.State
//                ) {
//                    val position = parent.getChildAdapterPosition(view)
//                    if (position == parent.adapter?.itemCount?.minus(1)) {
//                        val marginInPx = dpToPx(view.context, marginInDp)
//                        (view.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = marginInPx
//                    }
//                }
//            }
//        )
//    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    fun numberToMinuteForm(number : Int) : String {
        return if (number < 10) {
            "0$number"
        } else number.toString()
    }
}