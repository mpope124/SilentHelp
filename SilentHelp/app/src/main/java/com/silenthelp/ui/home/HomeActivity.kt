// Entry point & dashboard: handles insets, permission rationale, navigation, and displays post-call alerts based on threat level.
// Created By Carmelo Vera
package com.silenthelp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.silenthelp.R
import com.silenthelp.core.ThreatPolicy
import com.silenthelp.core.manager.SettingsManager
import com.silenthelp.ui.fakecall.FakeCallActivity
import com.silenthelp.ui.incident.IncidentLogActivity
import com.silenthelp.ui.schedule.ScheduleCallActivity
import com.silenthelp.ui.settings.UserSettingsActivity

class HomeActivity : AppCompatActivity() {

    /** Manager for storing and retrieving data */
    private lateinit var settingsManager: SettingsManager

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for Home Screen */
        setContentView(R.layout.activity_home)

        /** Initialize SettingsManager */
        settingsManager = SettingsManager(this)

        // -------------------------------------------------------------------------
        // HANDLE DISPLAY CUTOUTS AND STATUS BAR INSETS
        // -------------------------------------------------------------------------
        val root = findViewById<View>(R.id.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            /** Apply top inset padding so content isn’t hidden under the system bar */
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            WindowInsetsCompat.CONSUMED
        }

        // -------------------------------------------------------------------------
        // SHOW RATIONAL OR PENDING ALERT
        // -------------------------------------------------------------------------
        /** Check Permission Mic and Location */
        if (!settingsManager.hasSeenRationale()) {
            showPermissionRationale()
        } else {
            maybeShowAlertedDialog()
        }

        //==========================================================================
        // HOME SCREEN NAVIGATION BUTTONS
        //==========================================================================

        findViewById<Button>(R.id.btnFakeCall)
            .setOnClickListener { startActivity(Intent(this, FakeCallActivity::class.java)) }

        findViewById<Button>(R.id.btnScheduleCall)
            .setOnClickListener { startActivity(Intent(this, ScheduleCallActivity::class.java)) }

        findViewById<Button>(R.id.btnIncidentLog)
            .setOnClickListener { startActivity(Intent(this, IncidentLogActivity::class.java)) }

        findViewById<Button>(R.id.btnOpenUserSettings)
            .setOnClickListener { startActivity(Intent(this, UserSettingsActivity::class.java)) }
        /** Ensure any post-call dialog is shown after navigation buttons are read maybeShowAlertedDialog() */
        maybeShowAlertedDialog()
    }

    //==========================================================================
    // HANDLE NEW INTENTS AND RESUME
    //==========================================================================
    /** Update the stored Intent so extras are available */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        maybeShowAlertedDialog()
    }

    /** Re-run alert check */
    override fun onResume() {
        super.onResume()
        maybeShowAlertedDialog()
    }

    //==========================================================================
    // PERMISSION RATIONAL DIALOG
    //==========================================================================
    /** Explains why Mic & Location are needed before requesting them later */
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Why Silent Help needs Mic & Location")
            .setMessage(
                "• Microphone: so the app can listen for your safety keywords during a fake call.\n\n"
                        + "• Location: so your trusted contacts receive coordinates if you use higher threat levels.\n\n"
                        + "You'll be asked for these permissions the first time you start a fake call.")
            .setPositiveButton("Got it") { _, _ ->
                settingsManager.markRationaleSeen()
                maybeShowAlertedDialog()
            }
            .show()
    }

    //==========================================================================
    // POST CALL ALERT DIALOG
    //==========================================================================
    private fun maybeShowAlertedDialog() {
        /** Header */
        val lvl   = intent.getIntExtra("threat_level", 0)
        if (lvl == 0) return

        /** Retrieve contact names and coordinates */
        val names = intent.getStringArrayExtra("alerted_contacts") ?: arrayOf()
        val lat   = intent.getDoubleExtra("lat", 0.0)
        val lon   = intent.getDoubleExtra("lon", 0.0)

        /** Format contact names or placeholder if empty */
        val nameText = if (names.isNotEmpty()) names.joinToString() else "—"

        /** Build the messages from the template, replacing placeholders */
        var msg = ThreatPolicy.LEVEL_TEMPLATE[lvl] ?: ""
        msg = msg.replace("##LOC##", "$lat, $lon")
            .replace("##NAME##", nameText)

        /** Show Dialog */
        AlertDialog.Builder(this)
            .setTitle("Silent Help – Level $lvl")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()

        /** Clear extras so it doesn't repeat */
        intent.removeExtra("threat_level")
        intent.removeExtra("alerted_contacts")
        intent.removeExtra("lat")
        intent.removeExtra("lon")
    }
}