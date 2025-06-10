package com.silenthelp.ui.user

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.silenthelp.R

class UserSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usersettingslayout)

        val backButton: ImageView = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }
    }
}