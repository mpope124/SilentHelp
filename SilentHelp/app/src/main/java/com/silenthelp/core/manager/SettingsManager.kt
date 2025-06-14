// Created by Kelley Rosa
package com.silenthelp.core.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.core.model.Contact
import com.silenthelp.core.model.DetectionLog

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("silenthelp_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    //==========================================================================
    // CONTACTS
    //==========================================================================
    private val CONTACTS_KEY = "contacts_json"

    fun getContacts(): List<Contact> {
        val json = prefs.getString(CONTACTS_KEY, "[]")
        val type = object : TypeToken<List<Contact>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveContacts(contacts: List<Contact>) {
        prefs.edit { putString(CONTACTS_KEY, gson.toJson(contacts)) }
    }

    fun getContactForLevel(level: Int): Contact? =
        getContacts().firstOrNull { it.level == level }

    //==========================================================================
    // KEYWORDS
    //==========================================================================
    private fun keyFor(level: Int) = "level_${level}_keywords"

    fun getKeywords(level: Int): Set<String> =
        prefs.getStringSet(keyFor(level), emptySet()) ?: emptySet()

    fun saveKeywordList(level: Int, keywords: Set<String>) {
        prefs.edit {
            putStringSet(keyFor(level), keywords.map { it.lowercase() }.toSet())
        }
    }

    fun addKeyword(word: String, level: Int) {
        val updated = getKeywords(level).toMutableSet().apply {
            add(word.lowercase())
        }
        saveKeywordList(level, updated)
    }

    //==========================================================================
    // DETECTION LOG
    //==========================================================================
    private val DETECTION_LOG_KEY = "detection_log"

    fun logDetection(phrase: String, level: Int) {
        val logs = getDetectionLogs().toMutableList()
        logs += DetectionLog(
            phrase = phrase,
            matchedLevel = level,
            timestamp = System.currentTimeMillis()
        )
        prefs.edit { putString(DETECTION_LOG_KEY, gson.toJson(logs)) }
    }

    fun getDetectionLogs(): List<DetectionLog> {
        val json = prefs.getString(DETECTION_LOG_KEY, "[]")
        val type = object : TypeToken<List<DetectionLog>>() {}.type
        return gson.fromJson(json, type)
    }
}

