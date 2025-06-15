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
import com.silenthelp.ui.keyword.KeywordSettingsActivity
import com.silenthelp.ui.user.UserSettingsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // handle the cut-out / status-bar inset
        val root = findViewById<View>(R.id.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            WindowInsetsCompat.CONSUMED
        }

        settingsManager = SettingsManager(this)

        //==========================================================================
        // Home Screen Navigation Buttons
        //==========================================================================
        findViewById<Button>(R.id.btnOpenKeywordSettings)
            .setOnClickListener { startActivity(Intent(this, KeywordSettingsActivity::class.java)) }

        findViewById<Button>(R.id.btnOpenUserSettings)
            .setOnClickListener { startActivity(Intent(this, UserSettingsActivity::class.java)) }

        findViewById<Button>(R.id.btnFakeCall)
            .setOnClickListener { startActivity(Intent(this, FakeCallActivity::class.java)) }

        findViewById<Button>(R.id.btnIncidentLog)
            .setOnClickListener { startActivity(Intent(this, IncidentLogActivity::class.java)) }

        maybeShowAlertedDialog()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        maybeShowAlertedDialog()
    }

    override fun onResume() {
        super.onResume()
        maybeShowAlertedDialog()
    }

    /* ───────────────────── post-call pop-up helper ───────────────────── */

    private fun maybeShowAlertedDialog() {
        val lvl   = intent.getIntExtra("threat_level", 0)
        if (lvl == 0) return

        val names = intent.getStringArrayExtra("alerted_contacts") ?: arrayOf()
        val lat   = intent.getDoubleExtra("lat", 0.0)
        val lon   = intent.getDoubleExtra("lon", 0.0)

        val nameText = if (names.isNotEmpty())
            names.joinToString()                  // “Mom”  or  “Mom, Alice”
        else
            "—"                                   // fallback if somehow empty

        var msg = ThreatPolicy.LEVEL_TEMPLATE[lvl] ?: ""
        msg = msg.replace("##LOC##", "$lat, $lon")
            .replace("##NAME##", nameText)

        AlertDialog.Builder(this)
            .setTitle("Silent Help – Level $lvl")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()

        // clear so it won’t repeat
        intent.removeExtra("threat_level")
        intent.removeExtra("alerted_contacts")
        intent.removeExtra("lat")
        intent.removeExtra("lon")
    }
}
