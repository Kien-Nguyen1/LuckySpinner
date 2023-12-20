package com.example.luckyspinner.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
import kotlinx.coroutines.delay
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
    val deviceId = inputData.getString("deviceId")
    var messageSpinner = "1"
    var messageMember = "1"
    var hasHandleRandomSpinner : Boolean? = null
    var hasHandleRandomMember : Boolean = false
    var hasGetListDay : Boolean = false

    var countDelay = 0
    lateinit var listDay : List<Int>


    override suspend fun doWork(): Result {
        getListDayFromEvent(channelId)
        getMembers(channelId)
        getSpinnerFromEvent(channelId, eventId)

        suspend fun sendMessage() : Result {
            return try {
                withContext(Dispatchers.IO) {
                    if ((hasHandleRandomSpinner != true) || !hasHandleRandomMember || !hasGetListDay) {
                        delay(1000)
                        if (++countDelay > 10) {
                            return@withContext Result.retry()
                        }
                        return@withContext sendMessage()
                    }
                    if (!isSendMessageToDay()) {
                        return@withContext Result.success()
                    }
                    val message = messageMember + messageSpinner
                    val respond = TelegramApiClient.telegramApi.sendMessage(telegramChannelId!!, message)
                    if (!respond.isSuccessful) return@withContext Result.failure()
                    outputData = workDataOf(CHAT_ID to telegramChannelId, MESSAGE to message)
                    makeStatusNotification("Send message successfully!", applicationContext)
                    return@withContext Result.success(outputData)
                }
            } catch (e: Exception) {
                print("${Log.e("TAG", e.message.toString())}")
                return Result.failure()
            }
        }
        return sendMessage()

    }

    fun isSendMessageToDay() : Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return true//moke
        return listDay.contains(today)
    }
    fun  getElement(idChannel : String?, idSpinner : String?, spinnerName : String, isLast : Boolean) {
        val list = ArrayList<ElementSpinner>()

        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_SPINNER}/$idSpinner/${Constants.FS_USER_ELEMENT_SPINNER}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("Here get elements come ${it.result.size()}")
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
                    if (list.size != 0) {
                        val randomInt = Random.nextInt(list.size)
                        messageSpinner += "\n For ${spinnerName} : ${list[randomInt].nameElement} is random chooser."
                    } else {
                        messageSpinner += "\n For  ${spinnerName} : Don't have any elements!!"
                    }
                    if (isLast) {
                        hasHandleRandomSpinner = (hasHandleRandomSpinner == null)
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
    fun handleForLoopElement() {
        sList.forEachIndexed { index, spinner ->
            getElement(channelId, spinner.idSpin, spinner.titleSpin, index == sList.size - 1)
        }
    }
    fun getSpinnerFromEvent(idChannel: String?, idEvent: String?) {
        db.collection(Constants.FS_LIST_CHANNEL+"/$deviceId/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}/$idEvent/${Constants.FS_USER_SPINNER}")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("Here we go getSpin")
                    println(it.result.size())
                    for (document : QueryDocumentSnapshot in it.result) {
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val  s = document.toObject<Spinner>()
                            if (s.hasSelected) sList.add(s)
                        }
                    }
                    handleForLoopElement()

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    getSpinnerFromEvent(idChannel, idEvent)
                }

            }
    }

    fun  getMembers(idChannel : String?) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_MEMBER}")
            .get()
            .addOnCompleteListener {
                println("Here come getMembers")
                println(it.result.size())
                if (it.result.size() == 0) {
                    hasHandleRandomMember = true
                    messageMember = "Can not get members"
                    return@addOnCompleteListener
                }
                if (it.isSuccessful) {
                    it.result.forEachIndexed { index, document ->
                        Log.d(
                            Constants.FIRE_STORE,
                            document.id + " => " + document.data
                        )
                        if (document.exists()) {
                            val m = document.toObject<Member>()
                            if (m.hasSelected)  mList.add(m)
                        }
                        if (index == it.result.size() - 1) {
                            hasHandleRandomMember = true
                            val randomInt = Random.nextInt(mList.size)
                            messageMember = "The random member : ${mList[randomInt].nameMember}"
                        }
                    }

                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    getMembers(idChannel)
                }
            }
    }
    fun  getListDayFromEvent(idChannel : String?) {
        db.collection(Constants.FS_LIST_CHANNEL+"/${Constants.DEVICE_ID}/${Constants.FS_USER_CHANNEL}/$idChannel/${Constants.FS_USER_EVENT}")
            .document(eventId.toString())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(
                        Constants.FIRE_STORE,
                        it.result.id + " => " + it.result.data
                    )
                    if (it.result.exists()) {
                        val  e = it.result.toObject<Event>()
                        listDay = e?.listDay ?: ArrayList()
                        hasGetListDay = true
                    }
                } else {
                    Log.w(
                        Constants.FIRE_STORE,
                        "Error getting documents.",
                        it.exception
                    )
                    getListDayFromEvent(idChannel)
                }

            }
    }
}