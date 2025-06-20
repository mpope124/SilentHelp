// Holds the pop-up messages for each threat level
// Created By Kelley Rosa
package com.silenthelp.core

object ThreatPolicy {

    // =========================================================================
    // Map of threat level → template text
    // =========================================================================
    val LEVEL_TEMPLATE = mapOf(
        /* Level 1: low-urgency comfort request */
        1 to """ 
            |Contact(s) [##NAME##] received the message: 
            |“Hey, I feel a bit uncomfortable. Could you call me when you get a second?"
            |""".trimMargin(),

        /* Level 2: moderate alert with 2-minute ambient recording */
        2 to """
            * Silent Help has saved your time and location * 
            * Ambient audio recording started (1 min) *
            
            Contact(s) [##NAME##] received the message: 
            “I’m uneasy right now—can you call me in a couple of minutes and check on me?”
            """.trimIndent(),

        /* Level 3: high alert with 5-minute recording + coordinates */
        3 to """
            * Silent Help has saved your time and location * 
            * Ambient audio recording started (1.5 mins) *
            
            Contact(s) [##NAME##] received the message: 
            “Something feels wrong. Please keep an eye on me and call ASAP.
            Here are my coordinates: ##LOC##"
            
            """.trimIndent(),

        /* Level 4: emergency – 10-minute recording, services notified, coordinates */
        4 to """
            * Silent Help has saved your time and location *
            * Ambient audio recording started (2 mins)
            * Emergency services in your area have been notified * 
            
            Contact(s) [##NAME##] received the message: 
            “EMERGENCY SOS.
            Call me ASAP! Stay on the phone with me until help arrives.
            Here are my coordinates: ##LOC##”
        """.trimIndent()
    )
}
