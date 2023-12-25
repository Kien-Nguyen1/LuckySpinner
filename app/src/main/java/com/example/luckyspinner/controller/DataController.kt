package com.example.luckyspinner.controller

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.models.ElementSpinner
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DataController {
    fun getChannels(db: FirebaseFirestore) : Task<QuerySnapshot> {
        val task = db.collection(Constants.FS_LIST_CHANNEL + "/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}")
            .get()
        return task
    }

    fun  deleteChannel(db : FirebaseFirestore, channelId : String) : Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}")
            .document(channelId)
            .delete()
    }

    fun saveChannel(db : FirebaseFirestore,  channel: Channel) : Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL + "/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}")
            .document(channel.idChannel)
            .set(channel)
    }


    fun  getEvents(db : FirebaseFirestore, idChannel : String) : Task<QuerySnapshot> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .get()
    }

    fun  deleteEvent(db : FirebaseFirestore, idChannel: String, idEvent : String) : Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .document(idEvent)
            .delete()
    }

    //element in spin
    fun  getElement(db : FirebaseFirestore, idChannel : String, idSpinner : String) : Task<QuerySnapshot>{
     return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .get()
    }

    fun deleteElement(db: FirebaseFirestore, idChannel : String, idSpinner : String, idElement : String) : Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(idElement)
            .delete()
    }

    fun saveElement(db: FirebaseFirestore, idChannel : String, idSpinner: String, element: ElementSpinner): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(element.idElement)
            .set(element)
    }

     fun  getMembers(db: FirebaseFirestore, idChannel : String): Task<QuerySnapshot> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .get()
     }

    fun deleteMember(db: FirebaseFirestore, idChannel : String, idMember : String): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .document(idMember)
            .delete()

    }
    fun saveMember(db: FirebaseFirestore, idChannel: String, member: Member): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .document(member.idMember)
            .set(member)
    }

     fun updateCheckBoxForMember(db: FirebaseFirestore, idChannel: String, idMember: String, isSelected : Boolean): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .document(idMember)
            .update("hasSelected", !isSelected )
    }

    fun  getSpinners(db : FirebaseFirestore, idChannel : String): Task<QuerySnapshot> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}")
            .get()
    }

    fun saveSpinner(db: FirebaseFirestore, idChannel: String, spinner: Spinner): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}")
            .document(spinner.idSpin)
            .set(spinner)
    }

    fun deleteSpinner(db: FirebaseFirestore, idChannel : String, idSpinner : String): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}")
            .document(idSpinner)
            .delete()
    }

    fun  getSpinnerFromEvent(db: FirebaseFirestore, idChannel : String, idEvent : String): Task<QuerySnapshot> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_SPINNER}")
            .get()
    }

    fun  getMemberFromEvent(db: FirebaseFirestore, idChannel : String, idEvent : String): Task<QuerySnapshot> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_MEMBER}")
            .get()
    }

    fun saveSpinnerInEvent(db: FirebaseFirestore, idChannel: String, idEvent: String, spinner: Spinner): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_SPINNER}")
            .document(spinner.idSpin)
            .set(spinner)
    }

    fun saveEvent(db: FirebaseFirestore, idChannel: String, event : Event ): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .document(event.idEvent)
            .set(event)
    }

    fun getEvent(db: FirebaseFirestore, idChannel: String, idEvent : String ): Task<DocumentSnapshot> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .document(idEvent)
            .get()
    }

    fun saveMemberInEvent(db: FirebaseFirestore, idChannel: String, idEvent: String, member: Member): Task<Void> {
        return db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_MEMBER}")
            .document(member.idMember)
            .set(member)
    }

}