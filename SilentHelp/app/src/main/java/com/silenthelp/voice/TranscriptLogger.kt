package com.silenthelp.voice

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TranscriptLogger(context: Context) {

    private val logFile: File = File(context.filesDir, "transcript_log.txt")
    private val timeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    fun append(segment: String) {
        logFile.appendText("${timeFmt.format(Date())}: $segment\n")
    }

    fun readAll(): String? = if (logFile.exists()) logFile.readText() else null
}
