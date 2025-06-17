// Scans incoming text for keywords
// Created by Kelley Rosa
package com.silenthelp.voice

class KeywordDetector(private val words: Set<String>,) {
    private val fired = mutableSetOf<String>()

    /** Serves the given phrase for keywords */
    fun detect(phrase: String): List<String> {
        val matches = words.filter { phrase.contains(it, ignoreCase = true) }
        val newHits = matches.filter { fired.add(it) }
        return newHits
    }
}
