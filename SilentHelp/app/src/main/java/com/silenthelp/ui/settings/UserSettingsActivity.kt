// Provides a unified hub to manage both keywords and contacts.
// Created by Kelley Rosa
package com.silenthelp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.ui.keyword.KeywordSettingsActivity
import com.silenthelp.ui.contact.ContactSettingsActivity

class UserSettingsActivity : AppCompatActivity() {

    /** Back arrow in header */
    private lateinit var backButton: ImageView

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for user settings */
        setContentView(R.layout.activity_user_settings)

        /** Hides Global ActionBar */
        supportActionBar?.hide()

        backButton = findViewById(R.id.btn_back)

        /** Back arrow closes this activity */
        backButton.setOnClickListener {
            finish()
        }

        // =========================================================================
        // NAVIGATION
        // =========================================================================
        /** Button to launch the keyword management screen */
        findViewById<Button>(R.id.btnManageKeywords)
            .setOnClickListener {
                startActivity(Intent(this, KeywordSettingsActivity::class.java))
            }

        /** Button to launch the trusted-contacts management screen */
        findViewById<Button>(R.id.btnManageContacts)
            .setOnClickListener {
                startActivity(Intent(this, ContactSettingsActivity::class.java))
            }
    }
}
