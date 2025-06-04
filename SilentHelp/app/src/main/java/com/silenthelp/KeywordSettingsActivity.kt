// Created by Kelley Rosa 05-22-25

package com.silenthelp

import android.os.Bundle
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast



class KeywordSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_keyword_settings)

        // Functionality for Back Button
        val backButton: ImageView = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }


        // Spinner setup goes here
        val spinner: Spinner = findViewById(R.id.spinner_threat_level)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.threat_levels,
            R.layout.spinner_keyword_settings_item_selected
        )
        adapter.setDropDownViewResource(R.layout.spinner_keyword_settings_dropdown_menu)
        spinner.adapter = adapter

        // spinner selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // You can react here based on selection
                val selectedLevel = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@KeywordSettingsActivity, "Selected: $selectedLevel", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
