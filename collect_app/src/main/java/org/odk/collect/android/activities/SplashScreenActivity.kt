/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.odk.collect.android.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import kotlinx.android.synthetic.main.splash_screen.*
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.ScreenUtils
import java.io.File
import javax.inject.Inject

class SplashScreenActivity : Activity() {

    @Inject
    lateinit var settingsProvider: SettingsProvider

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // this splash screen should be a blank slate
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        DaggerUtils.getComponent(this).inject(this)
        init()
    }

    private fun init() {
        setContentView(R.layout.splash_screen)

        val isSplashScreenEnabled = settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_SHOW_SPLASH)
        val splashLogoPath = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_SPLASH_PATH)

        if (isSplashScreenEnabled && !splashLogoPath.isNullOrBlank()) {
            startSplashScreen(splashLogoPath)
        } else {
            endSplashScreen()
        }
    }

    private fun endSplashScreen() {
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
    }

    private fun startSplashScreen(path: String) {
        val customSplash = File(path)
        if (customSplash.exists()) {
            splash_default.visibility = View.GONE
            splash.setImageBitmap(FileUtils.getBitmapScaledToDisplay(customSplash, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth()))
            splash.visibility = View.VISIBLE
        }

        Handler().postDelayed({ endSplashScreen() }, 2000)
    }
}
