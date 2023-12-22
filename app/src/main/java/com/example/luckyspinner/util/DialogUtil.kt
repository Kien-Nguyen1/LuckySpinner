package com.example.luckyspinner.util

import android.app.AlertDialog
import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object DialogUtil {
        suspend fun showYesNoDialog(context : Context?) : Boolean {
            return suspendCancellableCoroutine {
                val builder = AlertDialog.Builder(context)

                builder.setTitle("Confirmation")
                builder.setMessage("Do you want to delete?")

                // Set up the buttons
                builder.setPositiveButton("Yes") { dialog, which ->
                    // User clicked Yes, handle the action
                    // Add your code here to handle the positive button click
                    // For example, you can perform some action or dismiss the dialog
                    dialog.dismiss()
                    it.resume(true)
                }

                builder.setNegativeButton("No") { dialog, which ->
                    // User clicked No, handle the action or dismiss the dialog
                    // Add your code here to handle the negative button click
                    dialog.dismiss()
                    it.resume(false)
                }

                // Create and show the dialog
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

    }

}