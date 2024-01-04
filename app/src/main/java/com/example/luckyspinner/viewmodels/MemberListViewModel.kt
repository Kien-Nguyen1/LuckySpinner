package com.example.luckyspinner.viewmodels

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.util.Constants
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MemberListViewModel : ViewModel() {
    var memberList = MutableLiveData<List<Member>>()
    val eventList = MutableLiveData<List<Event>>()
    var isAddingMemberSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    var isDeletingMemberSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    var isEdtingMemberSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    val isShowProgressDialog = MutableLiveData<Boolean>()

    private val db = FirebaseFirestore.getInstance()


    suspend fun  getMembers(idChannel : String) {
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

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                }
            }
    }
     fun  getEvents(idChannel : String) {
         DataController.getEvents(db, idChannel)
            .addOnCompleteListener {
                val list : MutableList<Event> = ArrayList()
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
                    eventList.value = list
                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    viewModelScope.launch {
                        getEvents(idChannel)
                    }
                }
            }
    }

    fun deleteMember(idChannel : String, idMember : String) {
        isShowProgressDialog.postValue(true)
        DataController.deleteMember(db, idChannel, idMember)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully deleted!"
                )
                isDeletingMemberSuccess.value = true
                isShowProgressDialog.value = false
                viewModelScope.launch {
                    getMembers(idChannel)
                }
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e)
                isDeletingMemberSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
    fun addMember(idChannel: String, member: Member) {
        isShowProgressDialog.postValue(true)
        DataController.saveMember(db, idChannel, member)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isShowProgressDialog.value = false
                    isAddingMemberSuccess.value = true
                }
            }
            .addOnFailureListener {
                isAddingMemberSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
    fun editMember(idChannel: String, member: Member) {
        isShowProgressDialog.postValue(true)
        DataController.saveMember(db, idChannel, member)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isEdtingMemberSuccess.value = true
                    isShowProgressDialog.value = false
                }
            }
            .addOnFailureListener {
                isEdtingMemberSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
     fun updateCheckBoxForMember(idChannel: String, idMember: String, isSelected : Boolean) = viewModelScope.launch(Dispatchers.IO){
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .document(idMember)
            .update("hasSelected", !isSelected )
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully update!"
                )
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error updating document", e)
            }

    }

    suspend fun updateForChooseAllMember(idChannel: String, list : List<Member>, isSelected : Boolean) = viewModelScope.launch(Dispatchers.IO){
        withContext(Dispatchers.Main) {
            isShowProgressDialog.value = true
        }
        list.forEachIndexed { index, member ->
            if (member.hasSelected == isSelected) {
                if (index == list.lastIndex) viewModelScope.launch{getMembers(idChannel)}
                return@forEachIndexed
            }
            db.collection(Constants.FS_LIST_CHANNEL + "/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
                .document(member.idMember)
                .update("hasSelected", isSelected)
                .addOnSuccessListener {
                    Log.d(
                        Constants.FIRE_STORE,
                        "DocumentSnapshot successfully update!"
                    )
                    if (index == list.lastIndex) {
                        isShowProgressDialog.value =false
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error updating document",
                        e
                    )
                    isShowProgressDialog.value =false
                }
        }
    }
}