// Manages the keyword-entry UI: displays existing keywords per level, allows adding new keywords, and launches the edit screen.
// Created by Kelley Rosa
package com.silenthelp.ui.keyword

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager


class KeywordSettingsActivity : AppCompatActivity() {
    // =========================================================================
    // VIEW REFERENCES
    // =========================================================================
    private lateinit var backButton: ImageView
    private lateinit var inputKeyword: EditText
    private lateinit var spinner: Spinner
    private lateinit var addButton: Button

    private lateinit var listLevel1: TextView
    private lateinit var listLevel2: TextView
    private lateinit var listLevel3: TextView
    private lateinit var listLevel4: TextView

    private lateinit var linkEditLevel1: TextView
    private lateinit var linkEditLevel2: TextView
    private lateinit var linkEditLevel3: TextView
    private lateinit var linkEditLevel4: TextView

    private val settingsManager by lazy { SettingsManager.getInstance(this) }

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyword_settings)

        supportActionBar?.hide()

        backButton = findViewById(R.id.btn_back)
        inputKeyword = findViewById(R.id.input_new_keyword)
        spinner = findViewById(R.id.spinner_threat_level)
        addButton = findViewById(R.id.btn_add_keyword)

        listLevel1 = findViewById(R.id.list_level1_keywords)
        listLevel2 = findViewById(R.id.list_level2_keywords)
        listLevel3 = findViewById(R.id.list_level3_keywords)
        listLevel4 = findViewById(R.id.list_level4_keywords)

        linkEditLevel1 = findViewById(R.id.link_edit_level1)
        linkEditLevel2 = findViewById(R.id.link_edit_level2)
        linkEditLevel3 = findViewById(R.id.link_edit_level3)
        linkEditLevel4 = findViewById(R.id.link_edit_level4)

        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            R.layout.spinner_keyword_settings_item_selected
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_keyword_settings_dropdown_menu)
        spinner.adapter = spinnerAdapter

        backButton.setOnClickListener {
            finish()
        }

        addButton.setOnClickListener {
            val word = inputKeyword.text.toString().trim()
            val level = spinner.selectedItemPosition + 1

            if (word.isBlank()) {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existing = settingsManager.getKeywords(level)
            if (existing.any { it.equals(word, ignoreCase = true) }) {
                Toast.makeText(this, "Keyword already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = existing.toMutableSet()
            updated.add(word)
            settingsManager.saveKeywordList(level, updated)

            Toast.makeText(this, "Keyword added", Toast.LENGTH_SHORT).show()
            inputKeyword.text.clear()
            spinner.setSelection(0)
            refreshAllLevels()
        }

        refreshAllLevels()
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private fun refreshAllLevels() {
        refreshLevel(1, listLevel1, linkEditLevel1)
        refreshLevel(2, listLevel2, linkEditLevel2)
        refreshLevel(3, listLevel3, linkEditLevel3)
        refreshLevel(4, listLevel4, linkEditLevel4)
    }

    private fun refreshLevel(level: Int, listTextView: TextView, editLink: TextView) {
        val keywords = settingsManager.getKeywords(level)

        if (keywords.isEmpty()) {
            listTextView.text = getString(R.string.no_keywords_added)
            editLink.alpha = 0.4f
            editLink.isClickable = false
            editLink.setOnClickListener {
                Toast.makeText(this, "No keywords available to edit", Toast.LENGTH_SHORT).show()
            }
        } else {
            listTextView.text = keywords.joinToString("\n")
            editLink.alpha = 1.0f
            editLink.isClickable = true
            editLink.setOnClickListener {
                launchEditScreen(level)
            }
        }
    }

    private fun launchEditScreen(level: Int) {
        val intent = Intent(this, EditKeywordsActivity::class.java)
        intent.putExtra("threat_level", level)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            refreshAllLevels() //  Reload contact list after returning
        }
    }

    // =========================================================================
    // KEYBOARD HANDLING
    // =========================================================================

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && ev.action == MotionEvent.ACTION_DOWN) {
            val outRect = Rect()
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
