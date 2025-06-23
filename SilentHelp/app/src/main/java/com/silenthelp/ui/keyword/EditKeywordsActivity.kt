// Edit existing keywords for a specific threat level
// Created by Kelley Rosa

package com.silenthelp.ui.keyword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.graphics.Rect
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager

class EditKeywordsActivity : AppCompatActivity() {

    // =========================================================================
    // MEMBER DECLARATIONS
    // =========================================================================
    private lateinit var settingsManager: SettingsManager
    private lateinit var containerKeywords: LinearLayout
    private var threatLevel: Int = 0
    private val keywordViews = mutableListOf<Pair<TextView, View>>() // (TextView, rowView)

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_keywords)
        supportActionBar?.hide()

        settingsManager = SettingsManager.getInstance(this)
        containerKeywords = findViewById(R.id.container_keywords)
        threatLevel = intent.getIntExtra("threat_level", 0)

        findViewById<TextView>(R.id.edit_keywords_title).text = "Edit Level $threatLevel Keywords"
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        /** Load existing keywords for this threat level */
        val keywords = settingsManager.getKeywords(threatLevel)
        val inflater = LayoutInflater.from(this)
        for (word in keywords) {
            addKeywordRow(inflater, word)
        }
    }

    // =========================================================================
    // BUILD INDIVIDUAL KEYWORD ROWS
    // =========================================================================
    private fun addKeywordRow(inflater: LayoutInflater, word: String) {
        val row = inflater.inflate(R.layout.item_edit_keyword, containerKeywords, false)

        /** View Mode */
        val keywordText = row.findViewById<TextView>(R.id.keyword_text)
        val editIcon = row.findViewById<ImageView>(R.id.edit_button)
        val deleteIcon = row.findViewById<ImageView>(R.id.delete_button)

        /** Edit Mode */
        val keywordEdit = row.findViewById<EditText>(R.id.keyword_edit)
        val editButtons = row.findViewById<LinearLayout>(R.id.edit_buttons)
        val saveBtn = row.findViewById<Button>(R.id.btn_save)
        val cancelBtn = row.findViewById<Button>(R.id.btn_cancel)

        /**  Track original word explicitly */
        var originalWord = word

        /** Initial setup */
        keywordText.text = originalWord
        keywordEdit.setText(originalWord)
        containerKeywords.addView(row)
        keywordViews.add(Pair(keywordText, row))

        /** Edit Button */
        editIcon.setOnClickListener {
            keywordEdit.setText(keywordText.text.toString())
            keywordText.visibility = View.GONE
            keywordEdit.visibility = View.VISIBLE
            editButtons.visibility = View.VISIBLE
            editIcon.visibility = View.GONE
            deleteIcon.visibility = View.GONE

            keywordEdit.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(keywordEdit, InputMethodManager.SHOW_IMPLICIT)
        }

        /** Save Button */
        saveBtn.setOnClickListener {
            val newWord = keywordEdit.text.toString().trim().lowercase()

            if (newWord.isEmpty()) {
                Toast.makeText(this, "Keyword cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentList = settingsManager.getKeywords(threatLevel).toMutableSet()

            /** Remove original, insert new only if it's changed */
            if (newWord != originalWord) {
                currentList.remove(originalWord)
                currentList.add(newWord)
                settingsManager.saveKeywordList(threatLevel, currentList)
                keywordText.text = newWord
                originalWord = newWord
                setResult(RESULT_OK)
            }

            /** Exit edit mode */
            keywordEdit.visibility = View.GONE
            keywordText.visibility = View.VISIBLE
            editButtons.visibility = View.GONE
            editIcon.visibility = View.VISIBLE
            deleteIcon.visibility = View.VISIBLE

            /** Hide keyboard */
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(keywordEdit.windowToken, 0)
        }

        /** Cancel Button */
        cancelBtn.setOnClickListener {
            keywordEdit.setText(originalWord)
            keywordEdit.visibility = View.GONE
            keywordText.visibility = View.VISIBLE
            editButtons.visibility = View.GONE
            editIcon.visibility = View.VISIBLE
            deleteIcon.visibility = View.VISIBLE

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(keywordEdit.windowToken, 0)
        }

        /** Delete Button */
        deleteIcon.setOnClickListener {
            val updatedList = settingsManager.getKeywords(threatLevel).toMutableSet()
            updatedList.remove(originalWord)
            settingsManager.saveKeywordList(threatLevel, updatedList)
            containerKeywords.removeView(row)
            keywordViews.removeAll { it.first == keywordText }
            setResult(RESULT_OK)
        }
    }

    // =========================================================================
    // TAP OUTSIDE TO CANCEL EDIT + HIDE KEYBOARD
    // =========================================================================
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            for ((keywordText, row) in keywordViews) {
                val keywordEdit = row.findViewById<EditText>(R.id.keyword_edit)
                val editButtons = row.findViewById<LinearLayout>(R.id.edit_buttons)
                val editIcon = row.findViewById<ImageView>(R.id.edit_button)
                val deleteIcon = row.findViewById<ImageView>(R.id.delete_button)

                if (keywordEdit.visibility == View.VISIBLE) {
                    val outRect = Rect()
                    row.getGlobalVisibleRect(outRect)

                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        /** Revert text and exit edit mode */
                        keywordEdit.setText(keywordText.text)

                        keywordEdit.visibility = View.GONE
                        editButtons.visibility = View.GONE
                        keywordText.visibility = View.VISIBLE
                        editIcon.visibility = View.VISIBLE
                        deleteIcon.visibility = View.VISIBLE

                        /** Hide keyboard */
                        if (focusedView != null) {
                            focusedView.clearFocus()
                            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
                        }
                    }
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

}
