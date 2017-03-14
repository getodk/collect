/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

/**
 * An example of tab content that launches an activity via
 * {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
 */
public class FileManagerTabs extends TabActivity {

    private static final String FORMS_TAB = "forms_tab";
    private static final String DATA_TAB = "data_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.manage_files));

        final TabHost tabHost = getTabHost();
        tabHost.getTabWidget().setBackgroundColor(Color.DKGRAY);

        Intent remote = new Intent(this, DataManagerList.class);
        tabHost.addTab(tabHost
                .newTabSpec(DATA_TAB)
                .setIndicator(getString(R.string.data))
                .setContent(remote));

        Intent local = new Intent(this, FormManagerList.class);
        tabHost.addTab(tabHost
                .newTabSpec(FORMS_TAB)
                .setIndicator(getString(R.string.forms))
                .setContent(local));

        // hack to set font size
        LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
        TabWidget tw = (TabWidget) ll.getChildAt(0);

        int fontsize = Collect.getQuestionFontsize();

        ViewGroup rllf = (ViewGroup) tw.getChildAt(0);
        TextView savedFormTab = getTextViewChild(rllf);
        if (savedFormTab != null) {
            savedFormTab.setTextSize(fontsize);
            savedFormTab.setTextColor(Color.WHITE);
            savedFormTab.setPadding(0, 0, 0, 6);
            savedFormTab.setSingleLine();
        }

        ViewGroup rlrf = (ViewGroup) tw.getChildAt(1);
        TextView blankFormTab = getTextViewChild(rlrf);
        if (blankFormTab != null) {
            blankFormTab.setTextSize(fontsize);
            blankFormTab.setTextColor(Color.WHITE);
            blankFormTab.setPadding(0, 0, 0, 6);
            blankFormTab.setSingleLine();
        }
    }

    private TextView getTextViewChild(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof TextView) {
                return (TextView) view;
            }
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
        Collect.getInstance().getActivityLogger().logOnStop(this);
        super.onStop();
    }

}
