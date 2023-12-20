package com.example.luckyspinner.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.FS_USER_CHANNEL
import com.example.luckyspinner.util.Constants.MESSAGE_DELETE_FAILED
import com.example.luckyspinner.util.Constants.MESSAGE_DELETE_SUCCESSFUL
import com.example.luckyspinner.util.Constants.MESSAGE_SAVE_FAILED
import com.example.luckyspinner.util.Constants.MESSAGE_SAVE_SUCCESSFUL
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch

class ChannelListViewModel : ViewModel() {
    var channelList = MutableLiveData<List<Channel>>()
    val db = FirebaseFirestore.getInstance()
    var message = MutableLiveData<String>()
    var isAddingSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    var isDeleteSuccess  : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    var isEditingSuccess : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    val isShowProgressDialog = MutableLiveData<Boolean>()

    fun  getChannels() {
        val cList : MutableList<Channel> = ArrayList()
        isShowProgressDialog.value = true
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/$FS_USER_CHANNEL")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val c = document.toObject<Channel>()
                            cList.add(c)
                        }
                    }
                    channelList.value = cList
                    isShowProgressDialog.value = false

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    message.value = Constants.MESSAGE_GET_FAILED
                    isShowProgressDialog.value = false
                }

            }

    }
    fun  deleteChannel(id : String) {
        isShowProgressDialog.value = true
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/$FS_USER_CHANNEL")
            .document(id)
            .delete()
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    Log.d(Constants.FIRE_STORE, "DocumentSnapshot successfully deleted!")
                    message.value = Constants.MESSAGE_DELETE_SUCCESSFUL
                    isDeleteSuccess.value = true
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    Log.w(Constants.FIRE_STORE, "Error deleting document", e)
                    isDeleteSuccess.value = false
                    message.value = MESSAGE_DELETE_FAILED
                    isShowProgressDialog.value = false

                }
            })
    }

    fun addChannel(channel: Channel) = viewModelScope.launch {
        isShowProgressDialog.value = true
        db.collection(Constants.FS_LIST_CHANNEL + "/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}")
            .document(channel.idChannel)
            .set(channel)
            .addOnSuccessListener {
                message.value = MESSAGE_SAVE_SUCCESSFUL
                isAddingSuccess.value = true

            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                message.value = MESSAGE_SAVE_FAILED
                isAddingSuccess.value = false
                isShowProgressDialog.value = false

            }
    }
    fun editChannel(channel: Channel) = viewModelScope.launch {
        isShowProgressDialog.value = true

        db.collection(Constants.FS_LIST_CHANNEL + "/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}")
            .document(channel.idChannel)
            .set(channel)
            .addOnSuccessListener {
                message.value = MESSAGE_SAVE_SUCCESSFUL
                isEditingSuccess.value = true

            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                message.value = MESSAGE_SAVE_FAILED
                isEditingSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
    fun getChannelFromFirestore(doc : DocumentSnapshot) : Channel {
        doc.data!!.let {
            val id = doc.id
            val name = it["nameChannel"].toString()
            return Channel(id, name)
        }
    }
}