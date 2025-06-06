package com.silenthelp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R.id.btnOpenKeywordSettings

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val keywordButton = findViewById<Button>(R.id.btnOpenKeywordSettings)
        val userSettingsButton = findViewById<Button>(R.id.btnOpenUserSettings)
        val fakeCallButton = findViewById<Button>(R.id.btnFakeCall)
        val incidentLogButton = findViewById<Button>(R.id.btnIncidentLog)

        keywordButton.setOnClickListener {
            val intent = Intent(this, KeywordSettingsActivity::class.java)
            startActivity(intent)
        }

        userSettingsButton.setOnClickListener {
            // Placeholder — update when Colin finishes UserSettingsActivity
            val intent = Intent(this, UserSettingsActivity::class.java)
            startActivity(intent)
        }

        fakeCallButton.setOnClickListener {
            // TODO: Implement Fake Call functionality
        }

        incidentLogButton.setOnClickListener {
            // TODO: Implement Incident Log functionality
        }
    }
}