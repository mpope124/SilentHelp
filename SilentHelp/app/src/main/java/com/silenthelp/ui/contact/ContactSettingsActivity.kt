// Allows user to add a contact with an associated threat level
// Created By Collin Jones
package com.silenthelp.ui.contact

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.core.model.Contact
import com.silenthelp.core.manager.SettingsManager

class ContactSettingsActivity : AppCompatActivity() {

    // =========================================================================
    // MEMBER DECLARATIONS
    // =========================================================================
    /** Manager for storing and retrieving contacts and other settings */
    private lateinit var settingsManager: SettingsManager

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for adding a new contact */
        setContentView(R.layout.activity_contact_settings)

        /** Hides Global ActionBar */
        supportActionBar?.hide()

        /** Initialize SettingsManager */
        settingsManager = SettingsManager(this)

        // =========================================================================
        // VIEW BINDINGS
        // =========================================================================
        val backButton: ImageView = findViewById(R.id.btn_back)
        val nameInput: EditText = findViewById(R.id.contactName)
        val phoneInput: EditText = findViewById(R.id.editTextPhone)
        val threatLevelSpinner: Spinner = findViewById(R.id.spinner_threat_level)
        val addButton: Button = findViewById(R.id.button)

        // =========================================================================
        // SPINNER SETUP
        // =========================================================================
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        threatLevelSpinner.adapter = adapter

        // =========================================================================
        // EVENT LISTENERS
        // =========================================================================
        /** Closes activity when back arrow is tapped */
        backButton.setOnClickListener {
            finish()
        }

        /** Add contact button saves the data */
        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val level = threatLevelSpinner.selectedItemPosition + 1

            /* Validates Inputs */
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /* Creates and persists the new contact */
            val newContact = Contact(name, phone, level)
            val existingContacts = settingsManager.getContacts().toMutableList()
            existingContacts.add(newContact)
            settingsManager.saveContacts(existingContacts)

            /* Provide feedback and reset the form */
            Toast.makeText(this, "Contact saved", Toast.LENGTH_SHORT).show()
            nameInput.text.clear()
            phoneInput.text.clear()
            threatLevelSpinner.setSelection(0)
        }
    }
}