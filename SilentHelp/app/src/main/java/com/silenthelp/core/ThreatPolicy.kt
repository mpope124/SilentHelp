package com.silenthelp.core

/** Text that appears in the post-call pop-up for each threat level.
 *  `##LOC##` will be replaced with latitude/longitude.
 *   `##NAME##`  → contact name(s)  */
object ThreatPolicy {

    val LEVEL_TEMPLATE = mapOf(
        1 to """ 
            |Contact ##NAME## received the message: 
            |“Hey, I feel a bit uncomfortable. Could you call me when you get a second?"
            |""".trimMargin(),

        2 to """
            * Silent Help has saved your time and location * 
            * Ambient audio recording started (2 min) *
            
            Contact ##NAME## received the message: 
            “I’m uneasy right now—can you call me in a couple of minutes and check on me?”
            """.trimIndent(),

        3 to """
            * Silent Help has saved your time and location * 
            * Ambient audio recording started (5 min) *
            
            Contact ##NAME## received the message: 
            “Something feels wrong. Please keep an eye on me and call ASAP.
            Here are my coordinates: ##LOC##"
            
            """.trimIndent(),

        4 to """
            * Silent Help has saved your time and location *
            * Ambient audio recording started (10 min)
            * Emergency services in your area have been notified * 
            
            Contact ##NAME## received the message: 
            “EMERGENCY SOS.
            Call me ASAP! Stay on the phone with me until help arrives.
            Here are my coordinates: ##LOC##”
        """.trimIndent()
    )
}
