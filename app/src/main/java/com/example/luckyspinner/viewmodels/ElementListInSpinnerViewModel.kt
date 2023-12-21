package com.example.luckyspinner.viewmodels

import android.provider.ContactsContract.Data
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.models.ElementSpinner
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class ElementListInSpinnerViewModel : ViewModel() {
    var elementList = MutableLiveData<List<ElementSpinner>>()
    var isAddingSuccess: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    var isDeleteSuccess  : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    var isEditingSuccess : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>()
    val isShowProgressDialog = MutableLiveData<Boolean>()


    val db = FirebaseFirestore.getInstance()


    fun  getElement(idChannel : String, idSpinner : String) {
        isShowProgressDialog.value = true
        val list : MutableList<ElementSpinner> = ArrayList()

        DataController.getElement(db, idChannel, idSpinner)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  e = getElementSpinnerFromFirestore(document)
                            list.add(e)
                        }
                    }
                    elementList.value = list
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
    fun getElementSpinnerFromFirestore(doc : DocumentSnapshot) : ElementSpinner {
        val NAME_ELEMENT_SPINNER_KEY = "nameElement"
        doc.data!!.let {
                val id = doc.id
                val name = it[NAME_ELEMENT_SPINNER_KEY].toString()
                return ElementSpinner(id, name)
            }

    }

    fun deleteElement(idChannel : String, idSpinner : String, idElement : String) {
        isShowProgressDialog.value = true

        DataController.deleteElement(db, idChannel, idSpinner, idElement)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully deleted!"
                )
                isDeleteSuccess.value = true
            }
            .addOnFailureListener {
                e ->
                Log.w(Constants.FIRE_STORE, "Error deleting document", e)
                isDeleteSuccess.value =false
                isShowProgressDialog.value = false

            }
    }
    fun addElement(idChannel : String, idSpinner: String, element: ElementSpinner) {
        isShowProgressDialog.value = true

        DataController.saveElement(db, idChannel, idSpinner, element)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully save!"
                )
                isAddingSuccess.value = true
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e)
            isAddingSuccess.value = false
                isShowProgressDialog.value = false
            }
    }

    fun editElement(idChannel : String, idSpinner: String, element: ElementSpinner) {
        isShowProgressDialog.value = true

        DataController.saveElement(db, idChannel, idSpinner, element)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully save!"
                )
                isEditingSuccess.value = true
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e)
            isEditingSuccess.value = false
                isShowProgressDialog.value = false
            }
    }


}