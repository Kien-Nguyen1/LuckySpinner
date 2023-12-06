package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.google.firebase.firestore.DocumentSnapshot

data class Channel(

    val idChannel : String = EMPTY_STRING,

    val nameChannel : String = EMPTY_STRING
) {

    companion object {
        fun getChannelFromFirestore(doc : DocumentSnapshot) : Channel {
            if (doc.exists()) {
                doc.data?.let {
                    val  id = doc.id
                    val name = it["nameChannel"].toString()
                    return Channel(id, name)
                }
                return Channel()
            } else {
                return Channel()
            }
        }
    }
}