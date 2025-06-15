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

    private lateinit var backButton: ImageView
    private lateinit var inputKeyword: EditText
    private lateinit var spinner: Spinner
    private lateinit var addButton: Button
    private lateinit var settingsManager: SettingsManager


    // Display areas for keywords under each threat level card
    private lateinit var listLevel1: TextView
    private lateinit var listLevel2: TextView
    private lateinit var listLevel3: TextView
    private lateinit var listLevel4: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_keyword_settings)
        supportActionBar?.hide()

        val linkEditLevel1 = findViewById<TextView>(R.id.link_edit_level1)
        val linkEditLevel2 = findViewById<TextView>(R.id.link_edit_level2)
        val linkEditLevel3 = findViewById<TextView>(R.id.link_edit_level3)
        val linkEditLevel4 = findViewById<TextView>(R.id.link_edit_level4)

        // Apply underline
        linkEditLevel1.paintFlags = linkEditLevel1.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        linkEditLevel2.paintFlags = linkEditLevel2.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        linkEditLevel3.paintFlags = linkEditLevel3.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        linkEditLevel4.paintFlags = linkEditLevel4.paintFlags or Paint.UNDERLINE_TEXT_FLAG


        // Initialize views
        backButton = findViewById(R.id.btn_back)
        inputKeyword = findViewById(R.id.input_new_keyword)
        spinner = findViewById(R.id.spinner_threat_level)
        addButton = findViewById(R.id.btn_add_keyword)

        listLevel1 = findViewById(R.id.list_level1_keywords)
        listLevel2 = findViewById(R.id.list_level2_keywords)
        listLevel3 = findViewById(R.id.list_level3_keywords)
        listLevel4 = findViewById(R.id.list_level4_keywords)

        settingsManager = SettingsManager(applicationContext)

        // Set up back button
        backButton.setOnClickListener {
            finish()
        }

        // Set up threat level spinner
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            R.layout.spinner_keyword_settings_item_selected
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_keyword_settings_dropdown_menu)
        spinner.adapter = spinnerAdapter

        // Handle Add button click
        addButton.setOnClickListener {
            val word = inputKeyword.text.toString().trim()
            val level = spinner.selectedItemPosition + 1 // Level indices start from 1

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
            refreshKeywordLists()
        }

        // Launch Edit screen for each level
        findViewById<TextView>(R.id.link_edit_level1).setOnClickListener {
            launchEditScreen(1)
        }
        findViewById<TextView>(R.id.link_edit_level2).setOnClickListener {
            launchEditScreen(2)
        }
        findViewById<TextView>(R.id.link_edit_level3).setOnClickListener {
            launchEditScreen(3)
        }
        findViewById<TextView>(R.id.link_edit_level4).setOnClickListener {
            launchEditScreen(4)
        }

        // Initial keyword display
        refreshKeywordLists()
    }

    private fun refreshKeywordLists() {
        listLevel1.text = formatList(settingsManager.getKeywords(1))
        listLevel2.text = formatList(settingsManager.getKeywords(2))
        listLevel3.text = formatList(settingsManager.getKeywords(3))
        listLevel4.text = formatList(settingsManager.getKeywords(4))
    }

    private fun formatList(set: Set<String>): String {
        return if (set.isEmpty()) "No keywords added" else set.joinToString("\n")
    }

    private fun launchEditScreen(level: Int) {
        val intent = Intent(this, EditKeywordsActivity::class.java)
        intent.putExtra("threat_level", level)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            refreshKeywordLists()
        }
    }

    // Hide Keyboard
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
