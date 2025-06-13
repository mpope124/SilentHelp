package com.silenthelp.manager

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TranscriptLogger(context: Context) {

    private val logFile: File = File(context.filesDir, "transcript_log.txt")
    private val timeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /** Appends one spoken segment to the file */
    fun append(segment: String) {
        logFile.appendText("${timeFmt.format(Date())}: $segment\n")
    }

    /** Reads the whole file (null if it doesn’t exist yet) */
    fun readAll(): String? = if (logFile.exists()) logFile.readText() else null
}
