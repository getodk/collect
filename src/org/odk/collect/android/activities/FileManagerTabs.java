package org.odk.collect.android.activities;

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

import org.odk.collect.android.R;
import org.odk.collect.android.logic.GlobalConstants;

/**
 * An example of tab content that launches an activity via
 * {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
 */
public class FileManagerTabs extends TabActivity {

    private static TextView tvlf;
    private static TextView tvrf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name) + " > " + getString(R.string.manage_files));

        final TabHost tabHost = getTabHost();
        tabHost.setBackgroundColor(Color.BLACK);

        Intent local = new Intent(this, LocalFileManagerList.class);
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(getString(R.string.local_files))
                .setContent(local));

        Intent remote = new Intent(this, RemoteFileManagerList.class);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(getString(R.string.remote_files))
                .setContent(remote));

        // hack to set font size
        LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
        TabWidget tw = (TabWidget) ll.getChildAt(0);

        RelativeLayout rllf = (RelativeLayout) tw.getChildAt(0);
        tvlf = (TextView) rllf.getChildAt(1);
        tvlf.setTextSize(GlobalConstants.APPLICATION_FONTSIZE + 12);
        tvlf.setPadding(0, 0, 0, 6);
        
        RelativeLayout rlrf = (RelativeLayout) tw.getChildAt(1);
        tvrf = (TextView) rlrf.getChildAt(1);
        tvrf.setTextSize(GlobalConstants.APPLICATION_FONTSIZE + 12);
        tvrf.setPadding(0, 0, 0, 6);

    }


    public static void setTabHeader(String string, String tab) {
        if (tab.equals("tab1")) {
            tvlf.setText(string);
        } else if (tab.equals("tab2")) {
            tvrf.setText(string);
        }

    }



}
