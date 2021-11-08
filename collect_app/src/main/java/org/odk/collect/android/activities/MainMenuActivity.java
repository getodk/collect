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

import static org.odk.collect.androidshared.ui.DialogFragmentUtils.showIfNotShowing;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.gdrive.GoogleDriveActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.ProjectIconView;
import org.odk.collect.android.projects.ProjectSettingsDialog;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.android.utilities.ThemeUtils;

import javax.inject.Inject;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends CollectAbstractActivity {
    // buttons
    private Button manageFilesButton;
    private Button sendDataButton;
    private Button viewSentFormsButton;
    private Button reviewDataButton;
    private Button getFormsButton;

    @Inject
    MainMenuViewModel.Factory viewModelFactory;

    @Inject
    CurrentProjectViewModel.Factory currentProjectViewModelFactory;

    @Inject
    SettingsProvider settingsProvider;

    private MainMenuViewModel mainMenuViewModel;

    private CurrentProjectViewModel currentProjectViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeUtils(this).setDarkModeForCurrentProject();

        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.main_menu);

        settingsProvider.getMetaSettings().save(MetaKeys.FIRST_LAUNCH, false);

        mainMenuViewModel = new ViewModelProvider(this, viewModelFactory).get(MainMenuViewModel.class);
        currentProjectViewModel = new ViewModelProvider(this, currentProjectViewModelFactory).get(CurrentProjectViewModel.class);
        currentProjectViewModel.getCurrentProject().observe(this, project -> {
            invalidateOptionsMenu();
            setTitle(project.getName());
        });

        initToolbar();

        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setText(getString(R.string.enter_data_button));
        enterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),
                        FillBlankFormActivity.class);
                startActivity(i);
            }
        });

        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        reviewDataButton.setText(getString(R.string.review_data_button));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.EDIT_SAVED);
                startActivity(i);
            }
        });

        // send data button. expects a result.
        sendDataButton = findViewById(R.id.send_data);
        sendDataButton.setText(getString(R.string.send_data_button));
        sendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),
                        InstanceUploaderListActivity.class);
                startActivity(i);
            }
        });

        //View sent forms
        viewSentFormsButton = findViewById(R.id.view_sent_forms);
        viewSentFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.VIEW_SENT);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        getFormsButton = findViewById(R.id.get_forms);
        getFormsButton.setText(getString(R.string.get_forms));
        getFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String protocol = settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_PROTOCOL);
                Intent i;
                if (protocol.equalsIgnoreCase(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
                    if (new PlayServicesChecker().isGooglePlayServicesAvailable(MainMenuActivity.this)) {
                        i = new Intent(getApplicationContext(),
                                GoogleDriveActivity.class);
                    } else {
                        new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
                        return;
                    }
                } else {
                    i = new Intent(getApplicationContext(),
                            FormDownloadListActivity.class);
                }
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        manageFilesButton = findViewById(R.id.manage_forms);
        manageFilesButton.setText(getString(R.string.manage_files));
        manageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),
                        DeleteSavedFormActivity.class);
                startActivity(i);
            }
        });

        TextView appName = findViewById(R.id.app_name);
        appName.setText(String.format("%s %s", getString(R.string.collect_app_name), mainMenuViewModel.getVersion()));

        TextView versionSHAView = findViewById(R.id.version_sha);
        String versionSHA = mainMenuViewModel.getVersionCommitDescription();
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }

        mainMenuViewModel.getSendableInstancesCount().observe(this, finalized -> {
            if (finalized > 0) {
                sendDataButton.setText(getString(R.string.send_data_button, String.valueOf(finalized)));
            } else {
                sendDataButton.setText(getString(R.string.send_data));
            }
        });


        mainMenuViewModel.getEditableInstancesCount().observe(this, unsent -> {
            if (unsent > 0) {
                reviewDataButton.setText(getString(R.string.review_data_button, String.valueOf(unsent)));
            } else {
                reviewDataButton.setText(getString(R.string.review_data));
            }
        });


        mainMenuViewModel.getSentInstancesCount().observe(this, sent -> {
            if (sent > 0) {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms_button, String.valueOf(sent)));
            } else {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentProjectViewModel.refresh();
        mainMenuViewModel.refreshInstances();
        setButtonsVisibility();
    }

    private void setButtonsVisibility() {
        reviewDataButton.setVisibility(mainMenuViewModel.shouldEditSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        sendDataButton.setVisibility(mainMenuViewModel.shouldSendFinalizedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        viewSentFormsButton.setVisibility(mainMenuViewModel.shouldViewSentFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        getFormsButton.setVisibility(mainMenuViewModel.shouldGetBlankFormButtonBeVisible() ? View.VISIBLE : View.GONE);
        manageFilesButton.setVisibility(mainMenuViewModel.shouldDeleteSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem projectsMenuItem = menu.findItem(R.id.projects);

        ProjectIconView projectIconView = (ProjectIconView) projectsMenuItem.getActionView();
        projectIconView.setProject(currentProjectViewModel.getCurrentProject().getValue());
        projectIconView.setOnClickListener(v -> onOptionsItemSelected(projectsMenuItem));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        if (item.getItemId() == R.id.projects) {
            showIfNotShowing(ProjectSettingsDialog.class, getSupportFragmentManager());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
