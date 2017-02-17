/*
 * Copyright 2017 Nafundi
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

package org.odk.collect.android.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.OpenSourceLicensesActivity;

public class AboutPreferencesActivity extends PreferenceActivity {

    public static final String KEY_OPEN_SOURCE_LICENSES = "open_source_licenses";
    public static final String KEY_TELL_YOUR_FRIENDS = "tell_your_friends";
    private static final String GOOGLE_PLAY_LINK =
            "https://play.google.com/store/apps/details?id=org.odk.collect.android";

    private PreferenceScreen mOpenSourceLicensesPreference;
    private PreferenceScreen mTellYourFriendsPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_preferences);
        setTitle(getString(R.string.about_preferences));

        mOpenSourceLicensesPreference = (PreferenceScreen) findPreference(KEY_OPEN_SOURCE_LICENSES);
        mTellYourFriendsPreference = (PreferenceScreen) findPreference(KEY_TELL_YOUR_FRIENDS);

        mOpenSourceLicensesPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getApplicationContext(),
                                OpenSourceLicensesActivity.class));
                        return true;
                    }
                });

        mTellYourFriendsPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                                getString(R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_LINK);
                        startActivity(Intent.createChooser(shareIntent,
                                getString(R.string.tell_your_friends_title)));
                        return true;
                    }
                });
    }
}
