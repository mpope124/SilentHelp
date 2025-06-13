package com.silenthelp.ui.fakecall

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.silenthelp.R

class FakeCallActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_call)

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        findViewById<Button>(R.id.btnAnswer).setOnClickListener {
            stopRingtone()
            startActivity(Intent(this, FakeCallActiveActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnDecline).setOnClickListener {
            stopRingtone()
            finish()
        }
    }

    private fun stopRingtone() {
        try {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        } catch (e: IllegalStateException) {
            Log.e("FakeCall", "MediaPlayer state error: ${e.message}")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}
