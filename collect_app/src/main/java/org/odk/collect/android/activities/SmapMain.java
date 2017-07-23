/*
 * Copyright (C) 2017 Smap Consulting Pty Ltd
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.ViewPagerAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.FormManagerList;
import org.odk.collect.android.fragments.SmapTaskListFragment;
import org.odk.collect.android.fragments.SmapTaskMapFragment;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.listeners.NFCListener;
import org.odk.collect.android.listeners.TaskDownloaderListener;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.taskModel.NfcTrigger;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.AuthDialogUtility;
import org.odk.collect.android.utilities.ManageForm;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.Utilities;
import org.odk.collect.android.views.SlidingTabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class SmapMain extends AppCompatActivity implements TaskDownloaderListener,
        NFCListener,
        InstanceUploaderListener,
        FormDownloaderListener {

    private static final int PROGRESS_DIALOG = 1;
    private static final int ALERT_DIALOG = 2;
    private static final int PASSWORD_DIALOG = 3;
    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private String mAlertMsg;

    private SmapTaskListFragment taskManagerList = SmapTaskListFragment.newInstance();
    private SmapTaskMapFragment taskManagerMap = SmapTaskMapFragment.newInstance();

    public ArrayList<NfcTrigger> nfcTriggersList;   // nfcTriggers (geofence should have separate list)
    public ArrayList<NfcTrigger> nfcTriggersMap;    // nfcTriggers (geofence should have separate list)

    private static List<TaskEntry> mTasks = null;
    private static List<TaskEntry> mMapTasks = null;

    private String mProgressMsg;
    public DownloadTasksTask mDownloadTasks;

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(getString(R.string.app_name));
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.smap_main_layout);
        initToolbar();

        String[] tabNames = {getString(R.string.smap_taskList), getString(R.string.smap_taskMap)};
        // Get the ViewPager and set its PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(taskManagerList);
        fragments.add(taskManagerMap);

        viewPager.setAdapter(new ViewPagerAdapter(
                getSupportFragmentManager(), tabNames, fragments));

        // Give the SlidingTabLayout the ViewPager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setFontColor(android.R.color.white);
        slidingTabLayout.setBackgroundColor(Color.DKGRAY);
        slidingTabLayout.setViewPager(viewPager);

        File f = new File(Collect.ODK_ROOT + "/collect.settings");
        File j = new File(Collect.ODK_ROOT + "/collect.settings.json");
        // Give JSON file preference
        if (j.exists()) {
            SharedPreferencesUtils sharedPrefs = new SharedPreferencesUtils();
            boolean success = sharedPrefs.loadSharedPreferencesFromJSONFile(j);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                j.delete();

                // Delete settings file to prevent overwrite of settings from JSON file on next startup
                if (f.exists()) {
                    f.delete();
                }
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        } else if (f.exists()) {
            boolean success = loadSharedPreferencesFromFile(f);
            if (success) {
                ToastUtils.showLongToast(R.string.settings_successfully_loaded_file_notification);
                f.delete();
            } else {
                ToastUtils.showLongToast(R.string.corrupt_settings_file_notification);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
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

    @Override
    public void onBackPressed() {
        //if (taskManagerList.getDrawerStatus()) {
        //    taskManagerList.setUserVisibleHint(false);
        //} else {
        //    super.onBackPressed();
        //}
    }

    // Get tasks and forms from the server
    public void processGetTask() {

        mProgressMsg = getString(R.string.smap_synchronising);
        showDialog(PROGRESS_DIALOG);
        mDownloadTasks = new DownloadTasksTask();
        mDownloadTasks.setDownloaderListener(this, this);
        mDownloadTasks.execute();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mDownloadTasks.setDownloaderListener(null, SmapMain.this);
                                mDownloadTasks.cancel(true);
                                // Refresh the task list
                                Intent intent = new Intent("refresh");
                                SmapMain.this.sendBroadcast(intent);
                            }
                        };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mProgressMsg);
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case ALERT_DIALOG:
                mAlertDialog = new AlertDialog.Builder(this).create();
                mAlertDialog.setMessage(mAlertMsg);
                mAlertDialog.setTitle(getString(R.string.smap_get_tasks));
                DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                };
                mAlertDialog.setCancelable(false);
                mAlertDialog.setButton(getString(R.string.ok), quitListener);
                mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
                return mAlertDialog;
            case PASSWORD_DIALOG:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog passwordDialog = builder.create();
                final SharedPreferences adminPreferences = this.getSharedPreferences(
                        AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

                passwordDialog.setTitle(getString(R.string.enter_admin_password));
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
                passwordDialog.setView(input, 20, 10, 20, 10);

                passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                        getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                String value = input.getText().toString();
                                String pw = adminPreferences.getString(
                                        AdminKeys.KEY_ADMIN_PW, "");
                                if (pw.compareTo(value) == 0) {
                                    Intent i = new Intent(getApplicationContext(),
                                            AdminPreferencesActivity.class);
                                    startActivity(i);
                                    input.setText("");
                                    passwordDialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            SmapMain.this,
                                            getString(R.string.admin_password_incorrect),
                                            Toast.LENGTH_SHORT).show();
                                    Collect.getInstance()
                                            .getActivityLogger()
                                            .logAction(this, "adminPasswordDialog",
                                                    "PASSWORD_INCORRECT");
                                }
                            }
                        });

                passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Collect.getInstance()
                                        .getActivityLogger()
                                        .logAction(this, "adminPasswordDialog",
                                                "cancel");
                                input.setText("");
                                return;
                            }
                        });

                passwordDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return passwordDialog;
        }
        return null;
    }

    /*
     * Forms Downloading Overrides
     */
    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        // TODO Auto-generated method stub
        // Ignore formsDownloading is called synchronously from taskDownloader
    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
        // TODO Auto-generated method stub
        mProgressMsg = getString(R.string.fetching_file, currentFile, String.valueOf(progress), String.valueOf(total));
        mProgressDialog.setMessage(mProgressMsg);
    }

    /*
     * Task Download overrides
     */
    @Override
    // Download tasks progress update
    public void progressUpdate(String progress) {
        mProgressMsg = progress;
        mProgressDialog.setMessage(mProgressMsg);
    }

    public void taskDownloadingComplete(HashMap<String, String> result) {

        Timber.i("Complete - Send intent");

        // Refresh task list
        Intent intent = new Intent("refresh");
        getApplication().sendBroadcast(intent);

        try {
            dismissDialog(PROGRESS_DIALOG);
            removeDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }
        try {
            dismissDialog(ALERT_DIALOG);
            removeDialog(ALERT_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

        if (result != null) {
            StringBuilder message = new StringBuilder();
            Set<String> keys = result.keySet();
            Iterator<String> it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                if (key.equals("err_not_enabled")) {
                    message.append(this.getString(R.string.smap_tasks_not_enabled));
                } else if (key.equals("err_no_tasks")) {
                    // No tasks is fine, in fact its the most common state
                    //message.append(this.getString(R.string.smap_no_tasks));
                } else {
                    message.append(key + " - " + result.get(key) + "\n\n");
                }
            }

            mAlertMsg = message.toString().trim();
            if (mAlertMsg.length() > 0) {
                showDialog(ALERT_DIALOG);
            }

        }
    }

    /*
     * Uploading overrides
     */
    @Override
    public void uploadingComplete(HashMap<String, String> result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void progressUpdate(int progress, int total) {
        mAlertMsg = getString(R.string.sending_items, String.valueOf(progress), String.valueOf(total));
        mProgressDialog.setMessage(mAlertMsg);
    }

    @Override
    public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
        // TODO Auto-generated method stub

    }

    /*
     * NFC Reading Overrides
     */

    @Override
    public void readComplete(String result) {

        boolean foundTask = false;
        ArrayList<NfcTrigger> triggers = null;
        //String tab = tabHost.getCurrentTabTag();      // smap TODO determine which tab is showing
        String tab = "taskList";                        // smap TODO

        boolean isMapTab = tab.equals("taskMap");
        if (isMapTab) {
            triggers = nfcTriggersMap;
        } else {
            triggers = nfcTriggersList;
        }


        if (triggers != null) {
            for (NfcTrigger trigger : triggers) {
                if (trigger.uid.equals(result)) {
                    foundTask = true;

                    Intent i = new Intent();
                    if (isMapTab) {
                        i.setAction("startMapTask");
                    } else {
                        i.setAction("startTask");
                    }
                    i.putExtra("position", trigger.position);
                    sendBroadcast(i);

                    Toast.makeText(
                            SmapMain.this,
                            getString(R.string.smap_starting_task_from_nfc, result),
                            Toast.LENGTH_SHORT).show();

                    break;
                }
            }
        }
        if (!foundTask) {
            Toast.makeText(
                    SmapMain.this,
                    getString(R.string.smap_no_matching_tasks_nfc, result),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Copied from Collect Main Menu Activity
     */
    private boolean loadSharedPreferencesFromFile(File src) {
        // this should probably be in a thread if it ever gets big
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(
                    this).edit();
            prefEdit.clear();
            // first object is preferences
            Map<String, ?> entries = (Map<String, ?>) input.readObject();

            AutoSendPreferenceMigrator.migrate(entries);

            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean) {
                    prefEdit.putBoolean(key, (Boolean) v);
                } else if (v instanceof Float) {
                    prefEdit.putFloat(key, (Float) v);
                } else if (v instanceof Integer) {
                    prefEdit.putInt(key, (Integer) v);
                } else if (v instanceof Long) {
                    prefEdit.putLong(key, (Long) v);
                } else if (v instanceof String) {
                    prefEdit.putString(key, ((String) v));
                }
            }
            prefEdit.apply();
            AuthDialogUtility.setWebCredentialsFromPreferences(this);

            // second object is admin options
            SharedPreferences.Editor adminEdit = getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES,
                    0).edit();
            adminEdit.clear();
            // first object is preferences
            Map<String, ?> adminEntries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : adminEntries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean) {
                    adminEdit.putBoolean(key, (Boolean) v);
                } else if (v instanceof Float) {
                    adminEdit.putFloat(key, (Float) v);
                } else if (v instanceof Integer) {
                    adminEdit.putInt(key, (Integer) v);
                } else if (v instanceof Long) {
                    adminEdit.putLong(key, (Long) v);
                } else if (v instanceof String) {
                    adminEdit.putString(key, ((String) v));
                }
            }
            adminEdit.apply();

            res = true;
        } catch (IOException | ClassNotFoundException e) {
            Timber.e(e, "Exception while loading preferences from file due to : %s ", e.getMessage());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }
        return res;
    }

    /*
     * The user has selected an option to edit / complete a task
     */
    public void completeTask(TaskEntry entry) {

        String surveyNotes = null;
        String formPath = Collect.FORMS_PATH + entry.taskForm;
        String instancePath = entry.instancePath;
        long taskId = entry.id;
        String status = entry.taskStatus;

        Timber.i("Complete task" + entry.id + " : " + entry.name + " : " + entry.taskStatus);

        if (entry.repeat) {
            entry.instancePath = duplicateInstance(formPath, entry.instancePath, entry);
        }

        // set the adhoc location
        boolean canUpdate = false;
        try {
            canUpdate = Utilities.canComplete(status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean reviewFinal = sharedPreferences.getBoolean(PreferenceKeys.KEY_STORE_REVIEW_FINAL, true);

        if (!canUpdate && reviewFinal) {
            // Show a message if this task is read only
            Toast.makeText(
                    SmapMain.this,
                    getString(R.string.read_only),
                    Toast.LENGTH_LONG).show();
        } else if (!canUpdate && !reviewFinal) {
            // Show a message if this task is read only and cannot be reviewed
            Toast.makeText(
                    SmapMain.this,
                    getString(R.string.no_review),
                    Toast.LENGTH_LONG).show();
        }

        // Open the task if it is editable or reviewable
        if (canUpdate || reviewFinal) {
            // Get the provider URI of the instance
            String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
            String[] whereArgs = {
                    instancePath
            };

            Cursor cInstanceProvider = Collect.getInstance().getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                    null, where, whereArgs, null);

            if (cInstanceProvider.getCount() != 1) {
                Timber.e("Unique instance not found: count is:" +
                        cInstanceProvider.getCount());
            } else {
                cInstanceProvider.moveToFirst();
                Uri instanceUri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                        cInstanceProvider.getLong(
                                cInstanceProvider.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
                surveyNotes = cInstanceProvider.getString(
                        cInstanceProvider.getColumnIndex(InstanceProviderAPI.InstanceColumns.T_SURVEY_NOTES));
                // Start activity to complete form

                // Use an explicit intent
                Intent i = new Intent(this, org.odk.collect.android.activities.FormEntryActivity.class);
                i.setData(instanceUri);

                //Intent i = new Intent(Intent.ACTION_EDIT, instanceUri);

                //i.putExtra(FormEntryActivity.KEY_FORMPATH, formPath);    // TODO Don't think this is needed
                i.putExtra(FormEntryActivity.KEY_TASK, taskId);
                i.putExtra(FormEntryActivity.KEY_SURVEY_NOTES, surveyNotes);
                i.putExtra(FormEntryActivity.KEY_CAN_UPDATE, canUpdate);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE, ApplicationConstants.FormModes.EDIT_SAVED);
                //if (instancePath != null) {    // TODO Don't think this is needed
                //    i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, instancePath);
                //}
                startActivity(i);
            }
            cInstanceProvider.close();
        }

    }


    /*
     * Duplicate the instance
     * Call this if the instance repeats
     */
    public String duplicateInstance(String formPath, String originalPath, TaskEntry entry) {
        String newPath = null;

        // 1. Get a new instance path
        ManageForm mf = new ManageForm();
        newPath = mf.getInstancePath(formPath, 0);

        // 2. Duplicate the instance entry and get the new path
        Utilities.duplicateTask(originalPath, newPath, entry);

        // 3. Copy the instance files
        Utilities.copyInstanceFiles(originalPath, newPath);
        return newPath;
    }

    /*
 * Get the tasks shown on the map
 */
    public List<TaskEntry> getMapTasks() {
        return mMapTasks;
    }
}
