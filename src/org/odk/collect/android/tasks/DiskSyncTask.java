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

import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.FileUtils;

import android.content.ContentResolver;
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
 * sdcard manuall
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DiskSyncTask extends AsyncTask<Void, String, Void> {
    private final static String t = "DiskSyncTask";

    Cursor mCursor;
    ContentResolver mContentResolver;
    DiskSyncListener mListener;


    public DiskSyncTask(Cursor c, ContentResolver cr) {
        super();
        mCursor = c;
        mContentResolver = cr;
        mListener = null;
    }


    @Override
    protected Void doInBackground(Void... params) {
        mCursor.moveToPosition(-1);

        File formDir = new File(FileUtils.FORMS_PATH);
        if (formDir.exists() && formDir.isDirectory()) {
            ArrayList<File> xForms = new ArrayList<File>(Arrays.asList(formDir.listFiles()));

            while (mCursor.moveToNext()) {
                String sqlFilename =
                    mCursor.getString(mCursor.getColumnIndex(FormsColumns.FORM_FILE_PATH));
                String md5 =  mCursor.getString(mCursor.getColumnIndex(FormsColumns.MD5_HASH));
                File sqlFile = new File(sqlFilename);
                if (sqlFile.exists()) {
                    // remove it from the arraylist
                    xForms.remove(sqlFile);
                    if (!FileUtils.getMd5Hash(sqlFile).contentEquals(md5)) {
                        // Probably someone overwrite the file on the sdcard, update its md5.
                        String id =  mCursor.getString(mCursor.getColumnIndex(FormsColumns._ID));
                        Uri update = Uri.withAppendedPath(FormsColumns.CONTENT_URI, id);
                        ContentValues updateValues = new ContentValues();
                        // Note, this is the same path here, but update will automatically update the .md5
                        // and the cache path.
                        updateValues.put(FormsColumns.FORM_FILE_PATH, sqlFile.getAbsolutePath());
                        int count = mContentResolver.update(update, updateValues, null, null);
                        Log.i(t, count + " records successfully updated");
                    }
                } else {
                    Log.w(t, "file referenced by content provider does not exist " + sqlFile);
                }
            }

            // Whatever is left in our arraylist isn't in the database, so add it
            for (int i = 0; i < xForms.size(); i++) {
                ContentValues values = new ContentValues();
                File addMe = xForms.get(i);
                
                if (addMe.getName().endsWith(".xml") || addMe.getName().endsWith(".xhtml")) {
                    HashMap<String, String> fields = FileUtils.parseXML(addMe);
                    
                    String title = fields.get(FileUtils.TITLE);
                    String ui = fields.get(FileUtils.UI);
                    String model = fields.get(FileUtils.MODEL);
                    String formid = fields.get(FileUtils.FORMID);
                    String submission = fields.get(FileUtils.SUBMISSIONURI);
                    
                    if (title != null) {
                        values.put(FormsColumns.DISPLAY_NAME, title);   
                    } else {
                        // TODO:  Return some nasty error.
                    }
                    if (formid != null) {
                        values.put(FormsColumns.JR_FORM_ID, formid);                    
                    } else {
                     // TODO:  return some nasty error.  
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
                    
                    Uri uri = mContentResolver.insert(FormsColumns.CONTENT_URI, values);
                } else {
                    // it's a [formname]-media directory, likely, so skip it
                }
            }
        }
        return null;

    }


    public void setDiskSyncListener(DiskSyncListener l) {
        mListener = l;
    }

   


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mListener != null) {
            // mListener.SyncComplete();
        }
    }

}
