// Manages the keyword-entry UI: displays existing keywords per level, allows adding new keywords, and launches the edit screen.
// Created by Kelley Rosa
package com.silenthelp.ui.keyword

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager
import android.graphics.Paint
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager



class KeywordSettingsActivity : AppCompatActivity() {
    // =========================================================================
    // VIEW REFERENCES
    // =========================================================================
    /** Back arrow in header */
    private lateinit var backButton: ImageView
    /** Text field to enter a new keyword */
    private lateinit var inputKeyword: EditText
    /** Dropdown to choose threat level */
    private lateinit var spinner: Spinner
    /** Add button to add the entered keyword */
    private lateinit var addButton: Button

    /** Display Textview for each threat level card */
    private lateinit var listLevel1: TextView
    private lateinit var listLevel2: TextView
    private lateinit var listLevel3: TextView
    private lateinit var listLevel4: TextView

    /** Manager for storing and retrieving keyword data */
    private lateinit var settingsManager: SettingsManager

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Set Layout for keyword settings */
        setContentView(R.layout.activity_keyword_settings)

        /** Hides Global ActionBar */
        supportActionBar?.hide()

        /** Underlines edit section links for each threat level card */
        val linkEditLevel1 = findViewById<TextView>(R.id.link_edit_level1)
        val linkEditLevel2 = findViewById<TextView>(R.id.link_edit_level2)
        val linkEditLevel3 = findViewById<TextView>(R.id.link_edit_level3)
        val linkEditLevel4 = findViewById<TextView>(R.id.link_edit_level4)

        listOf(linkEditLevel1, linkEditLevel2, linkEditLevel3, linkEditLevel4).forEach { tv ->
            tv.paintFlags = tv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        // =========================================================================
        // BIND VIEWS
        // =========================================================================
        backButton = findViewById(R.id.btn_back)
        inputKeyword = findViewById(R.id.input_new_keyword)
        spinner = findViewById(R.id.spinner_threat_level)
        addButton = findViewById(R.id.btn_add_keyword)

        listLevel1 = findViewById(R.id.list_level1_keywords)
        listLevel2 = findViewById(R.id.list_level2_keywords)
        listLevel3 = findViewById(R.id.list_level3_keywords)
        listLevel4 = findViewById(R.id.list_level4_keywords)

        /** Initialize SettingsManager */
        settingsManager = SettingsManager(applicationContext)

        // =========================================================================
        // SETUP SPINNER
        // =========================================================================
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            R.layout.spinner_keyword_settings_item_selected
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_keyword_settings_dropdown_menu)
        spinner.adapter = spinnerAdapter

        // =========================================================================
        // EVENT HANDLERS
        // =========================================================================

        /** Back arrow closes this activity */
        backButton.setOnClickListener {
            finish()
        }

        /** Add button updates prefs */
        addButton.setOnClickListener {
            val word = inputKeyword.text.toString().trim()
            val level = spinner.selectedItemPosition + 1

            /* Ensure non-empty input */
            if (word.isBlank()) {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /* Prevents duplicates */
            val existing = settingsManager.getKeywords(level)
            if (existing.any { it.equals(word, ignoreCase = true) }) {
                Toast.makeText(this, "Keyword already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /* Save new keyword set */
            val updated = existing.toMutableSet()
            updated.add(word)
            settingsManager.saveKeywordList(level, updated)

            Toast.makeText(this, "Keyword added", Toast.LENGTH_SHORT).show()
            inputKeyword.text.clear()
            spinner.setSelection(0)
            refreshKeywordLists()
        }

        /** Launch the edit screen for each level when its “Edit” link is tapped */
        linkEditLevel1.setOnClickListener { launchEditScreen(1) }
        linkEditLevel2.setOnClickListener { launchEditScreen(2) }
        linkEditLevel3.setOnClickListener { launchEditScreen(3) }
        linkEditLevel4.setOnClickListener { launchEditScreen(4) }

        /** Initial population of the keyword lists */
        refreshKeywordLists()
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /** Refreshes each level’s TextView with the latest keyword set */
    private fun refreshKeywordLists() {
        listLevel1.text = formatList(settingsManager.getKeywords(1))
        listLevel2.text = formatList(settingsManager.getKeywords(2))
        listLevel3.text = formatList(settingsManager.getKeywords(3))
        listLevel4.text = formatList(settingsManager.getKeywords(4))
    }

    /** Formats a Set<String> into a multi-line string or shows a placeholder */
    private fun formatList(set: Set<String>): String {
        return if (set.isEmpty()) "No keywords added" else set.joinToString("\n")
    }

    /** Starts EditKeywordsActivity for the given threat level */
    private fun launchEditScreen(level: Int) {
        val intent = Intent(this, EditKeywordsActivity::class.java)
        intent.putExtra("threat_level", level)
        startActivityForResult(intent, 1000)
    }

    /** After returning from the edit screen, refresh lists if changed */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            refreshKeywordLists()
        }
    }

    // =========================================================================
    // KEYBOARD HANDLING
    // =========================================================================

    /** Hides the soft keyboard when the user taps outside an input field. */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && ev.action == MotionEvent.ACTION_DOWN) {
            val outRect = android.graphics.Rect()
            view.getGlobalVisibleRect(outRect)
            if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                view.clearFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
