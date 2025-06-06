// Created by Kelley Rosa 06-05-25
package com.silenthelp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.model.Keyword

class KeywordManager(context: Context) {

    // Initialize SharedPreferences to store keyword data
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("KeywordPrefs", Context.MODE_PRIVATE)


    private val gson = Gson()
    private val keywordListType = object : TypeToken<MutableList<Keyword>>() {}.type

    // Load keywords from SharedPreferences
    private var keywords: MutableList<Keyword> = loadKeywords()

    // Returns the current list of keywords
    fun getKeywords(): List<Keyword> = keywords

    // Adds a new keyword
    fun addKeyword(keyword: Keyword) {
        keywords.add(keyword)
        saveKeywords()
    }

    // Removes a keyword
    fun removeKeyword(index: Int) {
        if (index in keywords.indices) {
            keywords.removeAt(index)
            saveKeywords()
        }
    }

    // Edits and updates keyword
    fun updateKeyword(index: Int, newKeyword: Keyword) {
        if (index in keywords.indices) {
            keywords[index] = newKeyword
            saveKeywords()
        }
    }

    // Saves current keyword list to SharedPreferences
    private fun saveKeywords() {
        val json = gson.toJson(keywords)
        sharedPrefs.edit().putString("keyword_list", json).apply()
    }

    // Loads keyword list from SharedPreferences
    private fun loadKeywords(): MutableList<Keyword> {
        val json = sharedPrefs.getString("keyword_list", null)
        return if (json != null) gson.fromJson(json, keywordListType) else mutableListOf()
    }
}

