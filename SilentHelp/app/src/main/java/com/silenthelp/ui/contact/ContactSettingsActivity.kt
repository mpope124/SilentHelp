// Allows user to add a contact with an associated threat level
// Created By Collin Jones
package com.silenthelp.ui.contact

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
import com.silenthelp.core.model.Contact


class ContactSettingsActivity : AppCompatActivity() {

    // =========================================================================
    // VIEW REFERENCES
    // =========================================================================
    /** Back arrow in header */
    private lateinit var backButton: ImageView
    /** Text field to enter a new contact */
    private lateinit var nameInput: EditText
    /** Text field to enter phone number for contact */
    private lateinit var phoneInput: EditText
    /** Dropdown to choose threat level */
    private lateinit var threatLevelSpinner: Spinner
    /** Add button to add the entered keyword */
    private lateinit var addButton: Button

    /** Display Textview for each threat level card */
    private lateinit var listLevel1: TextView
    private lateinit var listLevel2: TextView
    private lateinit var listLevel3: TextView
    private lateinit var listLevel4: TextView

    private lateinit var editLink1: TextView
    private lateinit var editLink2: TextView
    private lateinit var editLink3: TextView
    private lateinit var editLink4: TextView

    /** Shared settings manager for saving and retrieving data */
    private val settingsManager by lazy { SettingsManager.getInstance(this) }

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_settings)

        /** Hides Global ActionBar */
        supportActionBar?.hide()

        // =========================================================================
        // VIEW BINDINGS
        // =========================================================================
        backButton= findViewById(R.id.btn_back)
        nameInput = findViewById(R.id.contactName)
        phoneInput = findViewById(R.id.editTextPhone)
        threatLevelSpinner = findViewById(R.id.spinner_threat_level)
        addButton = findViewById(R.id.button)

        /** Initialize Contact List */
        listLevel1 = findViewById(R.id.list_level1_contacts)
        listLevel2 = findViewById(R.id.list_level2_contacts)
        listLevel3 = findViewById(R.id.list_level3_contacts)
        listLevel4 = findViewById(R.id.list_level4_contacts)

        /** Initialize Edit Section Links */
        editLink1 = findViewById(R.id.link_edit_level1)
        editLink2 = findViewById(R.id.link_edit_level2)
        editLink3 = findViewById(R.id.link_edit_level3)
        editLink4 = findViewById(R.id.link_edit_level4)

        // =========================================================================
        // SPINNER SETUP
        // =========================================================================
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            R.layout.spinner_keyword_settings_item_selected
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_keyword_settings_dropdown_menu)
        threatLevelSpinner.adapter = spinnerAdapter

        // =========================================================================
        // EVENT LISTENERS
        // =========================================================================
        /** Closes activity when back arrow is tapped */
        backButton.setOnClickListener {
            finish()
        }

        /** Add contact button saves the data */
        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim().capitalizeWords()
            val phone = phoneInput.text.toString().trim()
            val level = threatLevelSpinner.selectedItemPosition + 1

            if (name.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please enter both name and phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newContact = Contact(name, phone, level)
            val updated = settingsManager.getContacts().toMutableList()
            updated.add(newContact)
            settingsManager.saveContactList(updated)

            Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show()
            nameInput.text.clear()
            phoneInput.text.clear()
            threatLevelSpinner.setSelection(0)
            refreshAllLevels()
        }

        /** Initial population of the contact lists */
        refreshAllLevels()
    }
    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /** Refreshes all four levels of contact cards */
    private fun refreshAllLevels() {
        refreshLevel(1, listLevel1, editLink1)
        refreshLevel(2, listLevel2, editLink2)
        refreshLevel(3, listLevel3, editLink3)
        refreshLevel(4, listLevel4, editLink4)
    }

    /** Capitalized Words */
    private fun String.capitalizeWords(): String =
        this.lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }

    /** Refreshes a single threat level's card with contacts and edit link */
    private fun refreshLevel(level: Int, listTextView: TextView, editLink: TextView) {
        val contacts = settingsManager.getContacts().filter { it.level == level }

        if (contacts.isEmpty()) {
            listTextView.text = getString(R.string.no_contacts_added)
            editLink.alpha = 0.4f
            editLink.isClickable = false
            editLink.setOnClickListener {
                Toast.makeText(this, "No contacts available to edit. Please add one first.", Toast.LENGTH_SHORT).show()
            }
        } else {
            listTextView.text = contacts.joinToString("\n") { it.name }
            editLink.alpha = 1.0f
            editLink.isClickable = true
            editLink.setOnClickListener {
                launchEditScreen(level)
            }
        }
    }

    /** Launches the edit contacts screen for a specific threat level */
    private fun launchEditScreen(level: Int) {
        val intent = Intent(this, EditContactsActivity::class.java)
        intent.putExtra("threat_level", level)
        startActivityForResult(intent, 1000)
    }

    /** After returning from edit screen, refresh UI */
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            refreshAllLevels() //  Reload contact list after returning
        }
    }

    /** Hides keyboard when tapping outside input fields */
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