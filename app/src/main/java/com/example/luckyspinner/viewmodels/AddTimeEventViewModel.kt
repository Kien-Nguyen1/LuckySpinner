package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.ElementSpinner
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddTimeEventViewModel : ViewModel() {
    var memberList = MutableLiveData<List<Member>>()
    var spinnerList = MutableLiveData<List<Spinner>>()

    private val db = FirebaseFirestore.getInstance()

    fun  getMembers(idChannel : String?, idEvent : String?) {
        val list : MutableList<Member> = ArrayList()

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            list.add(document.toObject<Member>())
                        }
//                        val  s = Spinner.getSpinnerFromFirestore(document)
                    }
                    memberList.value = list

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }

            }
    }
    fun editEvent(idChannel: String?, event : Event) {

    }
    fun  getSpinnerFromEvent(idChannel : String?, idEvent : String?) {
        val sList : MutableList<Spinner> = ArrayList()
        val workList : MutableList<Spinner> = ArrayList()
        println("Here come from event")

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_SPINNER}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("getspin ${it.result.size()}")
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  s = document.toObject<Spinner>()
                            sList.add(s)
                            if (s.hasSelected) {
                                workList.add(s)
                            }
                        }

                    }
                    spinnerList.value = sList
                    if (sList.size == 0) getSpinnerFromChannel(idChannel, idEvent)

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }

            }
    }

    fun  getSpinnerFromChannel(idChannel : String?, idEvent: String?) {
        val sList : MutableList<Spinner> = ArrayList()

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  s = document.toObject<Spinner>()
                            sList.add(s)
                        }
                    }
                    spinnerList.value = sList
                    viewModelScope.launch(Dispatchers.IO) {
                        saveListSpinner(idChannel, idEvent)
                    }

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }

            }
    }
    fun saveListSpinner(idChannel: String?, idEvent: String?) {
        val sList  = spinnerList.value ?: return
        val collectionRef = db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_SPINNER}")
        sList.forEach {
            collectionRef.document(it.idSpin)
            .set(it)
            .addOnSuccessListener {
                println("Here come saveListSpinner success")

            }
            .addOnFailureListener {
                saveListSpinner(idChannel, idEvent)
            }
        }

    }
    fun checkBoxSpinner(idChannel: String?, idEvent : String, idSpinner : String, hasSelected : Boolean) = viewModelScope.launch(Dispatchers.IO) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_SPINNER}")
            .document(idSpinner)
            .update("hasSelected", !hasSelected)
            .addOnSuccessListener {
                println("Here come change success!$hasSelected")
                getSpinnerFromEvent(idChannel, idEvent)
            }
    }

    fun saveEvent(idChannel: String?, event : Event ) : Job = viewModelScope.launch(Dispatchers.IO) {

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .document(event.idEvent)
            .set(event)
            .addOnSuccessListener {  }
            .addOnFailureListener {
                saveEvent(idChannel, event)
            }
    }
}