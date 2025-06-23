// Initializes and exposes the core managers for the entire app
// Created by Kelley Rosa
package com.silenthelp.core

import android.app.Application
import com.silenthelp.core.manager.SettingsManager
import com.silenthelp.models.Incident
import com.silenthelp.repository.IncidentRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log


class SilentHelpApp : Application() {

    // =========================================================================
    // GLOBAL MANAGERS
    // =========================================================================
    lateinit var settingsManager: SettingsManager
        private set


    // =========================================================================
    // APPLICATION LIFECYCLE
    // =========================================================================
    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
    }
}

