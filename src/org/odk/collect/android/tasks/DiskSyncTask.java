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

package org.odk.collect.android.tasks;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.FileUtils;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Background task for adding to the forms content provider, any forms that have been added to the
 * sdcard manually. Returns immediately if it detects an error.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DiskSyncTask extends AsyncTask<Void, String, String> {
    private final static String t = "DiskSyncTask";

    DiskSyncListener mListener;


    @Override
    protected String doInBackground(Void... params) {
        // get all forms
        Cursor mCursor =
            Collect.getInstance().getContentResolver()
                    .query(FormsColumns.CONTENT_URI, null, null, null, null);
        if (mCursor == null) {
            Log.e(t, "Forms Content Provider returned NULL");
            return null;
        }

        mCursor.moveToPosition(-1);

        File formDir = new File(Collect.FORMS_PATH);
        if (formDir.exists() && formDir.isDirectory()) {
            // This is all the files in the /odk/foms directory
            ArrayList<File> xFormsToAdd = new ArrayList<File>(Arrays.asList(formDir.listFiles()));

            while (mCursor.moveToNext()) {
                // For each element in the provider, see if the file already exists
                String sqlFilename =
                    mCursor.getString(mCursor.getColumnIndex(FormsColumns.FORM_FILE_PATH));
                String md5 = mCursor.getString(mCursor.getColumnIndex(FormsColumns.MD5_HASH));
                File sqlFile = new File(sqlFilename);
                if (sqlFile.exists()) {
                    // remove it from the list of forms (we only want forms we haven't added at the
                    // end)
                    xFormsToAdd.remove(sqlFile);
                    if (!FileUtils.getMd5Hash(sqlFile).contentEquals(md5)) {
                        // Probably someone overwrite the file on the sdcard
                        // So re-parse it and update it's information
                        String id = mCursor.getString(mCursor.getColumnIndex(FormsColumns._ID));
                        Uri updateUri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, id);
                        ContentValues updateValues = new ContentValues();

                        HashMap<String, String> fields = null;
                        try {
                            fields = FileUtils.parseXML(sqlFile);
                        } catch (RuntimeException e) {
                            return sqlFile.getName() + " :: " + e.getMessage();
                        }

                        String title = fields.get(FileUtils.TITLE);
                        String ui = fields.get(FileUtils.UI);
                        String model = fields.get(FileUtils.MODEL);
                        String formid = fields.get(FileUtils.FORMID);
                        String submission = fields.get(FileUtils.SUBMISSIONURI);

                        // update date
                        Long now = Long.valueOf(System.currentTimeMillis());
                        updateValues.put(FormsColumns.DATE, now);

                        if (title != null) {
                            updateValues.put(FormsColumns.DISPLAY_NAME, title);
                        } else {
                            return Collect.getInstance().getString(R.string.xform_parse_error,
                                sqlFile.getName(), "title");
                        }
                        if (formid != null) {
                            updateValues.put(FormsColumns.JR_FORM_ID, formid);
                        } else {
                            return Collect.getInstance().getString(R.string.xform_parse_error,
                                sqlFile.getName(), "id");
                        }
                        if (ui != null) {
                            updateValues.put(FormsColumns.UI_VERSION, ui);
                        }
                        if (model != null) {
                            updateValues.put(FormsColumns.MODEL_VERSION, model);
                        }
                        if (submission != null) {
                            updateValues.put(FormsColumns.SUBMISSION_URI, submission);
                        }
                        // Note, the path doesn't change here, but it needs to be included so the
                        // update will automatically update the .md5 and the cache path.
                        updateValues.put(FormsColumns.FORM_FILE_PATH, sqlFile.getAbsolutePath());
                        int count =
                            Collect.getInstance().getContentResolver()
                                    .update(updateUri, updateValues, null, null);
                        Log.i(t, count + " records successfully updated");
                    }
                } else {
                    Log.w(t, "file referenced by content provider does not exist " + sqlFile);
                }
            }

            // Whatever is left in our arraylist isn't in the database, so add it
            for (int i = 0; i < xFormsToAdd.size(); i++) {
                ContentValues values = new ContentValues();
                File addMe = xFormsToAdd.get(i);

                // Ignore invisible files that start with periods.
                if (!addMe.getName().startsWith(".")
                        && (addMe.getName().endsWith(".xml") || addMe.getName().endsWith(".xhtml"))) {

                    HashMap<String, String> fields = null;
                    try {
                        fields = FileUtils.parseXML(addMe);
                    } catch (RuntimeException e) {
                        return addMe.getName() + " :: " + e.getMessage();
                    }

                    String title = fields.get(FileUtils.TITLE);
                    String ui = fields.get(FileUtils.UI);
                    String model = fields.get(FileUtils.MODEL);
                    String formid = fields.get(FileUtils.FORMID);
                    String submission = fields.get(FileUtils.SUBMISSIONURI);

                    if (title != null) {
                        values.put(FormsColumns.DISPLAY_NAME, title);
                    } else {
                        return Collect.getInstance().getString(R.string.xform_parse_error,
                            addMe.getName(), "title");
                    }
                    if (formid != null) {
                        values.put(FormsColumns.JR_FORM_ID, formid);
                    } else {
                        return Collect.getInstance().getString(R.string.xform_parse_error,
                            addMe.getName(), "id");
                    }
                    if (ui != null) {
                        values.put(FormsColumns.UI_VERSION, ui);
                    }
                    if (model != null) {
                        values.put(FormsColumns.MODEL_VERSION, model);
                    }
                    if (submission != null) {
                        values.put(FormsColumns.SUBMISSION_URI, submission);
                    }

                    values.put(FormsColumns.FORM_FILE_PATH, addMe.getAbsolutePath());
                    Collect.getInstance().getContentResolver()
                            .insert(FormsColumns.CONTENT_URI, values);
                } else {
                    // it's a [formname]-media directory, likely, so skip it
                }
            }
        }
        if (mCursor != null) {
            mCursor.close();
        }
        return Collect.getInstance().getString(R.string.finished_disk_scan);

    }


    public void setDiskSyncListener(DiskSyncListener l) {
        mListener = l;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mListener != null) {
            mListener.SyncComplete(result);
        }
    }

}
