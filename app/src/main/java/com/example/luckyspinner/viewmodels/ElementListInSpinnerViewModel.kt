package com.example.luckyspinner.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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


    val db = FirebaseFirestore.getInstance()


    fun  getElement(idChannel : String?, idSpinner : String?) {
        val list : MutableList<ElementSpinner> = ArrayList()

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .get()
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

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
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

    fun deleteElement(idChannel : String?, idSpinner : String?, idElement : String) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(idElement)
            .delete()
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
            }
    }
    fun addElement(idChannel : String?, idSpinner: String?, element: ElementSpinner) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(element.idElement)
            .set(element)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully save!"
                )
                isAddingSuccess.value = true
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e)
            isAddingSuccess.value = false}
    }

    fun editElement(idChannel : String?, idSpinner: String?, element: ElementSpinner) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(element.idElement)
            .set(element)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully save!"
                )
                isEditingSuccess.value =true
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e)
            isEditingSuccess.value = false}
    }


}