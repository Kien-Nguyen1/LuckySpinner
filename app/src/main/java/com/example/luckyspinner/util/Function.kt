package com.example.luckyspinner.util

import android.content.Context
import android.os.CountDownTimer
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.core.View
import java.util.Calendar

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
}