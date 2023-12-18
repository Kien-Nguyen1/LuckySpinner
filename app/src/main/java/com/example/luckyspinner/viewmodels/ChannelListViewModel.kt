package com.example.luckyspinner.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.FS_USER_CHANNEL
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
    var isAddingSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    lateinit var context: Context
    var isDeleteSuccess  : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)

     fun  getChannels() {
        val cList : MutableList<Channel> = ArrayList()

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

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }

            }
    }
    fun  deleteChannel(id : String) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/$FS_USER_CHANNEL")
            .document(id)
            .delete()
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    Log.d(Constants.FIRE_STORE, "DocumentSnapshot successfully deleted!")
                    isDeleteSuccess.value = true
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    Log.w(Constants.FIRE_STORE, "Error deleting document", e)
                    isDeleteSuccess.value = false
                }
            })
    }

    fun addChannel(channelId: String, nameChannel: String) = viewModelScope.launch {
        val channel = Channel(channelId, nameChannel)
        db.collection(Constants.FS_LIST_CHANNEL + "/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}")
            .document(channelId)
            .set(channel)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isAddingSuccess.value = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                isAddingSuccess.value = false
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