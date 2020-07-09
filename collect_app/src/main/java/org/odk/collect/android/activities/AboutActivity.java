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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AboutListAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.CustomTabHelper;

import java.util.List;

import timber.log.Timber;

public class AboutActivity extends CollectAbstractActivity implements
        AboutListAdapter.AboutItemClickListener {

    private static final String LICENSES_HTML_PATH = "https://docs.getodk.org/getting-started/";
    private static final String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=";
    private static final String ODK_WEBSITE = "https://hedera.online";
    private static final String ODK_FORUM = "mailto:contact@hedera.online";

    private CustomTabHelper websiteTabHelper;
    private CustomTabHelper forumTabHelper;
    private CustomTabHelper licensuriTabHelper;
    private Uri websiteUri;
    private Uri forumUri;
    private Uri licensuri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        initToolbar();

        int[][] items = {
                {R.drawable.ic_website, R.string.odk_website, R.string.odk_website_summary},
                {R.drawable.ic_forum, R.string.odk_forum, R.string.odk_forum_summary},

                {R.drawable.ic_stars, R.string.all_open_source_licenses, R.string.all_open_source_licenses_msg}
        };

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new AboutListAdapter(items, this, this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        websiteTabHelper = new CustomTabHelper();
        forumTabHelper = new CustomTabHelper();
        licensuriTabHelper = new CustomTabHelper();

        websiteUri = Uri.parse(ODK_WEBSITE);
        forumUri = Uri.parse(ODK_FORUM);
        licensuri = Uri.parse(LICENSES_HTML_PATH);
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
                    websiteTabHelper.openUri(this, websiteUri);
                    break;
                case 1:
                    forumTabHelper.openUri(this, forumUri);
                    break;
                case 2:
                    licensuriTabHelper.openUri(this, licensuri);
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
