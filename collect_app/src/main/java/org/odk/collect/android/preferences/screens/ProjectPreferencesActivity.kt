/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.odk.collect.android.preferences.screens

import android.os.Bundle
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.CollectAbstractActivity
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.fragments.dialogs.MovingBackwardsDialog.MovingBackwardsDialogListener
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog.ResetSettingsResultDialogListener
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.listeners.OnBackPressedListener
import org.odk.collect.android.logic.PropertyManager
import javax.inject.Inject

class ProjectPreferencesActivity :
    CollectAbstractActivity(),
    ResetSettingsResultDialogListener,
    MovingBackwardsDialogListener {

    private var onBackPressedListener: OnBackPressedListener? = null

    @Inject
    lateinit var propertyManager: PropertyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences_layout)
        DaggerUtils.getComponent(this).inject(this)
    }

    override fun onPause() {
        super.onPause()
        propertyManager.reload()
    }

    // If the onBackPressedListener is set then onBackPressed is delegated to it.
    override fun onBackPressed() {
        if (onBackPressedListener != null) {
            onBackPressedListener!!.doBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDialogClosed() {
        ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
    }

    override fun preventOtherWaysOfEditingForm() {
        val fragment = supportFragmentManager.findFragmentById(R.id.preferences_fragment_container) as FormEntryAccessPreferencesFragment
        fragment.preventOtherWaysOfEditingForm()
    }

    fun setOnBackPressedListener(onBackPressedListener: OnBackPressedListener?) {
        this.onBackPressedListener = onBackPressedListener
    }
}
