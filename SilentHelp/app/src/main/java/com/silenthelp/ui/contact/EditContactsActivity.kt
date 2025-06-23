// Edit existing emergency contacts for a specific threat level

package com.silenthelp.ui.contact

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import com.silenthelp.core.manager.SettingsManager
import com.silenthelp.core.model.Contact

class EditContactsActivity : AppCompatActivity() {

    // =========================================================================
    // MEMBER DECLARATIONS
    // =========================================================================
    private lateinit var settingsManager: SettingsManager
    private lateinit var containerContacts: LinearLayout
    private var threatLevel: Int = 0
    private val rowViews = mutableListOf<View>()

    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_contacts)
        supportActionBar?.hide()

        settingsManager = SettingsManager.getInstance(this)
        containerContacts = findViewById(R.id.container_contacts)
        threatLevel = intent.getIntExtra("threat_level", 0)

        findViewById<TextView>(R.id.edit_contacts_title).text = "Edit Level $threatLevel Contacts"
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            setResult(RESULT_OK)
            finish() }

        /** Load saved contacts for this level and inflate rows */
        val contacts = settingsManager.getContacts().filter { it.level == threatLevel }
        val inflater = LayoutInflater.from(this)
        for ((name, number) in contacts) {
            addContactRow(inflater, name, number)
        }
    }

    // =========================================================================
    // ROW CREATION
    // =========================================================================
    private fun addContactRow(inflater: LayoutInflater, name: String, number: String) {
        val row = inflater.inflate(R.layout.item_edit_contact, containerContacts, false)

        val originalName = name
        val originalPhone = number

        /** View Mode */
        val viewMode = row.findViewById<LinearLayout>(R.id.view_mode)
        val nameText = row.findViewById<TextView>(R.id.view_contact_name)
        val phoneText = row.findViewById<TextView>(R.id.view_contact_phone)
        val editIcon = row.findViewById<ImageView>(R.id.icon_edit)
        val deleteIcon = row.findViewById<ImageView>(R.id.icon_delete)

        /** Edit Mode */
        val editMode = row.findViewById<LinearLayout>(R.id.edit_mode)
        val nameInput = row.findViewById<EditText>(R.id.edit_contact_name)
        val phoneInput = row.findViewById<EditText>(R.id.edit_contact_phone)
        val saveButton = row.findViewById<Button>(R.id.btn_save)
        val cancelButton = row.findViewById<Button>(R.id.btn_cancel)

        /** Populate content */
        nameText.text = name
        phoneText.text = number
        nameInput.setText(name)
        phoneInput.setText(number)

        /** Add to container and track for dismiss logic */
        containerContacts.addView(row)
        rowViews.add(row)

        // =========================================================================
        // EVENT HANDLERS
        // =========================================================================

        /** Edit button — switch to edit mode */
        editIcon.setOnClickListener {
            viewMode.visibility = View.GONE
            editMode.visibility = View.VISIBLE

            nameInput.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT)
        }

        /** Cancel button — discard changes */
        cancelButton.setOnClickListener {
            nameInput.setText(nameText.text)
            phoneInput.setText(phoneText.text)
            editMode.visibility = View.GONE
            viewMode.visibility = View.VISIBLE

            /** Hide keyboard */
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(nameInput.windowToken, 0)

        }


        /** Save button */
        saveButton.setOnClickListener {
            val newName = nameInput.text.toString().trim()
                .split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercaseChar() } }

            val newPhone = phoneInput.text.toString().trim()

            if (newName.isBlank() || newPhone.isBlank()) {
                Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            nameText.text = newName
            phoneText.text = newPhone
            editMode.visibility = View.GONE
            viewMode.visibility = View.VISIBLE

            /** Hide keyboard */
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(nameInput.windowToken, 0)


            /** Persist the updated contact */
            val updatedContacts = settingsManager.getContacts().toMutableList()

            /** Find original contact by matching name + phone + level */
            val originalIndex = updatedContacts.indexOfFirst {
                it.level == threatLevel && it.name == originalName && it.phone == originalPhone
            }

            if (originalIndex != -1) {
                updatedContacts[originalIndex] = Contact(newName, newPhone, threatLevel)
                settingsManager.saveContactList(updatedContacts)
            }
            setResult(RESULT_OK)
        }

        /** Delete button */
        deleteIcon.setOnClickListener {
            /** Remove contact from saved list */
            val updatedContacts = settingsManager.getContacts().toMutableList()
            val removedIndex = updatedContacts.indexOfFirst {
                it.level == threatLevel && it.name == name && it.phone == number
            }
            if (removedIndex != -1) {
                updatedContacts.removeAt(removedIndex)
                settingsManager.saveContactList(updatedContacts)
            }

            containerContacts.removeView(row)
            rowViews.remove(row)
            setResult(RESULT_OK)

        }
    }

    // =========================================================================
    // HIDE KEYBOARD + CANCEL EDIT MODE ON OUTSIDE TAP
    // =========================================================================
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

            for (row in rowViews) {
                val editMode = row.findViewById<LinearLayout>(R.id.edit_mode)
                val viewMode = row.findViewById<LinearLayout>(R.id.view_mode)

                if (editMode.visibility == View.VISIBLE) {
                    val outRect = Rect()
                    row.getGlobalVisibleRect(outRect)

                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        /** Restore original text */
                        val nameInput = row.findViewById<EditText>(R.id.edit_contact_name)
                        val phoneInput = row.findViewById<EditText>(R.id.edit_contact_phone)
                        val nameText = row.findViewById<TextView>(R.id.view_contact_name)
                        val phoneText = row.findViewById<TextView>(R.id.view_contact_phone)

                        nameInput.setText(nameText.text)
                        phoneInput.setText(phoneText.text)

                        /** Exit edit mode */
                        editMode.visibility = View.GONE
                        viewMode.visibility = View.VISIBLE

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