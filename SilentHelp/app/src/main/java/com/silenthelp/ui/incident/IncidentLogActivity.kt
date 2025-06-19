// Displays the log of past keyword detections
// Created By Michael Pope
package com.silenthelp.ui.incident

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.silenthelp.R
import android.util.Log
import com.silenthelp.models.Incident
import com.silenthelp.repository.IncidentRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class IncidentLogActivity : AppCompatActivity() {
    // =========================================================================
    // ACTIVITY LIFECYCLE
    // =========================================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set Layout for Incident Log */
        setContentView(R.layout.activity_incident_log)

        IncidentRepository.api.getIncidents().enqueue(object : Callback<List<Incident>> {
            override fun onResponse(call: Call<List<Incident>>, response: Response<List<Incident>>) {
                if (response.isSuccessful) {
                    val incidents = response.body()
                    Log.d("API", "Received ${incidents?.size} incidents")
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





