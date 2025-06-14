package com.silenthelp.debug

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MicTestActivity : AppCompatActivity() {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var status: TextView

    /* Runtime-permission launcher */
    private val micPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening()
            else Toast.makeText(this, "Mic permission denied", Toast.LENGTH_LONG).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        /* ------------ simple vertical layout ------------ */
        status = TextView(this).apply { text = "Press the button then speak…" }
        val btn = Button(this).apply { text = "Start Listening" }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 64)
            addView(btn)
            addView(status)
        }
        setContentView(root)

        /* ------------ recognizer setup ------------ */
        recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) { status.text = "Ready…" }
                override fun onBeginningOfSpeech() { status.text = "Listening…" }
                override fun onRmsChanged(rms: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() { status.text = "Processing…" }

                override fun onPartialResults(p: Bundle?) {
                    val words = p?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION)?.joinToString(" ")
                    Log.d("MicTest", "PARTIAL = $words")
                    status.text = "Partial: $words"
                }
                override fun onResults(r: Bundle?) {
                    val words = r?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION)?.joinToString(" ")
                    Log.d("MicTest", "FINAL   = $words")
                    status.text = "Final: $words"
                }
                override fun onError(error: Int) {
                    Log.e("MicTest", "ERROR $error")
                    status.text = "Error $error (see Logcat)"
                }
                override fun onEvent(t: Int, b: Bundle?) {}
            })
        }

        btn.setOnClickListener {
            if (needsMicPerm()) micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
            else startListening()
        }
    }

    private fun needsMicPerm() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault().toLanguageTag())
        }
        recognizer.startListening(intent)
    }

    override fun onDestroy() {
        recognizer.destroy()
        super.onDestroy()
    }
}
