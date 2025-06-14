package com.silenthelp.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceEngine(
    private val context: Context,
    private val onPartial: (String) -> Unit,
    private val onFinal: (String) -> Unit,
) {

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val main = Handler(Looper.getMainLooper())
    private var restarting = false

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rms: Float) {}
            override fun onBufferReceived(buf: ByteArray?) {}
            override fun onEndOfSpeech() { restart() }

            override fun onPartialResults(b: Bundle?) {
                b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    ?.let(onPartial)
            }

            override fun onResults(b: Bundle?) {
                b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    ?.let(onFinal)
                restart()
            }

            override fun onError(err: Int) { restart() }
            override fun onEvent(t: Int, b: Bundle?) {}
        })
    }

    fun startListening() = recognizer.startListening(buildIntent())
    fun stop()            = recognizer.destroy()

    private fun restart() {
        if (restarting) return
        restarting = true
        main.postDelayed({
            recognizer.stopListening()
            recognizer.startListening(buildIntent())
            restarting = false
        }, 1_000)
    }

    private fun buildIntent() =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        }
}
