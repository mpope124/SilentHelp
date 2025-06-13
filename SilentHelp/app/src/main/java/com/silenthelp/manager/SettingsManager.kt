// Created by Kelley Rosa 06-05-25
package com.silenthelp.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.model.Contact
import com.silenthelp.model.DetectionLog
import androidx.core.content.edit

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("silenthelp_prefs", Context.MODE_PRIVATE)

    // === Contact persistence helpers  =========================================
    private val CONTACTS_KEY = "contacts_json"
    private val gson = Gson()                      // put this once at top of class

    /** Returns all saved contacts (empty list if none). */
    fun getContacts(): List<Contact> {
        val json = prefs.getString(CONTACTS_KEY, "[]")
        val type = object : TypeToken<List<Contact>>() {}.type
        return gson.fromJson(json, type)
    }

    /** Replaces the entire contact list.  Call this from your UI when a user
     *  adds/edits a contact. */
    fun saveContacts(contacts: List<Contact>) {
        prefs.edit { putString(CONTACTS_KEY, gson.toJson(contacts)) }
    }


    fun getContactForLevel(level: Int): Contact? =
        getContacts().firstOrNull { it.level == level }


    fun logDetection(phrase: String, level: Int) {
        val logs = getDetectionLogs().toMutableList()
        logs.add(
            DetectionLog(
                phrase = phrase,
                matchedLevel = level,
                timestamp = System.currentTimeMillis()
            )
        )
        prefs.edit { putString("detection_log", gson.toJson(logs)) }
    }

    fun getDetectionLogs(): List<DetectionLog> {
        val json = prefs.getString("detection_log", "[]")
        val type = object : TypeToken<List<DetectionLog>>() {}.type
        return gson.fromJson(json, type)
    }

    /* ---------- NEW: full speech transcript ---------- */
    fun logTranscript(segment: String) {
        val all = getTranscriptLog().toMutableList()
        all.add("${System.currentTimeMillis()}: $segment")
        prefs.edit { putString("transcript_log", gson.toJson(all)) }
    }

    fun getTranscriptLog(): List<String> {
        val json = prefs.getString("transcript_log", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    // --------------------------------------------------------------------------
// Keyword storage – one String-set per threat-level
// --------------------------------------------------------------------------
    private fun keyFor(level: Int) = "level_${level}_keywords"

    /** Return the keyword set for the requested threat-level (empty if none). */
    fun getKeywords(level: Int): Set<String> =
        prefs.getStringSet(keyFor(level), emptySet()) ?: emptySet()

    /** Replace the entire keyword set for a threat-level. */
    fun saveKeywordList(level: Int, keywords: Set<String>) {
        prefs.edit().putStringSet(keyFor(level),
            keywords.map { it.lowercase() }.toSet()
        ).apply()
    }

    /** Convenience: add a single keyword to the chosen threat-level. */
    fun addKeyword(word: String, level: Int) {
        val updated = getKeywords(level).toMutableSet()
        updated += word.lowercase()
        saveKeywordList(level, updated)
    }


}
