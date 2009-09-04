/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.odk.collect.android.R;
import org.odk.collect.android.db.FileDbAdapter;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.prefs.GlobalPreferences;
import org.odk.collect.android.utils.FileUtils;

import java.util.ArrayList;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenu extends Activity {

    // request codes for returning chosen form to main menu.
    private static final int FORM_CHOOSER = 0;
    private static final int INSTANCE_CHOOSER_TABS = 1;
    private static final int INSTANCE_UPLOADER = 2;

    // menu options
    public static final int MENU_PREFERENCES = Menu.FIRST;

    // buttons
    private Button mEnterDataButton;
    private Button mManageFormsButton;
    private Button mSendDataButton;
    private Button mReviewDataButton;

    // counts for buttons
    private static int mSavedCount;
    private static int mCompletedCount;
    private static int mAvailableCount;
    private static int mFormsCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }


    /**
     * Create View with buttons to launch activities.
     */
    private void buildView() {

        setContentView(R.layout.main_menu);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormChooser.class);
                startActivityForResult(i, FORM_CHOOSER);
            }
        });

        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserTabs.class);
                startActivityForResult(i, INSTANCE_CHOOSER_TABS);
            }
        });

        // send data button. expects a result.
        mSendDataButton = (Button) findViewById(R.id.send_data);
        mSendDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceUploader.class);
                startActivityForResult(i, INSTANCE_UPLOADER);
            }

        });

        // manage forms button. no result expected.
        mManageFormsButton = (Button) findViewById(R.id.manage_forms);
        mManageFormsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormManagerTabs.class);
                startActivity(i);
            }
        });


        // gets count and updates buttons.
        refreshView();

    }


    /**
     * Upon return, check intent for data needed to launch other activities.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == RESULT_CANCELED) {
            return;
        }

        String formPath = null;
        Intent i = null;
        switch (requestCode) {
            // returns with a form path, start entry
            case FORM_CHOOSER:
                formPath = intent.getStringExtra(GlobalConstants.KEY_FORMPATH);
                i = new Intent(this, FormEntry.class);
                i.putExtra(GlobalConstants.KEY_FORMPATH, formPath);
                startActivity(i);
                break;
            // returns with an instance path, start entry
            case INSTANCE_CHOOSER_TABS:
                formPath = intent.getStringExtra(GlobalConstants.KEY_FORMPATH);
                String instancePath = intent.getStringExtra(GlobalConstants.KEY_INSTANCEPATH);
                i = new Intent(this, FormEntry.class);
                i.putExtra(GlobalConstants.KEY_FORMPATH, formPath);
                i.putExtra(GlobalConstants.KEY_INSTANCEPATH, instancePath);
                startActivity(i);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private void refreshView() {
        updateButtonCount();
    }



    /**
     * Updates the button count and sets the text in the buttons.
     */
    private void updateButtonCount() {

        // create adapter
        FileDbAdapter fda = new FileDbAdapter(this);
        fda.open();

        // count for saved instances
        Cursor c = fda.fetchFiles(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_SAVED);
        mSavedCount = c.getCount();
        c.close();

        // count for completed instances
        c = fda.fetchFiles(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_COMPLETED);
        mCompletedCount = c.getCount();
        c.close();

        // count for downloaded forms
        ArrayList<String> forms = FileUtils.getFilesAsArrayList(GlobalConstants.FORMS_PATH);
        if (forms != null) {
            mFormsCount = forms.size();
        } else {
            mFormsCount = 0;
        }

        // count for available forms
        c = fda.fetchFiles(FileDbAdapter.TYPE_FORM, FileDbAdapter.STATUS_AVAILABLE);
        mAvailableCount = c.getCount();
        c.close();
        fda.close();

        // update button text
        if(mAvailableCount == mFormsCount) {
            mEnterDataButton.setText(getString(R.string.enter_data_button, mAvailableCount));
        } else {
            mEnterDataButton.setText(getString(R.string.enter_data));
        }

        mManageFormsButton.setText(getString(R.string.manage_forms));
        mSendDataButton.setText(getString(R.string.send_data_button, mCompletedCount));
        mReviewDataButton.setText(getString(R.string.review_data_button, mSavedCount
                + mCompletedCount));
    }


    private void createPreferencesMenu() {
        Intent i = new Intent(this, GlobalPreferences.class);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.preferences)).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                createPreferencesMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
