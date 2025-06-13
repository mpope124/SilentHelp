// Created by Kelley Rosa 06-07-25
package com.silenthelp.service

import android.content.Context
import android.widget.Toast
import com.silenthelp.model.Contact

object NotificationService {
    fun sendAlert(context: Context, contact: Contact, level: Int, message: String) {
        Toast.makeText(context, "Message sent to ${contact.name}", Toast.LENGTH_LONG).show()
        // Here you would normally trigger SMS sending logic
    }
}
