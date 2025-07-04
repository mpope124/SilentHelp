// Central manager for app-wide preferences: runtime-rationale flag, trusted contacts, keyword lists per threat level, and detection logs.
// Created by Kelley Rosa
package com.silenthelp.core.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.core.model.Contact

class SettingsManager(private val context: Context) {

    // =========================================================================
    // SETUP & RATIONALE FLAG
    // =========================================================================
    /** SharedPreferences file for all SilentHelp settings */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("silenthelp_prefs", Context.MODE_PRIVATE)

    /** Gson instance for JSON deserialization */
    private val gson = Gson()

    /** Key to track whether the user has seen the permission rationale */
    private val SEEN_RATIONALE = "seen_rationale"

    /** Check if the one-time permission rationale was sh\own */
    fun hasSeenRationale(): Boolean =
        prefs.getBoolean(SEEN_RATIONALE, false)

    /** Mark that the permission rationale dialog has been shown */
    fun markRationaleSeen() {
        prefs.edit { putBoolean(SEEN_RATIONALE, true) }
    }

    //==========================================================================
    // CONTACTS MANAGEMENT
    //==========================================================================
    /** JSON key under which the Contact list is stored */
    private val CONTACTS_KEY = "contacts_json"

    /** Return all saved contacts */
    fun getContacts(): List<Contact> {
        val json = prefs.getString(CONTACTS_KEY, "[]")
        val type = object : TypeToken<List<Contact>>() {}.type
        return gson.fromJson(json, type)
    }

    /** Overwrite the entire contact list in preferences */
    fun saveContactList(contacts: List<Contact>) {
        prefs.edit { putString(CONTACTS_KEY, gson.toJson(contacts)) }
    }

    /** Return the first contact that matches a given threat level (if any) */
    fun getContactForLevel(level: Int): Contact? =
        getContacts().firstOrNull { it.level == level }

    /** Return all contacts matching a given threat level */
    fun getContactsForLevel(level: Int): List<Contact> =
        getContacts().filter { it.level == level }

    //==========================================================================
    // KEYWORD LISTS PER THREAT LEVEL
    //==========================================================================
    /** Build the preference key for a keyword set at a given threat level */
    private fun keyFor(level: Int) = "level_${level}_keywords"

    /** Return the set of keywords for a specific threat level */
    fun getKeywords(level: Int): Set<String> =
        prefs.getStringSet(keyFor(level), emptySet()) ?: emptySet()

    /** Replace the entire keyword set for a threat level */
    fun saveKeywordList(level: Int, keywords: Set<String>) {
        prefs.edit {
            putStringSet(keyFor(level), keywords.map { it.lowercase() }.toSet())
        }
    }

    // =========================================================================
    // SINGLETON ACCESS
    // =========================================================================
    /** Use this method to access a shared instance of SettingsManager */
    companion object {
        fun getInstance(context: Context): SettingsManager {
            return SettingsManager(context.applicationContext)
        }
    }

}

