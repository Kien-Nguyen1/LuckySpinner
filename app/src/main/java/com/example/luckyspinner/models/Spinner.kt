package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.google.firebase.firestore.DocumentSnapshot

data class Spinner (
    var idSpin : String = EMPTY_STRING,
    var titleSpin : String = EMPTY_STRING
) {
    companion object {
        const val NAME_SPINNER_KEY = "nameSpinner"
        fun getSpinnerFromFirestore(doc : DocumentSnapshot) : Spinner {
            if (doc.exists()) {
                doc.data?.let {
                    val  id = doc.id
                    val name = it[NAME_SPINNER_KEY].toString()
                    return Spinner(id, name)
                }
                return Spinner()
            } else {
                return Spinner()
            }
        }
    }

}