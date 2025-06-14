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

    private val recog = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(p: Bundle?) = onReady()
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rms: Float) {}
            override fun onBufferReceived(buf: ByteArray?) {}
            override fun onEndOfSpeech() {}                 // MicTest didn’t need anything here

            override fun onPartialResults(p: Bundle?) {
                p?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    ?.let(onPartial)
            }

            override fun onResults(r: Bundle?) {
                r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    ?.let(onFinal)
            }

            override fun onError(err: Int) {
                Log.e("MicWrapper", "error $err")
                onErr(err)
            }

            override fun onEvent(t: Int, b: Bundle?) {}
        })
    }

    fun start() = recog.startListening(buildIntent())
    fun stop()  = recog.destroy()

    private fun buildIntent() =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())

            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                6_000      // recogniser will run for at least 4 s from first audio
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                3_000      // after you stop talking, wait 3 s of silence before final
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                2_000      // heuristic: shorter “maybe done” silence
            )
        }

}
