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
    const val EMPTY_STRING = ""

    const val DELAY_TIME_MILLIS: Long = 3000
}