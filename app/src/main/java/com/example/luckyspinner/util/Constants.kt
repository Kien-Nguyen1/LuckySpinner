@file:JvmName("Constants")
package com.example.luckyspinner.util

object Constants {
    const val BASE_URL = "https://api.telegram.org/bot"
    const val TOKEN = "TOKEN/"

    @JvmField
    val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
    const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"
    @JvmField
    val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
    const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 123

    const val CHAT_ID = "CHAT_ID"
    const val MESSAGE = "MESSAGE"
    const val TAG_OUTPUT = "TAG_OUTPUT"
    const val FS_LIST_CHANNEL = "listchannel"
    const val FIRE_STORE = "Fire Store"
    const val FS_USER_CHANNEL = "channels"
    const val FS_USER_SPINNER = "spinners"
    const val FS_USER_EVENT = "events"
    const val FS_USER_ELEMENT_SPINNER = "elementSpinners"

    const val EMPTY_STRING = ""
    const val ID_SPINNER_KEY = "ID_SPINNER_KEY"
    const val ID_CHANNEL_KEY = "ID_CHANNEL_KEY"
    const val ID_EVENT_KEY = "ID_EVENT_KEY"

    const val DELAY_TIME_MILLIS: Long = 3000
    lateinit var DEVICE_ID : String

    const val MONDAY = 1
    const val TUESDAY = 2
    const val WEDNESDAY = 3
    const val THURSDAY = 4
    const val FRIDAY = 5
    const val SATURDAY = 6
    const val SUNDAY = 7
}