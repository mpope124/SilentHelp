package com.silenthelp.ui.fakecall

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager
import com.silenthelp.voice.KeywordDetector
import com.silenthelp.voice.MicWrapper
import com.silenthelp.core.ThreatPolicy


class FakeCallActiveActivity : AppCompatActivity() {

    private lateinit var tvTranscript: TextView

    private lateinit var settings: SettingsManager
    private var mic: MicWrapper? = null
    private lateinit var l1Detector: KeywordDetector
    private lateinit var l2Detector: KeywordDetector

    private val contactsAlerted = mutableSetOf<String>()
    private var highestLevel = 0
    private var lastLocation: Location? = null

    //==========================================================================
    // Permission Launcher
    //==========================================================================
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_FINE_LOCATION]   == true
        ) getLastLocation()

        if (result[Manifest.permission.RECORD_AUDIO] == true) {
            initEngine()
        } else {
            tvTranscript.text = "Microphone permission denied."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_call_active)
        supportActionBar?.hide()

        tvTranscript = findViewById(R.id.tvTranscript)
        settings = SettingsManager(this)

        seedKeywordsOnce()
        l1Detector = KeywordDetector(settings.getKeywords(1))
        l2Detector = KeywordDetector(settings.getKeywords(2))

        findViewById<Button>(R.id.btnHangUp).setOnClickListener { endCall() }

        requestPermissionsIfNeeded()
    }

    override fun onDestroy() {
        mic?.stop()
        super.onDestroy()
    }

    private fun requestPermissionsIfNeeded() {
        val permsNeeded = mutableListOf<String>()

        if (needsPerm(Manifest.permission.RECORD_AUDIO))
            permsNeeded += Manifest.permission.RECORD_AUDIO

        // coarse is enough for lat/lon snapshot
        if (needsPerm(Manifest.permission.ACCESS_COARSE_LOCATION))
            permsNeeded += Manifest.permission.ACCESS_COARSE_LOCATION

        if (permsNeeded.isEmpty()) {
            getLastLocation()
            initEngine()
        } else {
            permLauncher.launch(permsNeeded.toTypedArray())
        }
    }

    private fun needsPerm(name: String) =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, name) != PackageManager.PERMISSION_GRANTED

    private fun initEngine() {
        tvTranscript.text = "Ready…"

        mic = MicWrapper(
            context   = this,
            onReady   = { runOnUiThread { tvTranscript.text = "Listening…" } },
            onPartial = { txt -> handleTranscript(txt, false) },
            onFinal   = { txt -> handleTranscript(txt, true) },
            onErr     = { code -> runOnUiThread {
                tvTranscript.text = "Error $code – speak again"
            } }
        ).also { it.start() }
    }

    private fun handleTranscript(text: String, isFinal: Boolean) {
        runOnUiThread {
            tvTranscript.text = (if (isFinal) "Final:   " else "Partial: ") + text
        }

        when {
            l2Detector.detect(text).isNotEmpty() -> handleHit(2)
            l1Detector.detect(text).isNotEmpty() -> handleHit(1)
        }
    }

    private fun handleHit(level: Int) {
        highestLevel = maxOf(highestLevel, level)

        settings.getContactForLevel(level)?.let { contact ->
            contactsAlerted += contact.name
        }

        val toast = if (level == 1)
            "Level-1 keyword detected"
        else "Level-$level keyword detected"
        runOnUiThread { Toast.makeText(this, toast, Toast.LENGTH_LONG).show() }
    }

    private fun endCall() {
        mic?.stop()

        val intent = Intent(this, com.silenthelp.ui.home.HomeActivity::class.java).apply {
            putExtra("threat_level", highestLevel)
            putExtra("alerted_contacts", contactsAlerted.toTypedArray())

            lastLocation?.let {
                putExtra("lat", it.latitude)
                putExtra("lon", it.longitude)
            }

            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        finish()
    }


    private fun seedKeywordsOnce() {
        if (settings.getKeywords(1).isEmpty()) {
            settings.addKeyword("help", 1)
            settings.addKeyword("hello", 1)
            settings.addKeyword("red", 2)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!coarseGranted && !fineGranted) return

        val lm: LocationManager = getSystemService() ?: return

        lastLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

}
