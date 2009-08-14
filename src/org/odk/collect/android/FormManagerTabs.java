package org.odk.collect.android;

/*
 * Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * An example of tab content that launches an activity via
 * {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
 */
public class FormManagerTabs extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.manage_forms));

        final TabHost tabHost = getTabHost();
        tabHost.setBackgroundColor(Color.BLACK);

        Intent saved = new Intent(this, LocalFormManager.class);
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(getString(R.string.local_forms))
                .setContent(saved));

        Intent completed = new Intent(this, RemoteFormManager.class);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(getString(R.string.remote_forms)).setContent(completed));

        // hack to set font size
        LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
        TabWidget tw = (TabWidget) ll.getChildAt(0);

        RelativeLayout rls = (RelativeLayout) tw.getChildAt(0);
        TextView tvs = (TextView) rls.getChildAt(1);
        tvs.setTextSize(SharedConstants.APPLICATION_FONTSIZE + 12);
        tvs.setPadding(0, 0, 0, 6);

        RelativeLayout rlc = (RelativeLayout) tw.getChildAt(1);
        TextView tvc = (TextView) rlc.getChildAt(1);
        tvc.setTextSize(SharedConstants.APPLICATION_FONTSIZE + 12);
        tvc.setPadding(0, 0, 0, 6);

    }



}
