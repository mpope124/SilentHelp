package com.silenthelp.models
//added by Michael
data class Incident (
    val _id: String? = null,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val severity: String
)