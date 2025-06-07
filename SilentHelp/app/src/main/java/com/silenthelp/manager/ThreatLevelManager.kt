// Created by Kelley Rosa 06-05-25
package com.silenthelp.manager

class ThreatLevelManager(private val settingsManager: SettingsManager) {

    fun detectThreatLevel(phrase: String): Int? {
        val normalized = phrase.lowercase()
        for (level in 1..4) {
            if (settingsManager.getKeywords(level).contains(normalized)) {
                return level
            }
        }
        return null
    }
}
