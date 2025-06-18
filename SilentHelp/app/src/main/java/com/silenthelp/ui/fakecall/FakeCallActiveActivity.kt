// Manages the live fake call screen
// Requests permissions, starts the speech engine, detects keywords across all threat levels, and on hangup reports the highest level + contacts + location back
// Created By Kelley Rosa

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
import android.os.Handler
import android.os.Looper


class FakeCallActiveActivity : AppCompatActivity() {

    // ─── TIMER STATE ───────────────────────────────────────────────
    private lateinit var durationText: TextView      // NEW
    private val handler = Handler(Looper.getMainLooper()) // NEW
    private var seconds = 0                          // NEW
    private val tick: Runnable = object : Runnable { // NEW
        override fun run() {
            seconds++
            val m = seconds / 60
            val s = seconds % 60
            durationText.text = String.format("%02d:%02d", m, s)
            handler.postDelayed(this, 1_000)
        }
    }
    // ───────────────────────────────────────────────────────────────



    // =========================================================================
    // VIEW REFERENCES
    // =========================================================================
    /** Wrapper around Android’s SpeechRecognizer */
    private var mic: MicWrapper? = null
    /** Displays partial/final speech transcripts */
    private lateinit var tvTranscript: TextView
    /** Last known location snapshot */
    private var lastLocation: Location? = null
    /** One detector per threat level (1–4), built from user-defined keywords */
    private lateinit var detectors: Map<Int, KeywordDetector>
    /** Highest threat level detected so far (0 = none) */
    private var highestLevel = 0
    /** Names of contacts alerted during this call */
    private val contactsAlerted = mutableSetOf<String>()

    /** Manager for storing and retrieving data */
    private lateinit var settings: SettingsManager

    //==========================================================================
    // Permission Launcher
    //==========================================================================
    /** Requests RECORD_AUDIO and COARSE/FINE_LOCATION */
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() ) { result ->
        if (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_FINE_LOCATION]   == true
            ) getLastLocation()
        if (result[Manifest.permission.RECORD_AUDIO] == true) {
            initEngine()
        } else {
            tvTranscript.text = "Microphone permission denied."
        }
    }

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for ACTIVE fake call */
        setContentView(R.layout.activity_fake_call_active)

        /** Hides Global ActionBar */
        supportActionBar?.hide()

        // =========================================================================
        // BIND VIEWS AND HELPERS
        // =========================================================================
        tvTranscript = findViewById(R.id.tvTranscript)
        durationText  = findViewById(R.id.textCallDuration)
        settings = SettingsManager(this)

        /** Build a KeywordDetector for each threat level */
        detectors = (1..4).associateWith { lvl ->
            KeywordDetector(settings.getKeywords(lvl))
        }

        /** Hang up button returns to HomeActivity */
        findViewById<Button>(R.id.btnHangUp).setOnClickListener { endCall() }

        /** start the call-duration timer */
        handler.post(tick)

        /** Kick off permission checks */
        requestPermissionsIfNeeded()
    }

    // =========================================================================
    // PERMISSION HANDLING
    // =========================================================================
    /** Checks for needed permissions and either requests or proceeds. */
    private fun requestPermissionsIfNeeded() {
        val permsNeeded = mutableListOf<String>()

        if (needsPerm(Manifest.permission.RECORD_AUDIO))
            permsNeeded += Manifest.permission.RECORD_AUDIO
        if (needsPerm(Manifest.permission.ACCESS_COARSE_LOCATION))
            permsNeeded += Manifest.permission.ACCESS_COARSE_LOCATION

        if (permsNeeded.isEmpty()) {
            getLastLocation()
            initEngine()
        } else {
            permLauncher.launch(permsNeeded.toTypedArray())
        }
    }

    /** Returns true if the given permission isnt granted yet */
    private fun needsPerm(name: String) =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, name) != PackageManager.PERMISSION_GRANTED

    // =========================================================================
    // SPEECH ENGINE SETUP
    // =========================================================================
    /** Initializes the MicWrapper to start listening immediately. */
    private fun initEngine() {
        tvTranscript.text = "Ready…"

        mic = MicWrapper(
            context   = this,
            onReady   = { runOnUiThread { tvTranscript.text = "Listening…" } },
            onPartial = { txt -> handleTranscript(txt, false) },
            onFinal   = { txt -> handleTranscript(txt, true) },
            onErr     = { code -> runOnUiThread {
                tvTranscript.text = "Error $code – speak again"
            }
                mic?.start()
            }
        ).also { it.start() }
    }

    /** When activity is destroyed, end speech engine */
    override fun onDestroy() {
        handler.removeCallbacks(tick)
        mic?.stop()
        super.onDestroy()
    }

    // =========================================================================
    // TRANSCRIPT HANDLING & KEYWORD DETECTION
    // =========================================================================
    /** Updates the UI with the transcript and checks each level’s detector on match */
    private fun handleTranscript(text: String, isFinal: Boolean) {
        runOnUiThread {
            tvTranscript.text = (if (isFinal) "Final:   " else "Partial: ") + text
        }

        for (level in 4 downTo 1) {
            if (detectors[level]?.detect(text)?.isNotEmpty() == true) {
                handleHit(level)
                break
            }
        }
    }

    /** Records a keyword hit: updates highestLevel, adds contact, and toasts */
    private fun handleHit(level: Int) {
        highestLevel = maxOf(highestLevel, level)

        settings.getContactForLevel(level)?.let { contact ->
            contactsAlerted += contact.name
        }

        val toast = if (level == 1)
            "Level-1 keyword detected"
        else "Level-$level keyword detected"
        runOnUiThread { Toast.makeText(this, toast, Toast.LENGTH_LONG).show()
        }
    }

    // =========================================================================
    // FINISHING THE CALL
    // =========================================================================
    /** Stops the mic and timer, packages threat_level, contacts, and coords into an Intent and returns to HomeActivity */
    private fun endCall() {
        mic?.stop()
        handler.removeCallbacks(tick)

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

    // =========================================================================
    // LOCATION SNAPSHOT
    // =========================================================================
    /**  Grabs the last known location from NETWORK or GPS provider, if coarse or fine location permission is granted. */
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
