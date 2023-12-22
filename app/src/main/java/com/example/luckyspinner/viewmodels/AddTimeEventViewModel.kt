package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddTimeEventViewModel : ViewModel() {
    var memberList = MutableLiveData<List<Member>>()
    var spinnerList = MutableLiveData<List<Spinner>>()
    var event = MutableLiveData<Event>()
    val isShowProgressDialog = MutableLiveData<Boolean>()

    val isGettingSpinnerSuccess = MutableLiveData<Boolean>()
    val isGettingEventSuccess = MutableLiveData<Boolean>()
    val isSaveListSpinnerSuccess = MutableLiveData<Boolean>()
    val isSaveEventSuccess = MutableLiveData<Boolean>()
    val isDeleteEventSuccess = MutableLiveData<Boolean>()




    private val db = FirebaseFirestore.getInstance()

    fun  getMembers(idChannel : String) {
        val list : MutableList<Member> = ArrayList()

        DataController.getMembers(db, idChannel)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("Here we come member ${it.result.size()}")

                    for (document : QueryDocumentSnapshot in it.result) {
                        println("Here we come member ${it.result.size()}")

                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            list.add(document.toObject<Member>())
                        }
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
    fun  getSpinnerFromEvent(idChannel : String, idEvent : String) {
        val sList : MutableList<Spinner> = ArrayList()

        viewModelScope.launch(Dispatchers.Main) {
            isShowProgressDialog.value = true
        }

        DataController.getSpinnerFromEvent(db, idChannel, idEvent)
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
                        }
                    }
                    spinnerList.value = sList
                    isShowProgressDialog.value = false
                    isGettingSpinnerSuccess.value = true

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    isShowProgressDialog.value = false
                    isGettingSpinnerSuccess.value = false

                }
            }
    }

    fun  getMemberFromEvent(idChannel : String, idEvent : String) {
        val list : MutableList<Member> = ArrayList()

        DataController.getMemberFromEvent(db, idChannel, idEvent)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  m = document.toObject<Member>()
                            list.add(m)
                        }
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

    fun  getSpinnerFromChannel(idChannel : String) {
        //only call in add event once
        val sList : MutableList<Spinner> = ArrayList()
        viewModelScope.launch(Dispatchers.Main) {
            isShowProgressDialog.value = true
        }
        DataController.getSpinners(db, idChannel)
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
                    isShowProgressDialog.value = false
                    isGettingSpinnerSuccess.value = true


                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    isShowProgressDialog.value = false
                    isGettingSpinnerSuccess.value = false
                }

            }
    }
    fun saveListSpinner(idChannel: String, idEvent: String) {
        val sList  = spinnerList.value ?: return
        isShowProgressDialog.value = true
        sList.forEachIndexed { index, spinner ->
            DataController.saveSpinnerInEvent(db, idChannel, idEvent, spinner)
                .addOnSuccessListener {
                    isShowProgressDialog.value = false
                    if (index == sList.size - 1) {
                        isSaveListSpinnerSuccess.value = true
                    }
                }
                .addOnFailureListener {
                    isShowProgressDialog.value = false
                    isSaveListSpinnerSuccess.value = false
                }
        }
    }

    fun saveListMember(idChannel: String, idEvent: String) {
        val memberList = memberList.value ?: return
        isShowProgressDialog.value = true
        memberList.forEachIndexed { index, member ->
            DataController.saveMemberInEvent(db, idChannel, idEvent, member)
                .addOnSuccessListener {
                    isShowProgressDialog.value = false
                    if (index == memberList.size - 1) {
                        isSaveListSpinnerSuccess.value = true
                    }
                }
                .addOnFailureListener {
                    isShowProgressDialog.value = false
                    isSaveListSpinnerSuccess.value = false
                }
        }

    }
    fun checkBoxSpinner(position : Int, hasSelected : Boolean) {
        val spinners = spinnerList.value!!
        spinners[position].hasSelected = !hasSelected
        spinnerList.value = spinners
    }
    fun checkBoxMember(position : Int, hasSelected : Boolean) {
        val members = memberList.value!!
        members[position].hasSelected = !hasSelected
        memberList.value = members
    }

    fun saveEvent(idChannel: String, event : Event )  {
        DataController.saveEvent(db, idChannel, event)
            .addOnSuccessListener {
                isSaveEventSuccess.value = true
            }
            .addOnFailureListener {
                saveEvent(idChannel, event)
                isSaveEventSuccess.value = false
            }
    }
    fun getEvent(idChannel: String, idEvent : String? ) : Job = viewModelScope.launch(Dispatchers.IO) {
        if (idEvent == null) {
            this.launch(Dispatchers.Main) {
                event.value = Event()
            }
        } else {
            DataController.getEvent(db, idChannel, idEvent)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.exists()) {
                            val e = it.result.toObject<Event>()
                            println("Here we come $e")
                            CoroutineScope(Dispatchers.Main).launch {
                                event.value = e ?: Event()
                                isGettingEventSuccess.value = true
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    isGettingEventSuccess.value = false
                }
        }
    }
}