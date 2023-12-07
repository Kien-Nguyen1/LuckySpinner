package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.FS_USER_CHANNEL
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class ChannelListViewModel : ViewModel() {
    var channelList = MutableLiveData<List<Channel>>()


    val db = FirebaseFirestore.getInstance()


     fun  getChannels(id : String) {
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
                            val  c = getChannelFromFirestore(document)
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

    fun getChannelFromFirestore(doc : DocumentSnapshot) : Channel {
        doc.data!!.let {
            val id = doc.id
            val name = it["nameChannel"].toString()
            return Channel(id, name)
        }
    }


}