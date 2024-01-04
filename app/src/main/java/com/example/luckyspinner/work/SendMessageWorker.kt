package com.example.luckyspinner.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.luckyspinner.controller.DataController
import com.example.luckyspinner.models.ElementSpinner
import com.example.luckyspinner.models.Event
import com.example.luckyspinner.models.Member
import com.example.luckyspinner.models.Spinner
import com.example.luckyspinner.network.TelegramApiClient
import com.example.luckyspinner.util.Constants
import com.example.luckyspinner.util.Constants.CHAT_ID
import com.example.luckyspinner.util.Constants.ID_EVENT_KEY
import com.example.luckyspinner.util.Constants.ID_TELEGRAM_CHANNEL_KEY
import com.example.luckyspinner.util.Constants.MESSAGE
import com.example.luckyspinner.util.makeStatusNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.random.Random

class SendMessageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private lateinit var outputData : Data

    val db = FirebaseFirestore.getInstance()
    val sList : MutableList<Spinner> = ArrayList()
    val mList : MutableList<Member> = ArrayList()
    val telegramChannelId = inputData.getString(ID_TELEGRAM_CHANNEL_KEY)
    val channelId = inputData.getString(Constants.ID_CHANNEL_KEY)
    val eventId = inputData.getString(ID_EVENT_KEY)
    val deviceId = inputData.getString(Constants.DEVICE_ID_KEY)
    var messageSpinner = "1"
    var messageMember = "1"
    var hasHandleRandomSpinner : Boolean? = null
    var hasHandleRandomMember : Boolean = false
    var hasGetListDay : Boolean = false

    var countDelay = 0
    lateinit var listDay : List<Int>
    var event : Event? = null


    override suspend fun doWork(): Result {


        suspend fun sendMessage() : Result {
            var hasHandleElement = false
            hasGetListDay = getListDayFromEvent(channelId, eventId.toString())
            hasHandleRandomMember = getMembers(channelId, eventId)
            val hasGetEvent = getSpinnerFromEvent(channelId, eventId)
            if (hasGetEvent) {
                hasHandleElement = handleForLoopElement()
            }


            println("Here do work go $hasHandleElement")
            return try {
                withContext(Dispatchers.Main) {
                    if (hasHandleElement && hasGetListDay && hasHandleRandomMember && hasGetEvent && hasHandleElement) {
                        if (!isSendMessageToDay()) {
                            return@withContext Result.success()
                        }
                        val message = messageMember + messageSpinner
                        println("Here come fail fake $telegramChannelId")

                        val respond = TelegramApiClient.telegramApi.sendMessage(telegramChannelId!!, message)
                        if (!respond.isSuccessful) {
                            return@withContext Result.failure(
                                workDataOf(MESSAGE to "Incorrect chat Id! Please make sure your group already add the LifeSpinBot")
                            )
                        }
                        outputData = workDataOf(CHAT_ID to telegramChannelId, MESSAGE to message)
                        makeStatusNotification("Send message successfully!", applicationContext)
                        DataController.saveEvent(db, channelId!!, event!!.apply {
                            isTurnOn = false
                        })
                        event!!.typeEvent?.let {
                            DataController.deleteEvent(db, channelId, eventId!!)
                        }
                        return@withContext Result.success(outputData)
                    } else {
                        return@withContext Result.failure()
                    }
                }
            } catch (e: Exception) {
                println(e)
                print("${Log.e("TAG", e.message.toString())}")
                return Result.failure(
                    workDataOf(MESSAGE to e.message.toString())
                )
            }
        }
        return sendMessage()

    }

    fun isSendMessageToDay() : Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
//        return true//moke
        return listDay.contains(today)
    }
    suspend fun  getElement(idChannel : String?, idSpinner : String?, spinnerName : String, isLast : Boolean) : Boolean {
        val list = ArrayList<ElementSpinner>()

        try {
            val documentQuery = db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
                .get()
                .await()
            for (document : QueryDocumentSnapshot in documentQuery) {
                Log.d(
                    Constants.FIRE_STORE,
                    document.id + " => " + document.data
                )
                if (document.exists()) {
                    val  e = document.toObject<ElementSpinner>()
                    list.add(e)
                }
            }
            if (list.isEmpty()) {
                messageSpinner += "\n For $spinnerName : No element in list."
            } else {
                val randomInt = Random.nextInt(list.size)
                messageSpinner += "\n For $spinnerName : ${list[randomInt].nameElement} is random chooser."
            }
            return true
        } catch (e : Exception) {
            return false
        }
    }
    suspend fun handleForLoopElement() : Boolean {
        return withContext(Dispatchers.Default) {
            var isDone = true
            sList.forEachIndexed { index, spinner ->
                val hasDone = getElement(channelId, spinner.idSpin, spinner.titleSpin, index == sList.size - 1)
                if (!hasDone) {
                    isDone = false
                }
            }
            return@withContext isDone
        }
    }
    suspend fun getSpinnerFromEvent(idChannel: String?, idEvent: String?) : Boolean {
        try {
            val document = db.collection(Constants.FS_LIST_CHANNEL+"/$deviceId/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}")
                .get()
                .await()
            println("Here come document ${document.size()}")
            for (doc : QueryDocumentSnapshot in document) {
                Log.d(
                    Constants.FIRE_STORE,
                    doc.id + " => " + doc.data
                )
                if (doc.exists()) {
                    val  s = doc.toObject<Spinner>()
                    if (s.listEvent.contains(idEvent) || event?.typeEvent == null) {
                        println("Here come containsss $idEvent")
                        sList.add(s)
                    }
//                    if (s.hasSelected) {
//                        sList.add(s)
//                        println("Here we come get spinner")
//                    }
                }
            }
            return true
        } catch (e : Exception) {
            return false
        }
    }

    suspend fun  getMembers(idChannel : String?, idEvent: String?) : Boolean {
        try {
            val documents = db.collection(Constants.FS_LIST_CHANNEL+"/$deviceId/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
                .get()
                .await()
            println("Here we come documents ${documents.size()}")
            documents.forEachIndexed { index, document ->
                Log.d(
                    Constants.FIRE_STORE,
                    document.id + " => " + document.data
                )
                if (document.exists()) {
                    val m = document.toObject<Member>()
                    if (m.listEvent.contains(idEvent) || event?.typeEvent == null)  {
                        mList.add(m)
                    }
                }
                if (index == documents.size() - 1) {
                    if (mList.isEmpty()) {
                        messageMember = "No members choosen in list"
                    } else {
                        //                    hasHandleRandomMember = true
                        val randomInt = Random.nextInt(mList.size)
                        messageMember = "The random member : ${mList[randomInt].nameMember}"
                    }
                }
            }
            return true
        } catch (e : Exception) {
            return  false
        }
    }
    suspend fun  getListDayFromEvent(idChannel : String?, eventId : String) : Boolean {
            try {
                val document = db.collection(Constants.FS_LIST_CHANNEL+"/$deviceId/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
                    .document(eventId)
                    .get()
                    .await()
                event = document.toObject<Event>()

                listDay = if (event != null) {
                    if (event!!.typeEvent == null || event!!.typeEvent == Constants.ONCE) {
                        arrayListOf(2,3,4,5,6,7,1)
                    }
                    else event!!.listDay
                } else {
                    ArrayList()
                }
                return true
            } catch (e : Exception) {
                return false
            }



//                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        Log.d(
//                            Constants.FIRE_STORE,
//                            it.result.id + " => " + it.result.data
//                        )
//                        if (it.result.exists()) {
//                            val  e = it.result.toObject<Event>()
//                            listDay = if (e != null) {
//                                if (e.typeEvent == null) arrayListOf(2,3,4,5,6,7,1)
//                                else e.listDay
//                            } else {
//                                ArrayList()
//                            }
//
//                            return true
//
//                            hasGetListDay = true
//                        }
//                    } else {
//                        Log.w(
//                            Constants.FIRE_STORE,
//                            "Error getting documents.",
//                            it.exception
//                        )
//                        hasGetListDay = false
////                    CoroutineScope(Dispatchers.IO).launch {
////                        getListDayFromEvent(idChannel, eventId)
////                    }
//                    }
//
//                }
    }
}