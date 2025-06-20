// Manages the live fake call screen
// Requests permissions, starts the speech engine, detects keywords across all threat levels, and on hangup reports the highest level + contacts + location back
// Created By Kelley Rosa

package com.silenthelp.ui.fakecall

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager
import com.silenthelp.voice.KeywordDetector
import com.silenthelp.voice.MicWrapper
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.core.ThreatPolicy
import com.silenthelp.models.Incident
import com.silenthelp.ui.home.HomeActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class FakeCallActiveActivity : AppCompatActivity() {

    // =========================================================================
    // TIMER STATE
    // =========================================================================
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
    /** Detect all keywords on call */
    private var detectedKeywords = mutableListOf<String>()

    private lateinit var fusedClient: FusedLocationProviderClient

    /** Media for ambient recording */
    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null


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

        fusedClient = LocationServices.getFusedLocationProviderClient(this)

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
        if (needsPerm(Manifest.permission.ACCESS_FINE_LOCATION))
            permsNeeded += Manifest.permission.ACCESS_FINE_LOCATION

        if (permsNeeded.isEmpty()) {
            getLastLocation()
            initEngine()
        } else {
            permLauncher.launch(arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
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
            detectors[level]?.detect(text)
                ?.takeIf { it.isNotEmpty() }
                ?.let { newHits ->
                    /* record any newly detected keywords */
                    detectedKeywords.addAll(newHits)
                    handleHit(level)
                    return
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

        if (level >= 2 && recorder == null) {
            // choose duration by level
            val durationMs = when(level) {
                2 -> 60_000L  // 1 min
                3 -> 90_000L  // 1.5 min
                4 -> 120_000L // 2 min
                else -> 0L
            }
            startAmbientRecording(durationMs, level)
        }
    }

    // =========================================================================
    // FINISHING THE CALL
    // =========================================================================
    /** Stops the mic and timer, packages threat_level, contacts, and coords into an Intent and returns to HomeActivity */
    private fun endCall() {
        mic?.stop()
        handler.removeCallbacks(tick)

        /** Build the Incident object */
        val now = Date()
        val ts  = SimpleDateFormat("MM/dd/yy HH:mm:ss", Locale.US).format(now)
        val datePart = SimpleDateFormat("MM/dd", Locale.US).format(now)
        val title = "$datePart – Level $highestLevel"
        var desc = ThreatPolicy.LEVEL_TEMPLATE[highestLevel] ?: ""
        desc = desc
            .replace("##LOC##", "${lastLocation?.latitude}, ${lastLocation?.longitude}")
            .replace("##NAME##", contactsAlerted.joinToString())

        val incident = Incident(
            _id              = null,
            title            = title,
            keywordsDetected = detectedKeywords.toList(),
            timestamp        = ts,
            severity         = "Level $highestLevel",
            contact          = contactsAlerted.toList(),
            location         = "${lastLocation?.latitude}, ${lastLocation?.longitude}",
            audioPath        = recordFile?.absolutePath
        )


        /** Append data to incidents.json */
        val file = File(filesDir, "incidents.json")
        val gson = Gson()
        val type = object: TypeToken<MutableList<Incident>>(){}.type
        val list: MutableList<Incident> = if (file.exists()) {
            gson.fromJson(file.readText(), type)
        } else {
            mutableListOf()
        }
        list.add(incident)
        file.writeText(gson.toJson(list))

        /** Fire the Intent back to HomeActivity for Popup */
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("threat_level", highestLevel)
            putExtra("alerted_contacts", contactsAlerted.toTypedArray())
            lastLocation?.let {
                putExtra("lat", it.latitude)
                putExtra("lon", it.longitude)
            }
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        file.writeText(gson.toJson(list))
        Log.d("FakeCall", "Wrote incidents.json: ${file.readText()}")

        startActivity(intent)
        finish()
    }

    // =========================================================================
    // LOCATION SNAPSHOT
    // =========================================================================
    /**  Grabs the last known location from NETWORK or GPS provider, if coarse or fine location permission is granted. */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    lastLocation = loc
                    Log.d("LocationDebug", "fused loc=$loc")
                } else {
                    Log.d("LocationDebug", "fused lastLocation was null")
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationDebug", "fused error", e)
            }
    }

    // =========================================================================
    // AMBIENT RECORDING
    // =========================================================================
    private fun startAmbientRecording(durationMs: Long, level: Int) {
        // 1) create a human-readable timestamp
        val now = Date()
        val ts  = SimpleDateFormat("MMddyyyy_HHmmss", Locale.US).format(now)

        // 2) build filename with timestamp and level
        val filename = "ambient_${ts}_L$level.mp4"
        recordFile = File(filesDir, filename)

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordFile!!.absolutePath)
            prepare()
            start()
        }

        // schedule stop after the given duration
        Handler(Looper.getMainLooper()).postDelayed({
            stopAmbientRecording()
        }, durationMs)
    }

    private fun stopAmbientRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (_: IllegalStateException) {
                // no‐op
            } finally {
                release()
            }
        }
        recorder = null
    }


}
