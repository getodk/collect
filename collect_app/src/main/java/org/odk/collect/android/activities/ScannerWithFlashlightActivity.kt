/*
 * Copyright (C) 2017 Shobhit
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

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import org.odk.collect.android.R
import org.odk.collect.android.application.CollectComposeThemeProvider
import org.odk.collect.strings.localization.LocalizedActivity

/**
 * Custom Scannner Activity extending from Activity to display a custom layout form scanner view.
 */
class ScannerWithFlashlightActivity : LocalizedActivity(), CollectComposeThemeProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(systemBars())

        // Remove rotation animation - we'll handle configuration changes in Fragments
        this.window.attributes.rotationAnimation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.ROTATION_ANIMATION_SEAMLESS
        } else {
            WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE
        }

        setContentView(R.layout.activity_custom_scanner)
    }
}
