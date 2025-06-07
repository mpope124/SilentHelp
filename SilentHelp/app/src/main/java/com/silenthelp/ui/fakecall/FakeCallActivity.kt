package com.silenthelp.ui.fakecall

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.media.Ringtone
import com.silenthelp.R
import com.silenthelp.ui.home.HomeActivity


class FakeCallActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var ringtone: Ringtone? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_call)

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        val btnAnswer = findViewById<Button>(R.id.btnAnswer)
        val btnDecline = findViewById<Button>(R.id.btnDecline)

        val goToHome = {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnAnswer.setOnClickListener { goToHome() }
        btnDecline.setOnClickListener { goToHome() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
