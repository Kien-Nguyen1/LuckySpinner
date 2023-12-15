package com.example.luckyspinner.viewmodels

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luckyspinner.models.ElementSpinner
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.util.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElementListInSpinnerViewModel : ViewModel() {
    var elementList = MutableLiveData<List<ElementSpinner>>()


    val db = FirebaseFirestore.getInstance()
    lateinit var context: Context

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
                            val  e = document.toObject<ElementSpinner>()
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
//    fun getElementSpinnerFromFirestore(doc : DocumentSnapshot) : ElementSpinner {
//        val NAME_ELEMENT_SPINNER_KEY = "nameElement"
//        doc.data!!.let {
//                val id = doc.id
//                val name = it[NAME_ELEMENT_SPINNER_KEY].toString()
//                return ElementSpinner(id, name)
//            }
//
//    }

    fun deleteElement(idChannel : String?, idSpinner : String?, idElement : String) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(idElement)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully deleted!"
                )
            }
            .addOnFailureListener { e -> Log.w(Constants.FIRE_STORE, "Error deleting document", e) }
    }
    fun addElement(idChannel : String?, idSpinner : String?, elementSpinner: ElementSpinner, addDialog : Dialog) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .document(elementSpinner.idElement)
            .set(elementSpinner)
            .addOnSuccessListener {
                Log.d(
                    Constants.FIRE_STORE,
                    "DocumentSnapshot successfully add!"
                )
                addDialog.dismiss()
                Toast.makeText(context, "Add element Successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                e -> Log.w(Constants.FIRE_STORE, "Error add document", e)
                addDialog.dismiss()
                Toast.makeText(context, "Add element fail!", Toast.LENGTH_SHORT).show()
            }
    }
}