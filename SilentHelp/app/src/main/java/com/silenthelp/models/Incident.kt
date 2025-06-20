package com.silenthelp.models
//added by Michael
data class Incident (
    val _id: String? = null,
    val title: String,                      // (mm/dd) Level #
    val keywordsDetected: List<String>,     // all keywords detected on call
    val timestamp: String,                  // MM/DD/YY hh:mm:ss
    val severity: String,                   // Threat Level
    val contact: List<String>,             // names you notified
    val location: String,                  // “lat, lon”
    val audioPath: String? = null           // Ambient Audio Recording Path
)