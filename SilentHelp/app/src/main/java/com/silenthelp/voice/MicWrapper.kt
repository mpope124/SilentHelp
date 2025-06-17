// Wrapper around SpeechRecognizer to provide callbacks
// Created by Kelley Rosa
package com.silenthelp.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class MicWrapper(
    private val context: Context,
    private val onReady:  () -> Unit = {},
    private val onPartial: (String) -> Unit,
    private val onFinal:   (String) -> Unit,
    private val onErr:     (Int) -> Unit = {}
) {

    // =========================================================================
    // SPEECH RECOGNIZER INITIALIZATION
    // =========================================================================
    /** Create and configure SpeechRecognizer instance */
    private val recog = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {

            /** Overrides required by RecognitionListener interface */
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rms: Float) {}
            override fun onBufferReceived(buf: ByteArray?) {}
            override fun onEvent(t: Int, b: Bundle?) {}

            /** Called on various error codes and forward via onErr */
            override fun onError(err: Int) {
                Log.e("MicWrapper", "error $err")
                onErr(err)
            }

            /** Called when the recognizer is ready for speech input */
            override fun onReadyForSpeech(p: Bundle?) = onReady()

            /** Called after user stops speaking */
            override fun onEndOfSpeech() {}

            /** Partial results arrive as a bundle of strings */
            override fun onPartialResults(p: Bundle?) {
                p?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    ?.let(onPartial)
            }

            /** Final results arrive as a bundle of strings */
            override fun onResults(r: Bundle?) {
                r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    ?.let(onFinal)
            }
        })
    }

    // =========================================================================
    // START / STOP METHODS
    // =========================================================================
    /** Begin listening with our configured intent */
    fun start() = recog.startListening(buildIntent())
    /** Stop and release the recognizer */
    fun stop()  = recog.destroy()

    // =========================================================================
    // INTENT CONFIGURATION
    // =========================================================================
    private fun buildIntent() =
        /** Freeform language model */
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            /** Partial results enabled */
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            /** Use the device’s default locale for transcription */
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())

            /** How long the recognizer will stay active after first audio (ms) */
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                6_000
            )
            /** How long of silence before finalizing (ms) */
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                3_000
            )
            /** Shorter silence threshold for “maybe done” events (ms) */
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                2_000
            )
        }
}
