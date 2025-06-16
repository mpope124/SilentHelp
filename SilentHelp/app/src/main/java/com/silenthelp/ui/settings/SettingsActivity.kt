package com.silenthelp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.ui.keyword.KeywordSettingsActivity
import com.silenthelp.ui.contact.ContactSettingsActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usersettingslayout)

        findViewById<Button>(R.id.btnManageKeywords)
            .setOnClickListener {
                startActivity(Intent(this, KeywordSettingsActivity::class.java))
            }

        findViewById<Button>(R.id.btnManageContacts)
            .setOnClickListener {
                startActivity(Intent(this, ContactSettingsActivity::class.java))
            }
    }
}
