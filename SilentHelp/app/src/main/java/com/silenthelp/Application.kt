// Created by Kelley Rosa 06-05-25
package com.silenthelp

import android.app.Application
import com.silenthelp.manager.KeywordManager
import com.silenthelp.manager.SettingsManager

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
