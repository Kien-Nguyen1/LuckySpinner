package com.example.luckyspinner.models

import com.example.luckyspinner.util.Constants.EMPTY_STRING
import com.google.firebase.firestore.DocumentSnapshot

data class Channel(
    var idChannel : String = EMPTY_STRING,
    var nameChannel : String = EMPTY_STRING
) {

    companion object {
        const val NAME_CHANNEL_KEY = "nameChannel"
        fun getChannelFromFirestore(doc : DocumentSnapshot) : Channel {
            if (doc.exists()) {
                doc.data?.let {
                    val  id = doc.id
                    val name = it[NAME_CHANNEL_KEY].toString()
                    return Channel(id, name)
                }
                return Channel()
            } else {
                return Channel()
            }
        }
    }
}