// Created by Kelley Rosa 06-05-25
package com.silenthelp

import android.content.Context
import android.content.SharedPreferences

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

    fun getAllKeywords(): Map<Int, Set<String>> {
        return (1..4).associateWith { getKeywords(it) }
    }
}
