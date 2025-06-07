package com.silenthelp.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.ui.fakecall.FakeCallActivity
import com.silenthelp.ui.incident.IncidentLogActivity
import com.silenthelp.ui.keyword.KeywordSettingsActivity
import com.silenthelp.R
import com.silenthelp.ui.user.UserSettingsActivity

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
            val intent = Intent(this, UserSettingsActivity::class.java)
            startActivity(intent)
        }

        fakeCallButton.setOnClickListener {
            val intent = Intent(this, FakeCallActivity::class.java)
            startActivity(intent)
        }

        incidentLogButton.setOnClickListener {
            startActivity(Intent(this, IncidentLogActivity::class.java))
        }
    }
}