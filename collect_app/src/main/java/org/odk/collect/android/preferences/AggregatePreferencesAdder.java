/*
 * Copyright (C) 2018 Callum Stott
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

package org.odk.collect.android.preferences;

import android.preference.PreferenceFragment;
import android.widget.Toast;

import org.odk.collect.android.R;

/**
 * Extracted use case class to isolate and allow testing of functionality (in this
 * case error handling). Originally contained in {@link ServerPreferencesFragment}.
 **/
class AggregatePreferencesAdder {

    private final PreferenceFragment fragment;

    AggregatePreferencesAdder(PreferenceFragment fragment) {
        this.fragment = fragment;
    }

    public boolean add() {
        try {
            fragment.addPreferencesFromResource(R.xml.aggregate_preferences);
            return true;
        } catch (ClassCastException e) {
            Toast.makeText(fragment.getActivity(), R.string.corrupt_imported_preferences_error, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
