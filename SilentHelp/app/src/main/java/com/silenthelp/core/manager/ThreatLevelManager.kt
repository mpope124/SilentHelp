// Created by Kelley Rosa
package com.silenthelp.core.manager

class ThreatLevelManager(private val settingsManager: SettingsManager) {

    fun detectThreatLevel(phrase: String): Int? {
        val normalizedPhrase = phrase.lowercase()

        for (level in 1..4) {
            val keywords = settingsManager.getKeywords(level).map { it.lowercase() }
            if (keywords.any { keyword -> normalizedPhrase.contains(keyword) }) {
                return level
            }
        }

        return null
    }
}
