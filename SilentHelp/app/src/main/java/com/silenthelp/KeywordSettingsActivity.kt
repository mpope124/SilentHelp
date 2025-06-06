package com.silenthelp

import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.silenthelp.adapter.KeywordAdapter
import com.silenthelp.data.KeywordManager
import com.silenthelp.model.Keyword

class KeywordSettingsActivity : AppCompatActivity() {

    // Declare manager and adapter for keywords
    private lateinit var keywordManager: KeywordManager
    private lateinit var keywordAdapter: KeywordAdapter

    // Declare input and list views
    private lateinit var inputKeyword: EditText
    private lateinit var spinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_keyword_settings)
        supportActionBar?.hide() // Hide default top bar

        // Initialize views
        inputKeyword = findViewById(R.id.input_new_keyword)
        spinner = findViewById(R.id.spinner_threat_level)
        recyclerView = findViewById(R.id.recycler_level1)
        addButton = findViewById(R.id.btn_add_keyword)
        backButton = findViewById(R.id.btn_back)

        // Initialize KeywordManager
        keywordManager = KeywordManager(applicationContext)

        // Initialize Spinner for threat levels
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            R.layout.spinner_keyword_settings_item_selected
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_keyword_settings_dropdown_menu)
        spinner.adapter = spinnerAdapter

        // Handle Back Button click
        backButton.setOnClickListener {
            finish()
        }

        // Initialize RecyclerView and its adapter
        keywordAdapter = KeywordAdapter(
            keywordManager.getKeywords().toMutableList(),
            onEdit = { index, keyword ->
                inputKeyword.setText(keyword.word)
                spinner.setSelection(keyword.level)
                keywordAdapter.setEditingIndex(index)
            },
            onDelete = { index ->
                keywordManager.removeKeyword(index)
                keywordAdapter.updateData(keywordManager.getKeywords())
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = keywordAdapter

        // Add or update keyword on button click
        addButton.setOnClickListener {
            val word = inputKeyword.text.toString().trim()
            val level = spinner.selectedItemPosition

            if (word.isBlank()) {
                Toast.makeText(this, "Please enter a keyword", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentIndex = keywordAdapter.getEditingIndex()
            if (currentIndex != null) {
                // Update keyword
                keywordManager.updateKeyword(currentIndex, Keyword(word, level))
                keywordAdapter.setEditingIndex(null)
            } else {
                // Add new keyword
                keywordManager.addKeyword(Keyword(word, level))
            }

            // Refresh list and clear input
            inputKeyword.text.clear()
            spinner.setSelection(0)
            keywordAdapter.updateData(keywordManager.getKeywords())
        }
    }

    // Hide keyboard and collapse edit state on outside touch
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()

            keywordAdapter.setEditingIndex(null)
        }
        return super.dispatchTouchEvent(ev)
    }
}
