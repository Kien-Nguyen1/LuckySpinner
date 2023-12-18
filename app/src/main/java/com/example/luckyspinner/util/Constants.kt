@file:JvmName("Constants")
package com.example.luckyspinner.util

object Constants {
    const val BASE_URL = "https://api.telegram.org/bot"
    const val TOKEN = "6827789799:AAE1Ii3BqYQmFGLFrsbY-QLvvqAW12J60IA/"

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
    const val LIST_MEMBER_ID = "LIST_MEMBER_ID"
    const val LIST_MEMBER_NAME = "LIST_MEMBER_NAME"
    const val LIST_SPINNER_ID = "LIST_SPINNER_ID"
    const val LIST_SPINNER_NAME = "LIST_SPINNER_NAME"


    const val TAG_OUTPUT = "TAG_OUTPUT"
    const val FS_LIST_CHANNEL = "listchannel"
    const val FIRE_STORE = "Fire Store"
    const val FS_USER_CHANNEL = "channels"
    const val FS_USER_SPINNER = "spinners"
    const val FS_USER_MEMBER = "members"
    const val FS_USER_EVENT = "events"
    const val FS_USER_ELEMENT_SPINNER = "elementSpinners"

    const val EMPTY_STRING = ""
    const val ID_SPINNER_KEY = "ID_SPINNER_KEY"
    const val ID_CHANNEL_KEY = "ID_CHANNEL_KEY"
    const val ID_EVENT_KEY = "ID_EVENT_KEY"
    const val CHANNEL_NAME = "CHANNEL_NAME"
    const val SPINNER_TITLE = "SPINNER_TITLE"

    const val MONDAY = 2
    const val TUESDAY = 3
    const val WEDNESDAY = 4
    const val THURSDAY = 5
    const val FRIDAY = 6
    const val SATURDAY = 7
    const val SUNDAY = 1

    const val EVENT_TYPE_ONCE = 0
    const val EVENT_TYPE_EVERY_DAY = 1

    const val DELAY_TIME_MILLIS: Long = 3000
    lateinit var DEVICE_ID : String
}