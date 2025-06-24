// BroadcastReceiver that handles scheduled alarms and triggers a fake call UI
package com.silenthelp.ui.fakecall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.silenthelp.R

class FakeCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        /** Log the time when the alarm is triggered */
        Log.d("FakeCallReceiver", "Alarm received at ${System.currentTimeMillis()}")

        /** Create an intent to launch the FakeCallActivity */
        val callIntent = Intent(context, FakeCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        /** Wrap the activity intent in a PendingIntent so it can be triggered by the notification */
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fake_call_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /* Create channel (required on Android 8+) */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fake Call Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for triggering fullscreen fake calls"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        /** Build the notification that simulates an incoming call */
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("Incoming Call")
            .setContentText("Tap to answer the call")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
