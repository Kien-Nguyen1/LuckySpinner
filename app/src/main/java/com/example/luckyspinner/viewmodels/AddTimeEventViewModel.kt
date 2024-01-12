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
import com.example.luckyspinner.util.Function.changeDayToPosition
import com.example.luckyspinner.util.Function.changeTheNumberOfDay
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTimeEventViewModel : ViewModel() {
    var memberList = MutableLiveData<List<Member>>()
    var spinnerList = MutableLiveData<List<Spinner>>()
    var event = MutableLiveData<Event>()
    val isShowProgressDialog = MutableLiveData<Boolean>()

    val isSearchingSpinner = MutableLiveData<Boolean?>()
    val isSearchingMember = MutableLiveData<Boolean?>()

    val isGettingSpinnerSuccess = MutableLiveData<Boolean?>()
    val isGettingEventSuccess = MutableLiveData<Boolean?>()
    val isSaveListSpinnerSuccess = MutableLiveData<Boolean>()
    val isSaveEventSuccess = MutableLiveData<Boolean?>()
    val isAddingSpinnerSuccess = MutableLiveData<Boolean?>()
    val isDeleteEventSuccess = MutableLiveData<Boolean>()




    private val db = FirebaseFirestore.getInstance()

    fun  getMembers(idChannel : String, isNewEvent : Boolean) {
        val list : MutableList<Member> = ArrayList()

        DataController.getMembers(db, idChannel)
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
                    }
                    memberList.value = list
                    if (isNewEvent){
                        allCheckboxMember(false)
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

    fun  getSpinnerFromChannel(idChannel : String, isNewEvent: Boolean) {
        //only call in add event once
        val sList : MutableList<Spinner> = ArrayList()
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
                    isGettingSpinnerSuccess.value = true
                    if (isNewEvent) {
                        allCheckboxSpinner(false)
                    }


                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    isGettingSpinnerSuccess.value = false
                }

            }
    }
    fun saveListSpinner(idChannel: String, idEvent: String) {
        val sList  = spinnerList.value!!
        println("Here come list $sList")
        println("Here come list"+idEvent)
        isShowProgressDialog.value = true
        sList.forEachIndexed { index, spinner ->
            DataController.saveSpinner(db, idChannel, spinner)
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
            DataController.saveMember(db, idChannel , member)
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
        if (hasSelected) {
            spinners[position].listEvent = spinners[position].listEvent.filter {
                it != event.value!!.idEvent
            }.toMutableList()
        } else {
            spinners[position].listEvent.add(event.value!!.idEvent)
        }
        spinnerList.value = spinners
    }
    fun checkBoxMember(position : Int, hasSelected : Boolean) {
        val members = memberList.value!!
        println("Here come $members")
        members[position].hasSelected = !hasSelected
        if (hasSelected) {
            members[position].listEvent = members[position].listEvent.filter {
                it != event.value!!.idEvent
            }.toMutableList()
        } else {
            members[position].listEvent.add(event.value!!.idEvent)
        }
        println("Here come $members")
        memberList.value = members
    }

    fun saveEvent(idChannel: String, event : Event, isTest : Boolean = false )  {
        DataController.saveEvent(db, idChannel, event)
            .addOnSuccessListener {
                if (!isTest) isSaveEventSuccess.value = true
            }
            .addOnFailureListener {
                saveEvent(idChannel, event)
                if (!isTest) isSaveEventSuccess.value = false
            }
    }
    fun getEvent(idChannel: String, idEvent : String?, newEventId : String = "") : Job = viewModelScope.launch(Dispatchers.IO) {
        if (idEvent == null) {
            this.launch(Dispatchers.Main) {
                event.value = Event(newEventId)
                handleClickDay(changeDayToPosition(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))
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

    fun editSpinner(idChannel: String, spinner: Spinner) {

    }

    fun addSpinner(idChannel: String, spinner: Spinner) {
        isShowProgressDialog.value = true
        DataController.saveSpinner(db, idChannel, spinner)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isAddingSpinnerSuccess.value = true
                    isShowProgressDialog.value = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                isAddingSpinnerSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
    fun handleClickDay(position: Int) {
        val event = event.value ?: return
        val listDay = event.listDay

        val dayNumber = if (listDay.contains(changeTheNumberOfDay(position))) 0 else changeTheNumberOfDay(position)
        listDay[position] = dayNumber
        this.event.value = event
    }

     fun allCheckboxSpinner(hasSelected: Boolean) {
        spinnerList.value!!.forEachIndexed { index, spinner ->
            val spinners = spinnerList.value!!
            spinners[index].hasSelected = !hasSelected
            if (hasSelected) {
                spinners[index].listEvent = spinners[index].listEvent.filter {
                    it != event.value!!.idEvent
                }.toMutableList()
            } else {
                spinners[index].listEvent.add(event.value!!.idEvent)
            }
            if (index == spinners.size - 1)     spinnerList.value = spinners
        }
    }
     fun allCheckboxMember(hasSelected: Boolean) {
         val members = memberList.value!!
         memberList.value!!.forEachIndexed { index, member ->
            members[index].hasSelected = !hasSelected
            if (hasSelected) {
                members[index].listEvent = members[index].listEvent.filter {
                    it != event.value!!.idEvent
                }.toMutableList()
            } else {
                members[index].listEvent.add(event.value!!.idEvent)
            }
            if (index == members.size - 1) {
                memberList.value = members
            }
        }
    }
}