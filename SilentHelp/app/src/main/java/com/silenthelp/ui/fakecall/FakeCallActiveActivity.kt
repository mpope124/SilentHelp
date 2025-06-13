package com.silenthelp.ui.fakecall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.silenthelp.R
import com.silenthelp.listener.ThreatKeywordListener
import com.silenthelp.ui.home.HomeActivity
import com.silenthelp.manager.TranscriptLogger
import java.io.File

class FakeCallActiveActivity : AppCompatActivity() {

    private lateinit var keywordListener: ThreatKeywordListener

    // --- permission launcher -------------------------------------------------
    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startKeywordListener() else finish()   // bail if user denies
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_call_active)

        // Ask for RECORD_AUDIO at runtime on Android 6+
        if (needsMicPermission() &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startKeywordListener()
        }

        findViewById<Button>(R.id.btnHangUp).setOnClickListener {
            val alerted = if (::keywordListener.isInitialized)
                keywordListener.getContactsAlerted().map { it.name }.toTypedArray()
            else emptyArray()

            // Return to a single HomeActivity instance with info on whom we messaged
            startActivity(Intent(this, HomeActivity::class.java).apply {
                putExtra("alerted_contacts", alerted)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        }
    }

    private fun startKeywordListener() {
        TranscriptLogger(this).append("*** Call started ***")
        keywordListener = ThreatKeywordListener(this).also { it.startListening() }
    }

    private fun needsMicPermission() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    override fun onDestroy() {
        if (::keywordListener.isInitialized) keywordListener.stopListening()
        super.onDestroy()
    }

}
