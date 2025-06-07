package com.silenthelp.ui.incident

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R

class IncidentLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.incident_log)

//        findViewById<Button>(R.id.button_back_to_home).setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            startActivity(intent)
//            finish()          // optional: removes this activity from the stack
//        }

    }
}