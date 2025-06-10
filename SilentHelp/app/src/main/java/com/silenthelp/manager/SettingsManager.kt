// Created by Kelley Rosa 06-05-25
package com.silenthelp.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.silenthelp.model.Contact

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("silenthelp_prefs", Context.MODE_PRIVATE)

    fun saveKeyword(keyword: String, level: Int) {
        val key = "level_${level}_keywords"
        val existing = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        existing.add(keyword.lowercase())
        prefs.edit().putStringSet(key, existing).apply()
    }

    fun getKeywords(level: Int): Set<String> {
        val key = "level_${level}_keywords"
        return prefs.getStringSet(key, setOf()) ?: setOf()
    }

    fun saveKeywordList(level: Int, keywords: Set<String>) {
        val key = "level_${level}_keywords"
        prefs.edit().putStringSet(key, keywords.map { it.lowercase() }.toSet()).apply()
    }


    fun getAllKeywords(): Map<Int, Set<String>> {
        return (1..4).associateWith { getKeywords(it) }
    }

    fun saveContacts(contacts: List<Contact>) {
        val json = Gson().toJson(contacts)
        prefs.edit().putString("user_contacts", json).apply()
    }

    fun getContacts(): List<Contact> {
        val json = prefs.getString("user_contacts", "[]")
        val type = object : TypeToken<List<Contact>>() {}.type
        return Gson().fromJson(json, type)
    }

}
