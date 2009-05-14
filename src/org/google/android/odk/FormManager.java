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

package org.google.android.odk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Responsible for displaying, adding and deleting all the valid forms in the
 * forms directory.
 * 
 * @author Carl Hartung
 */
public class FormManager extends ListActivity {

    private final String t = "FormManager";

    private static final int DIALOG_ADD_FORM = 0;
    private static final int DIALOG_DELETE_FORM = 1;

    // add or delete form
    private static final int MENU_ADD = Menu.FIRST;
    private static final int MENU_DELETE = Menu.FIRST + 1;

    private List<String> mFormNames;
    private File mFormsDirectory;
    private String mFormsPath;
    private String mDeleteForm;
    private AlertDialog mAlertDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(t,"called onCreate");

        setContentView(R.layout.formmanager);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.manage_forms));

        mFormNames = new ArrayList<String>();
        mFormsPath = SharedConstants.FORMS_PATH;

        TextView tv = (TextView) findViewById(R.id.formmanager_message);

        // First, check to see if there's an SD card.
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            tv.setText(getString(R.string.sdcard_error));
            return;
        }

        // Check to see if our storage directory exists and create it if not.
        boolean made = true;
        mFormsDirectory = new File(mFormsPath);
        if (!mFormsDirectory.exists()) {
            made = mFormsDirectory.mkdirs();
        }
        if (!made) {
            tv.setText(getString(R.string.directory_error, mFormsPath));
            return;
        }

        displayForms();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD, 0, getString(R.string.add_form)).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_DELETE, 0, getString(R.string.delete_form)).setIcon(android.R.drawable.ic_menu_delete);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                showDialog(DIALOG_ADD_FORM);
                return true;
            case MENU_DELETE:
                if (getListView().getCheckedItemPosition() != -1) {
                    showDialog(DIALOG_DELETE_FORM);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.delete_error),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    /**
     * Opens forms directory, puts valid files in array adapter for display
     */

    private void displayForms() {
        mFormNames.clear();
        File[] files = mFormsDirectory.listFiles();

        for (File f : files) {
            String fileName =
                    f.getAbsolutePath().substring(mFormsDirectory.getAbsolutePath().length() + 1);
            if (fileName.matches(SharedConstants.VALID_FORMNAME)) {
                mFormNames.add(fileName);
            }
        }

        Collections.sort(mFormNames, String.CASE_INSENSITIVE_ORDER);

        // This is needed for simple list single choice adapter.
        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<String> directoryList =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice,
                        mFormNames);
        setListAdapter(directoryList);
    }


    /**
     * Calls either the add form or delete form dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ADD_FORM:
                createAddDialog();
                break;
            case DIALOG_DELETE_FORM:
                createDeleteDialog();
                break;
        }
        return mAlertDialog;
    }


    /**
     * Create the form add dialog
     */
    private void createAddDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        final View v = li.inflate(R.layout.add_form, null);
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(getString(R.string.add_form));
        mAlertDialog.setView(v);
        DialogInterface.OnClickListener DialogUrl = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case AlertDialog.BUTTON1: // ok, download form
                        EditText et = (EditText) v.findViewById(R.id.add_url);
                        try {
                            URL u = new URL(et.getText().toString());
                            getFormFromUrl(u);
                        } catch (MalformedURLException e) {
                            Toast.makeText(getBaseContext(), getString(R.string.url_error),
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        displayForms();
                        break;
                    case AlertDialog.BUTTON2: // cancel, do nothing
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), DialogUrl);
        mAlertDialog.setButton2(getString(R.string.cancel), DialogUrl);
    }


    /**
     * Create the form delete dialog
     */
    private void createDeleteDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mDeleteForm = mFormNames.get(getListView().getCheckedItemPosition());
        mAlertDialog.setMessage(getString(R.string.delete_confirm, mDeleteForm));
        DialogInterface.OnClickListener dialogYesNoListener =
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        switch (i) {
                            case AlertDialog.BUTTON1: // yes, delete
                                deleteSelectedForm();
                                displayForms();
                                break;
                            case AlertDialog.BUTTON2: // no, do nothing
                                break;
                        }
                    }
                };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.yes), dialogYesNoListener);
        mAlertDialog.setButton2(getString(R.string.no), dialogYesNoListener);
    }


    /**
     * Deletes the selected form
     */
    private void deleteSelectedForm() {
        File f = new File(mFormsPath + "/" + mDeleteForm);
        if (f.delete()) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.form_deleted_ok, mDeleteForm), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.form_deleted_error, mDeleteForm), Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Downloads a valid form from a url to forms directory
     */
    private void getFormFromUrl(URL u) {
        try {
            
            // prevent deadlock when connection is invalid
            URLConnection c = u.openConnection();
            c.setConnectTimeout(SharedConstants.CONNECTION_TIMEOUT);
            c.setReadTimeout(SharedConstants.CONNECTION_TIMEOUT);
            
            InputStream is = c.getInputStream();

            String filename = u.getFile();
            filename = filename.substring(filename.lastIndexOf('/') + 1);

            if (filename.matches(SharedConstants.VALID_FORMNAME)) {
                File f = new File(mFormsPath + "/" + filename);
                OutputStream os = new FileOutputStream(f);
                byte buf[] = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0)
                    os.write(buf, 0, len);
                os.flush();
                os.close();
                is.close();
                Toast.makeText(this, getString(R.string.form_added_ok, filename),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.url_error), Toast.LENGTH_SHORT).show();
            }
            mAlertDialog.dismiss();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.url_error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } 

    }

    /**
     * Managed dialogs require setting your variables beforehand
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case DIALOG_DELETE_FORM:
                mDeleteForm = mFormNames.get(getListView().getCheckedItemPosition());
                ((AlertDialog) dialog).setMessage(getString(R.string.delete_confirm, mDeleteForm));
                break;
        }
    }
}
