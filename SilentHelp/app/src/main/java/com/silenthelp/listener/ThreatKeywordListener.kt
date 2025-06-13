package com.silenthelp.listener

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import com.silenthelp.manager.SettingsManager
import com.silenthelp.manager.ThreatLevelManager
import com.silenthelp.model.Contact                  // ← NEW
import com.silenthelp.service.NotificationService
import com.silenthelp.manager.TranscriptLogger
import java.util.*

class ThreatKeywordListener(private val context: Context) {

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val settings   = SettingsManager(context)
    private val logger     = TranscriptLogger(context)

    // --- NEW: remember everyone we texted during this call -----------------
    private val contactsAlerted = mutableSetOf<Contact>()
    fun getContactsAlerted(): Set<Contact> = contactsAlerted
    // -----------------------------------------------------------------------

    init {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                Log.e("KeywordDebug", "Speech error $error — restarting")
                recognizer.startListening(buildIntent())
            }

            override fun onResults(results: Bundle?) {
                handleTranscript(results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION)?.joinToString(" "))
            }

            override fun onPartialResults(partialResults: Bundle?) {
                handleTranscript(partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION)?.joinToString(" "))
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    /* --------------------------------------------------------------------- */

    private fun handleTranscript(raw: String?) {
        val spoken = raw?.lowercase(Locale.getDefault()) ?: return
        logger.append(spoken)                               // log every chunk

        val level = ThreatLevelManager(settings).detectThreatLevel(spoken) ?: return
        settings.logDetection(spoken, level)

        settings.getContactForLevel(level)?.let { contact ->
            contactsAlerted += contact                      // ← track it
            NotificationService.sendAlert(context, contact, level, spoken)
            Toast.makeText(
                context,
                "Keyword detected – message sent to ${contact.name}",
                Toast.LENGTH_LONG
            ).show()
        }

        recognizer.stopListening()                          // restart recognizer
        recognizer.startListening(buildIntent())
    }

    /* --------------------------------------------------------------------- */

    fun startListening() = recognizer.startListening(buildIntent())
    fun stopListening()  = recognizer.destroy()

    private fun buildIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
    }
}
