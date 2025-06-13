package com.silenthelp.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.manager.SettingsManager
import com.silenthelp.ui.fakecall.FakeCallActivity
import com.silenthelp.ui.incident.IncidentLogActivity
import com.silenthelp.ui.keyword.KeywordSettingsActivity
import com.silenthelp.ui.user.UserSettingsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        settingsManager = SettingsManager(this)

        findViewById<Button>(R.id.btnOpenKeywordSettings)
            .setOnClickListener { startActivity(Intent(this, KeywordSettingsActivity::class.java)) }

        findViewById<Button>(R.id.btnOpenUserSettings)
            .setOnClickListener { startActivity(Intent(this, UserSettingsActivity::class.java)) }

        findViewById<Button>(R.id.btnFakeCall)
            .setOnClickListener { startActivity(Intent(this, FakeCallActivity::class.java)) }

        findViewById<Button>(R.id.btnIncidentLog)
            .setOnClickListener { startActivity(Intent(this, IncidentLogActivity::class.java)) }

        checkAlertPopup()               // handle return from call
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)          // make the new extras visible
        checkAlertPopup()
    }


    override fun onResume() {
        super.onResume()
        checkAlertPopup()               // safety net for rotation, etc.
    }

    // ------------------------------------------------------------------------

    private fun checkAlertPopup() {
        val names = intent.getStringArrayExtra("alerted_contacts") ?: return
        if (names.isEmpty()) return

        AlertDialog.Builder(this)
            .setMessage(
                if (names.size == 1)
                    "A text was sent to ${names[0]}"
                else
                    "Texts were sent to: ${names.joinToString()}"
            )
            .setPositiveButton("OK", null)
            .show()

        intent.removeExtra("alerted_contacts")   // prevent repeat dialogs
    }
}
