package com.example.luckyspinner.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.fragments.AddChannelFragment
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.DEVICE_ID
import com.example.luckyspinner.util.Constants.FS_LIST_CHANNEL
import com.example.luckyspinner.util.Constants.FS_USER_CHANNEL
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class AddChannelViewModel : ViewModel() {
    private val database = Firebase.firestore
    var isSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    lateinit var context: Context
    fun addChannel(channelId: String, nameChannel: String) = viewModelScope.launch {
        val channel = Channel(channelId, nameChannel)
        database.collection(FS_LIST_CHANNEL + "/$DEVICE_ID/${FS_USER_CHANNEL}")
            .document(channelId)
            .set(channel)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isSuccess.value = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                isSuccess.value = false
            }
    }
}