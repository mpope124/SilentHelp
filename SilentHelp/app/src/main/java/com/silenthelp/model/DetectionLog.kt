package com.silenthelp.model

data class DetectionLog(
    val phrase: String,
    val matchedLevel: Int,
    val timestamp: Long
)
