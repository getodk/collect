/*
 * Copyright 2017 Marcos Lopez Gonzalez (asturcon1234@gmail.com)
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

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.OpenSourceLicensesActivity;
import org.odk.collect.android.utilities.CustomTabHelper;

import java.util.List;

import timber.log.Timber;

public class AboutPreferencesActivity extends PreferenceActivity {

    private static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private static final String ODK_WEBSITE = "https://opendatakit.org";
    private static final String ODK_FORUM = "https://forum.opendatakit.org";
    private CustomTabHelper websiteTabHelper;
    private CustomTabHelper forumTabHelper;
    private Uri websiteUri;
    private Uri forumUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup root = getRootView();
        Toolbar toolbar = (Toolbar) View.inflate(this, R.layout.toolbar, null);
        toolbar.setTitle(R.string.about_preferences);
        View shadow = View.inflate(this, R.layout.toolbar_action_bar_shadow, null);

        root.addView(toolbar, 0);
        root.addView(shadow, 1);

        websiteTabHelper = new CustomTabHelper();
        forumTabHelper = new CustomTabHelper();

        websiteUri = Uri.parse(ODK_WEBSITE);
        forumUri = Uri.parse(ODK_FORUM);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.about_preference_headers, target);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        switch ((int) header.id) {
            case R.id.odk_website:
                websiteTabHelper.openUri(this, websiteUri);
                break;
            case R.id.odk_forum:
                forumTabHelper.openUri(this, forumUri);
                break;
            case R.id.open_source_licenses:
                startActivity(new Intent(this, OpenSourceLicensesActivity.class));
                break;
            case R.id.tell_your_friends:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_URL
                                + getPackageName());
                startActivity(Intent.createChooser(shareIntent,
                        getString(R.string.tell_your_friends)));
                break;
            case R.id.leave_a_review:
                boolean reviewTaken = false;
                try {
                    // Open the google play store app if present
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getPackageName()));
                    PackageManager packageManager = getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
                    for (ResolveInfo info : list) {
                        ActivityInfo activity = info.activityInfo;
                        if (activity.name.contains("com.google.android")) {
                            ComponentName name = new ComponentName(
                                    activity.applicationInfo.packageName,
                                    activity.name);
                            intent.setComponent(name);
                            startActivity(intent);
                            reviewTaken = true;
                        }
                    }
                } catch (android.content.ActivityNotFoundException anfe) {
                    Timber.e(anfe);
                }
                if (!reviewTaken) {
                    // Show a list of all available browsers if user doesn't have a default browser
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(GOOGLE_PLAY_URL + getPackageName()));
                    startActivity(intent);
                }
                break;
        }

        super.onHeaderClick(header, position);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    private ViewGroup getRootView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
        } else {
            return (ViewGroup) findViewById(android.R.id.list).getParent();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        websiteTabHelper.bindCustomTabsService(this, websiteUri);
        forumTabHelper.bindCustomTabsService(this, forumUri);
    }

    @Override
    public void onDestroy() {
        unbindService(websiteTabHelper.getServiceConnection());
        unbindService(forumTabHelper.getServiceConnection());
        super.onDestroy();
    }
}