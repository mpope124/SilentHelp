package com.silenthelp.core.model

data class DetectionLog(
    val phrase: String,
    val matchedLevel: Int,
    val timestamp: Long
)
