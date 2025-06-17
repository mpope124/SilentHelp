// Displays the log of past keyword detections
// Created By Michael Pope
package com.silenthelp.ui.incident

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R

class IncidentLogActivity : AppCompatActivity() {
    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for Incident Log */
        setContentView(R.layout.activity_incident_log)

//        findViewById<Button>(R.id.button_back_to_home).setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//            finish()          // optional: removes this activity from the stack
//        }

    }
}