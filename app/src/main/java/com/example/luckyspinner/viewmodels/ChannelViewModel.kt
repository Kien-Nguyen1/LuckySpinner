package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject

class ChannelViewModel : ViewModel() {
    var channelList = MutableLiveData<List<Event>>()
    val db = FirebaseFirestore.getInstance()
    val isShowProgressDialog = MutableLiveData<Boolean>()


    fun  getEvents(idChannel : String) {
        isShowProgressDialog.value = true
        val list : MutableList<Event> = ArrayList()

        DataController.getEvents(db, idChannel)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  e = document.toObject<Event>()
                            if (e.typeEvent != null) {
                                list.add(e)
                            }
                        }
                    }
                    channelList.value = list
                    isShowProgressDialog.value = false

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    isShowProgressDialog.value = false
                }

            }
    }

    fun  deleteEvent(idChannel: String, idEvent : String) {

        DataController.deleteEvent(db, idChannel, idEvent)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully deleted!"
                )
                isShowProgressDialog.value = false
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e)
                isShowProgressDialog.value = false
            }

    }
    fun getEventFromFirestore(doc : DocumentSnapshot) : Event {
        val HOUR_EVENT_KEY = "hourElement"
        val TYPE_EVENT_KEY = "typeEvent"
        val MINUTE_EVENT_KEY = "minuteEvent"
        doc.data!!.let {
            val id = doc.id
            val type = it[TYPE_EVENT_KEY].toString().toInt()
            val hour = it[HOUR_EVENT_KEY].toString().toInt()
            val minute = it[MINUTE_EVENT_KEY].toString().toInt()
            return Event(id, type, hour, minute, ArrayList(1))
        }

    }
}