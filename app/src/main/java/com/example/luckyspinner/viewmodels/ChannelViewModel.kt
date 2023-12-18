package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject

class ChannelViewModel : ViewModel() {
    var eventList = MutableLiveData<List<Event>>()
    val db = FirebaseFirestore.getInstance()


    fun  getEvents(idChannel : String?) {
        val list : MutableList<Event> = ArrayList()

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  e = document.toObject<Event>()
                            list.add(e)
                        }

                    }
                    eventList.value = list

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }

            }
    }

    fun  deleteEvent(idChannel: String?, idEvent : String) {

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .document(idEvent)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully deleted!"
                )
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e) }

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