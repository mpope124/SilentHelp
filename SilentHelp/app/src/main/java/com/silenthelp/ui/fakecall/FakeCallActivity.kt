// Simulates an incoming call with ringtone. On answer transitions to the active fake call, on decline simply closes.
// Created By Carmelo Vera
package com.silenthelp.ui.fakecall

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R

class FakeCallActivity : AppCompatActivity() {

    /** Plays the looping ringtone when the fake call screen is shown */
    private lateinit var mediaPlayer: MediaPlayer

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Flags for Fake Call Receiver */
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        /** Set Layout for ACTIVE fake call */
        setContentView(R.layout.activity_fake_call)

        /** Logcat notification of launch */
        Log.d("FakeCallActivity", "FakeCallActivity launched!")

        /** Initialize and start the ringtone in a loop */
        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        // =========================================================================
        // BUTTON HANDLERS
        // =========================================================================
        /** Answer button: stop ringtone, launch the active-call screen, and finish */
        findViewById<Button>(R.id.btnAnswer).setOnClickListener {
            stopRingtone()
            startActivity(Intent(this, FakeCallActiveActivity::class.java))
            finish()
        }
        /** Decline button: stop ringtone and close this activity */
        findViewById<Button>(R.id.btnDecline).setOnClickListener {
            stopRingtone()
            finish()
        }
    }

    // =========================================================================
    // RINGTONE CONTROL
    // =========================================================================
    /** Safely stops and releases the MediaPlayer */
    private fun stopRingtone() {
        try {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        /* Catches IllegalStateException if the player is in an invalid state */
        } catch (e: IllegalStateException) {
            Log.e("FakeCall", "MediaPlayer state error: ${e.message}")
        }
    }

    /** When activity is destroyed, end ringtone  */
    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}
