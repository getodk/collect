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

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;

import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

/**
 * A host activity for {@link InstanceChooserList}.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class InstanceChooserTabs extends TabActivity {

    // count for tab menu bar
    private static int mSavedCount;
    private static int mCompletedCount;
    private static final String SAVED_TAB = "saved_tab";
    private static final String COMPLETED_TAB = "completed_tab";
    private static final int FONT_SIZE = 21;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.review_data));
        refreshView();
    }


    /**
     * Build tab host view and setup tab intents.
     */
    private void refreshView() {
        // update tab host count
        updateTabHostCount();

        // create tab host and tweak color
        final TabHost tabHost = getTabHost();
        tabHost.setBackgroundColor(Color.WHITE);
        tabHost.getTabWidget().setBackgroundColor(Color.BLACK);

        // create intent for saved tab
        Intent saved = new Intent(this, InstanceChooserList.class);
        saved.putExtra(FileDbAdapter.KEY_STATUS, FileDbAdapter.STATUS_INCOMPLETE);
        tabHost.addTab(tabHost.newTabSpec(SAVED_TAB)
                .setIndicator(getString(R.string.saved_data, mSavedCount)).setContent(saved));

        // create intent for completed tab
        Intent completed = new Intent(this, InstanceChooserList.class);
        completed.putExtra(FileDbAdapter.KEY_STATUS, FileDbAdapter.STATUS_COMPLETE);
        tabHost.addTab(tabHost.newTabSpec(COMPLETED_TAB)
                .setIndicator(getString(R.string.completed_data, mCompletedCount))
                .setContent(completed));

        // hack to set font size and padding in tab headers
        // arrived at these paths by using hierarchy viewer
        LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
        TabWidget tw = (TabWidget) ll.getChildAt(0);

        RelativeLayout rls = (RelativeLayout) tw.getChildAt(0);
        TextView tvs = (TextView) rls.getChildAt(1);
        tvs.setTextSize(FONT_SIZE);
        tvs.setPadding(0, 0, 0, 6);

        RelativeLayout rlc = (RelativeLayout) tw.getChildAt(1);
        TextView tvc = (TextView) rlc.getChildAt(1);
        tvc.setTextSize(FONT_SIZE);
        tvc.setPadding(0, 0, 0, 6);

        if (mSavedCount >= mCompletedCount) {
            getTabHost().setCurrentTabByTag(SAVED_TAB);
        } else {
            getTabHost().setCurrentTabByTag(COMPLETED_TAB);
        }
    }


    /**
     * Update count of saved and completed instances for tab host header.
     */
    private void updateTabHostCount() {
        // create file adapter
        FileDbAdapter fda = new FileDbAdapter();
        fda.open();

        // get saved instances
        Cursor c =
            fda.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_INCOMPLETE);
        mSavedCount = c.getCount();
        c.close();

        // get completed instances
        c = fda.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_COMPLETE);
        mCompletedCount = c.getCount();
        c.close();

        // memory cleanup
        fda.close();
    }

}
