package com.silenthelp.voice             // ← must match the folder path

class KeywordDetector(
    private val words: Set<String>,      // keywords, lower-case
) {
    private val fired = mutableSetOf<String>()

    fun detect(phrase: String): List<String> {
        val matches = words.filter { phrase.contains(it, ignoreCase = true) }
        val newHits = matches.filter { fired.add(it) }
        return newHits
    }
}
