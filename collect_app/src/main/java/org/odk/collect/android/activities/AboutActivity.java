/*
 * Copyright 2018 Shobhit Agarwal
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

package org.odk.collect.android.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AboutListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ExternalWebPageHelper;
import org.odk.collect.android.utilities.MultiClickGuard;

import java.util.List;

import timber.log.Timber;

public class AboutActivity extends CollectAbstractActivity implements
        AboutListAdapter.AboutItemClickListener {

    private static final String LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html";
    private static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=";

    private ExternalWebPageHelper websiteTabHelper;
    private ExternalWebPageHelper forumTabHelper;
    private Uri websiteUri;
    private Uri forumUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        initToolbar();

        int[][] items = {
                {R.drawable.ic_website, R.string.odk_website, R.string.odk_website_summary},
                {R.drawable.ic_forum, R.string.odk_forum, R.string.odk_forum_summary},
                {R.drawable.ic_share, R.string.tell_your_friends, R.string.tell_your_friends_msg},
                {R.drawable.ic_review_rate, R.string.leave_a_review, R.string.leave_a_review_msg},
                {R.drawable.ic_stars, R.string.all_open_source_licenses, R.string.all_open_source_licenses_msg}
        };

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new AboutListAdapter(items, this, this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        websiteTabHelper = new ExternalWebPageHelper();
        forumTabHelper = new ExternalWebPageHelper();

        websiteUri = Uri.parse(getString(R.string.app_url));
        forumUri = Uri.parse(getString(R.string.forum_url));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.about_preferences));
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(int position) {
        if (MultiClickGuard.allowClick(getClass().getName())) {
            switch (position) {
                case 0:
                    websiteTabHelper.openWebPageInCustomTab(this, websiteUri);
                    break;
                case 1:
                    forumTabHelper.openWebPageInCustomTab(this, forumUri);
                    break;
                case 2:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            getString(R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_URL
                                    + getPackageName());
                    startActivity(Intent.createChooser(shareIntent,
                            getString(R.string.tell_your_friends)));
                    break;
                case 3:
                    boolean intentStarted = false;
                    try {
                        // Open the google play store app if present
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + getPackageName()));
                        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                        for (ResolveInfo info : list) {
                            ActivityInfo activity = info.activityInfo;
                            if (activity.name.contains("com.google.android")) {
                                ComponentName name = new ComponentName(
                                        activity.applicationInfo.packageName,
                                        activity.name);
                                intent.setComponent(name);
                                startActivity(intent);
                                intentStarted = true;
                            }
                        }
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Toast.makeText(Collect.getInstance(),
                                getString(R.string.activity_not_found, "market view"),
                                Toast.LENGTH_SHORT).show();
                        Timber.d(anfe);
                    }
                    if (!intentStarted) {
                        // Show a list of all available browsers if user doesn't have a default browser
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(GOOGLE_PLAY_URL + getPackageName())));
                    }
                    break;
                case 4:
                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra(ExternalWebPageHelper.OPEN_URL, LICENSES_HTML_PATH);
                    startActivity(intent);
                    break;
            }
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
