package com.silenthelp.core

/** Text that appears in the post-call pop-up for each threat level.
 *  `##LOC##` will be replaced with latitude/longitude. */
object ThreatPolicy {

    val LEVEL_TEMPLATE = mapOf(
        1 to """Contact was sent: "I'm feeling awkward; can you call me." """,

        2 to """
            *Silent Help has logged your time and location.*  
            *Ambient audio now recording for 2 minutes.*  
            Contact was sent: "I'm feeling uneasy—can you check up on me in a couple of minutes?"
        """.trimIndent(),

        3 to """
            *Silent Help has logged your time and location.*  
            *Ambient audio now recording for 5 minutes.*  
            Contact was sent: "Something is wrong. Here's my info just in case: ##LOC##"
        """.trimIndent(),

        4 to """
            *Silent Help has logged your time and location.*  
            *Ambient audio now recording for 10 minutes.*  
            *Emergency services have been contacted.*  
            Contact was sent: "Emergency SOS! Here's my info: ##LOC##"
        """.trimIndent()
    )
}
