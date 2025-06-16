// Manages user-defined keywords with in-memory caching and JSON persistence in SharedPreferences.
// Created by Kelley Rosa
package com.silenthelp.core.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.core.model.Keyword
import androidx.core.content.edit

class KeywordManager(context: Context) {
    // =========================================================================
    // SHARED PREFERENCES & JSON SETUP
    // =========================================================================
    /** SharedPreferences file for keyword storage */
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("KeywordPrefs", Context.MODE_PRIVATE)

    /** Gson instance for JSON deserialization */
    private val gson = Gson()

    /** Type information for a MutableList<Keyword> */
    private val keywordListType = object : TypeToken<MutableList<Keyword>>() {}.type

    // =========================================================================
    // IN-MEMORY CACHE
    // =========================================================================
    /** Holds the current keywords */
    private var keywords: MutableList<Keyword> = loadKeywords()

    // =========================================================================
    // PUBLIC API
    // =========================================================================
    /** Returns the current list of keywords */
    fun getKeywords(): List<Keyword> = keywords

    /** Adds a new keyword and persist */
    fun addKeyword(keyword: Keyword) {
        keywords.add(keyword)
        saveKeywords()
    }

    /** Removes a keyword by index */
    fun removeKeyword(index: Int) {
        if (index in keywords.indices) {
            keywords.removeAt(index)
            saveKeywords()
        }
    }

    /** Edits and updates keyword at the given index */
    fun updateKeyword(index: Int, newKeyword: Keyword) {
        if (index in keywords.indices) {
            keywords[index] = newKeyword
            saveKeywords()
        }
    }

    // =========================================================================
    // PERSISTENCE HELPERS
    // =========================================================================
    /** Saves current keyword list to SharedPreferences */
    private fun saveKeywords() {
        val json = gson.toJson(keywords)
        sharedPrefs.edit { putString("keyword_list", json) }
    }

    /** Loads keyword list from SharedPreferences */
    private fun loadKeywords(): MutableList<Keyword> {
        val json = sharedPrefs.getString("keyword_list", null)
        return if (json != null) gson.fromJson(json, keywordListType) else mutableListOf()
    }
}