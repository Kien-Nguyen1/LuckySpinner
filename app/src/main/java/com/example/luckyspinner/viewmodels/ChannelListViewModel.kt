package com.example.luckyspinner.viewmodels

import android.provider.Settings
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelListViewModel : ViewModel() {
    var channelList = MutableLiveData<List<Channel>>()


    val db = FirebaseFirestore.getInstance()


     fun  getChannels(id : String) {
        val cList : MutableList<Channel> = ArrayList()

        db.collection(Constants.FS_LIST_CHANNEL+"/$/$")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        val  c = Channel.getChannelFromFirestore(document)
                        cList.add(c)
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


}