// Edit existing keywords for a specific threat level
// Created by Kelley Rosa
package com.silenthelp.ui.keyword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager

class EditKeywordsActivity : AppCompatActivity() {
    // =========================================================================
    // MEMBER DECLARATIONS
    // =========================================================================
    /** Manages keyword persistence in SharedPreferences */
    private lateinit var settingsManager: SettingsManager
    /** Container layout for keyword rows */
    private lateinit var containerKeywords: LinearLayout
    /** Threat level (1–4) passed via Intent extras */
    private var threatLevel: Int = 0
    /** Tracks pairs of (EditText, delete button view) for each row */
    private val keywordViews = mutableListOf<Pair<EditText, View>>()

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Set Layout for editing keywords */
        setContentView(R.layout.activity_edit_keywords)
        supportActionBar?.hide()

        /** Initialize SettingsManager */
        settingsManager = SettingsManager(this)

        /**  Bind the container that will hold our dynamic rows */
        containerKeywords = findViewById(R.id.container_keywords)

        /** Retrieve the threat level this screen is editing */
        threatLevel = intent.getIntExtra("threat_level", 0)

        /** Save and Back button control binding */
        val saveButton = findViewById<Button>(R.id.btn_save)
        val backButton = findViewById<ImageView>(R.id.btn_back)

        /** Back button simply closes the activity without saving */
        backButton.setOnClickListener {
            finish()
        }

        // =========================================================================
        // POPULATE EXISTING KEYWORDS
        // =========================================================================
        /** Load keywords for this threat level from settings manager */
        val stringKeywords = settingsManager.getKeywords(threatLevel)
        val inflater = LayoutInflater.from(this)

        /** For each keyword, inflate a row and populate it */
        for (keyword in stringKeywords) {
            addKeywordRow(inflater, keyword)
        }

        // =========================================================================
        // SAVE BUTTON HANDLER
        // =========================================================================
        /** Collect keywords from each row */
        saveButton.setOnClickListener {
            val updatedKeywords = keywordViews.mapNotNull { (editText, _) ->
                val word = editText.text.toString().trim()
                if (word.isNotEmpty()) word.lowercase() else null
            }.toSet()

            /** Persist the updated set and signal success */
            settingsManager.saveKeywordList(threatLevel, updatedKeywords)
            setResult(RESULT_OK)
            finish()
        }
    }

    // =========================================================================
    // DYNAMIC ROW CREATION AND DELETION
    // =========================================================================
    /** Inflates a keyword row containing editTest and Deletion Icon */
    private fun addKeywordRow(inflater: LayoutInflater, word: String) {
        val row = inflater.inflate(R.layout.item_edit_keyword, containerKeywords, false)
        val editText = row.findViewById<EditText>(R.id.edit_keyword)
        val deleteIcon = row.findViewById<ImageView>(R.id.btn_delete)
        /** Pre-fill the EditText with the existing keyword */
        editText.setText(word)
        /** Add this row view to the parent container */
        containerKeywords.addView(row)

        /** On delete icon click: remove the row view and its tracking entry */
        deleteIcon.setOnClickListener {
            containerKeywords.removeView(row)
            keywordViews.removeAll { it.first == editText }
        }

        /** Keep track of this row’s EditText and delete button for saving later */
        keywordViews.add(Pair(editText, deleteIcon))
    }
}
