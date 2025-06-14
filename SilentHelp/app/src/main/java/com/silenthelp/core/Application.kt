// Created by Kelley Rosa 06-05-25
package com.silenthelp.core

import android.app.Application
import com.silenthelp.core.manager.KeywordManager
import com.silenthelp.core.manager.SettingsManager

class SilentHelpApp : Application() {

    lateinit var settingsManager: SettingsManager
        private set

    lateinit var keywordManager: KeywordManager
        private set

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
        keywordManager = KeywordManager(this)
    }
}
