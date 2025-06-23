// Displays the log of past keyword detections
// Created By Michael Pope
package com.silenthelp.ui.incident

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.silenthelp.R
import com.silenthelp.models.Incident
import com.silenthelp.repository.IncidentRepository
import com.silenthelp.ui.adapter.IncidentAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class IncidentLogActivity : AppCompatActivity() {
    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    private lateinit var adapter: IncidentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for Incident Log */
        setContentView(R.layout.activity_incident_log)

        /** Hides Global ActionBar */
        supportActionBar?.hide()
        val backButton: ImageView = findViewById(R.id.btn_back)
        /** Closes activity when back arrow is tapped */
        backButton.setOnClickListener {
            finish()
        }

        adapter = IncidentAdapter(emptyList())
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadIncidents()
    }

        private fun loadIncidents() {
            IncidentRepository.api.getIncidents().enqueue(object : Callback<List<Incident>> {
                override fun onResponse(
                    call: Call<List<Incident>>,
                    response: Response<List<Incident>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            adapter.updateData(it)
                        }
                    } else {
                        Log.e("API", "Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Incident>>, t: Throwable) {
                    Log.e("API", "Failed: ${t.message}")
                }
            })
        }
    }





