package com.example.luckyspinner.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.models.Channel
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpinnerListViewModel : ViewModel() {
    var spinnerList = MutableLiveData<List<Spinner>>()
    val eventList = MutableLiveData<List<Event>>()
    val isAddingSpinnerSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    val isEditingSuccess : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    val isDeletingSuccess : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    val isShowProgressDialog = MutableLiveData<Boolean>()


    val db = FirebaseFirestore.getInstance()


    fun  getSpinners(idChannel : String) {
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

    fun addSpinner(idChannel: String, spinner: Spinner) = viewModelScope.launch {
        this.launch(Dispatchers.Main) {
            isShowProgressDialog.value = true
        }

        DataController.saveSpinner(db, idChannel, spinner)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isAddingSpinnerSuccess.value = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                isAddingSpinnerSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
    fun editSpinner(idChannel: String, spinner: Spinner) = viewModelScope.launch {
        this.launch(Dispatchers.Main) {
            isShowProgressDialog.value = true
        }
        DataController.saveSpinner(db, idChannel, spinner)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    isEditingSuccess.value = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("error", e.message.toString())
                isEditingSuccess.value = false
                isShowProgressDialog.value = false
            }
    }

    fun deleteSpinner(idChannel : String, idSpinner : String) {
        isShowProgressDialog.value = true
        DataController.deleteSpinner(db, idChannel, idSpinner)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully deleted!"
                )
                isDeletingSuccess.value = true
            }
            .addOnFailureListener {
                e ->
                Log.w(Constants.FIRE_STORE, "Error deleting document", e)
                isDeletingSuccess.value = false
                isShowProgressDialog.value = false
            }
    }
}