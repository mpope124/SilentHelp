package com.silenthelp.ui.keyword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.manager.SettingsManager

class EditKeywordsActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var containerKeywords: LinearLayout
    private var threatLevel: Int = 0
    private val keywordViews = mutableListOf<Pair<EditText, View>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_keywords)
        supportActionBar?.hide()

        settingsManager = SettingsManager(this)
        containerKeywords = findViewById(R.id.container_keywords)

        threatLevel = intent.getIntExtra("threat_level", 0)
        val saveButton = findViewById<Button>(R.id.btn_save)
        val backButton = findViewById<ImageView>(R.id.btn_back)

        backButton.setOnClickListener {
            finish()
        }

        // Load keywords for this threat level
        val stringKeywords = settingsManager.getKeywords(threatLevel)
        val inflater = LayoutInflater.from(this)

        for (keyword in stringKeywords) {
            addKeywordRow(inflater, keyword)
        }

        saveButton.setOnClickListener {
            val updatedKeywords = keywordViews.mapNotNull { (editText, _) ->
                val word = editText.text.toString().trim()
                if (word.isNotEmpty()) word.lowercase() else null
            }.toSet()

            settingsManager.saveKeywordList(threatLevel, updatedKeywords)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun addKeywordRow(inflater: LayoutInflater, word: String) {
        val row = inflater.inflate(R.layout.item_edit_keyword, containerKeywords, false)
        val editText = row.findViewById<EditText>(R.id.edit_keyword)
        val deleteIcon = row.findViewById<ImageView>(R.id.btn_delete)

        editText.setText(word)
        containerKeywords.addView(row)

        deleteIcon.setOnClickListener {
            containerKeywords.removeView(row)
            keywordViews.removeAll { it.first == editText }
        }

        keywordViews.add(Pair(editText, deleteIcon))
    }

}
