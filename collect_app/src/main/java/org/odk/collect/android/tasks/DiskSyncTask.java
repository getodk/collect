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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncTask;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.listeners.DiskSyncListener;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.Validator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

/**
 * Background task for adding to the forms content provider, any forms that have been added to the
 * sdcard manually. Returns immediately if it detects an error.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class DiskSyncTask extends AsyncTask<Void, String, String> {

    private static int counter;
    private DiskSyncListener listener;
    private String statusMessage = "";
    private FormsDao formsDao;

    @Override
    protected String doInBackground(Void... params) {
        formsDao = new FormsDao();
        int instance = ++counter;
        Timber.i("[%d] doInBackground begins!", instance);

        List<String> idsToDelete = new ArrayList<>();

        try {
            // Process everything then report what didn't work.
            StringBuilder errors = new StringBuilder();

            File formDir = new File(Collect.FORMS_PATH);
            if (formDir.exists() && formDir.isDirectory()) {
                // Get all the files in the /odk/foms directory
                List<File> formsToAdd = new LinkedList<File>();

                // Step 1: assemble the candidate form files
                //         discard files beginning with "."
                //         discard files not ending with ".xml" or ".xhtml"
                {
                    File[] formDefs = formDir.listFiles();
                    for (File addMe : formDefs) {
                        // Ignore invisible files that start with periods.
                        if (!addMe.getName().startsWith(".")
                                && (addMe.getName().endsWith(".xml") || addMe.getName().endsWith(
                                ".xhtml"))) {
                            formsToAdd.add(addMe);
                        } else {
                            Timber.i("[%d] Ignoring: %s", instance, addMe.getAbsolutePath());
                        }
                    }
                }

                // Step 2: quickly run through and figure out what files we need to
                // parse and update; this is quick, as we only calculate the md5
                // and see if it has changed.
                List<UriFile> uriToUpdate = new ArrayList<UriFile>();
                Cursor cursor = null;
                // open the cursor within a try-catch block so it can always be closed.
                try {
                    cursor = formsDao.getFormsCursor();
                    if (cursor == null) {
                        Timber.e("[%d] Forms Content Provider returned NULL", instance);
                        errors.append("Internal Error: Unable to access Forms content provider\r\n");
                        return errors.toString();
                    }

                    cursor.moveToPosition(-1);

                    while (cursor.moveToNext()) {
                        // For each element in the provider, see if the file already exists
                        String sqlFilename =
                                cursor.getString(
                                        cursor.getColumnIndex(FormsColumns.FORM_FILE_PATH));
                        String md5 = cursor.getString(
                                cursor.getColumnIndex(FormsColumns.MD5_HASH));
                        File sqlFile = new File(sqlFilename);
                        if (sqlFile.exists()) {
                            // remove it from the list of forms (we only want forms
                            // we haven't added at the end)
                            formsToAdd.remove(sqlFile);
                            String md5Computed = FileUtils.getMd5Hash(sqlFile);
                            if (md5Computed == null || md5 == null || !md5Computed.equals(md5)) {
                                // Probably someone overwrite the file on the sdcard
                                // So re-parse it and update it's information
                                String id = cursor.getString(
                                        cursor.getColumnIndex(FormsColumns._ID));
                                Uri updateUri = Uri.withAppendedPath(FormsColumns.CONTENT_URI, id);
                                uriToUpdate.add(new UriFile(updateUri, sqlFile));
                            }
                        } else {
                            //File not found in sdcard but file path found in database
                            //probably because the file has been deleted or filename was changed in sdcard
                            //Add the ID to list so that they could be deleted all together

                            String id = cursor.getString(
                                    cursor.getColumnIndex(FormsColumns._ID));

                            idsToDelete.add(id);
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                if (!idsToDelete.isEmpty()) {
                    //Delete the forms not found in sdcard from the database
                    formsDao.deleteFormsFromIDs(idsToDelete.toArray(new String[idsToDelete.size()]));
                }

                // Step3: go through uriToUpdate to parse and update each in turn.
                // This is slow because buildContentValues(...) is slow.
                Collections.shuffle(uriToUpdate); // Big win if multiple DiskSyncTasks running
                for (UriFile entry : uriToUpdate) {
                    Uri updateUri = entry.uri;
                    File formDefFile = entry.file;
                    // Probably someone overwrite the file on the sdcard
                    // So re-parse it and update it's information
                    ContentValues values;

                    try {
                        values = buildContentValues(formDefFile);
                    } catch (IllegalArgumentException e) {
                        errors.append(e.getMessage()).append("\r\n");
                        File badFile = new File(formDefFile.getParentFile(),
                                formDefFile.getName() + ".bad");
                        badFile.delete();
                        formDefFile.renameTo(badFile);
                        continue;
                    }

                    // update in content provider
                    int count =
                            Collect.getInstance().getContentResolver()
                                    .update(updateUri, values, null, null);
                    Timber.i("[%d] %d records successfully updated", instance, count);
                }
                uriToUpdate.clear();

                // Step 4: go through the newly-discovered files in xFormsToAdd and add them.
                // This is slow because buildContentValues(...) is slow.
                //
                Collections.shuffle(formsToAdd); // Big win if multiple DiskSyncTasks running
                while (!formsToAdd.isEmpty()) {
                    File formDefFile = formsToAdd.remove(0);

                    // Since parsing is so slow, if there are multiple tasks,
                    // they may have already updated the database.
                    // Skip this file if that is the case.
                    if (isAlreadyDefined(formDefFile)) {
                        Timber.i("[%d] skipping -- definition already recorded: %s",
                                instance, formDefFile.getAbsolutePath());
                        continue;
                    }

                    // Parse it for the first time...
                    ContentValues values;

                    try {
                        values = buildContentValues(formDefFile);
                    } catch (IllegalArgumentException e) {
                        errors.append(e.getMessage()).append("\r\n");
                        File badFile = new File(formDefFile.getParentFile(),
                                formDefFile.getName() + ".bad");
                        badFile.delete();
                        formDefFile.renameTo(badFile);
                        continue;
                    }

                    // insert into content provider
                    try {
                        // insert failures are OK and expected if multiple
                        // DiskSync scanners are active.
                        formsDao.saveForm(values);
                    } catch (SQLException e) {
                        Timber.i("[%d] %s", instance, e.toString());
                    }
                }
            }
            if (errors.length() != 0) {
                statusMessage = errors.toString();
            } else {
                Timber.d(Collect.getInstance().getString(R.string.finished_disk_scan));
            }
            return statusMessage;
        } finally {
            Timber.i("[%d] doInBackground ends!", instance);
        }
    }

    private boolean isAlreadyDefined(File formDefFile) {
        // first try to see if a record with this filename already exists...
        Cursor c = null;
        try {
            c = formsDao.getFormsCursorForFormFilePath(formDefFile.getAbsolutePath());
            return (c.getCount() > 0);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Attempts to parse the formDefFile as an XForm.
     * This is slow because FileUtils.parseXML is slow
     *
     * @return key-value list to update or insert into the content provider
     * @throws IllegalArgumentException if the file failed to parse or was missing fields
     */
    private ContentValues buildContentValues(File formDefFile) throws IllegalArgumentException {
        // Probably someone overwrite the file on the sdcard
        // So re-parse it and update it's information
        ContentValues updateValues = new ContentValues();

        HashMap<String, String> fields = null;
        try {
            fields = FileUtils.parseXML(formDefFile);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(formDefFile.getName() + " :: " + e.toString());
        }

        // update date
        Long now = System.currentTimeMillis();
        updateValues.put(FormsColumns.DATE, now);

        String title = fields.get(FileUtils.TITLE);

        if (title != null) {
            updateValues.put(FormsColumns.DISPLAY_NAME, title);
        } else {
            throw new IllegalArgumentException(
                    Collect.getInstance().getString(R.string.xform_parse_error,
                            formDefFile.getName(), "title"));
        }
        String formid = fields.get(FileUtils.FORMID);
        if (formid != null) {
            updateValues.put(FormsColumns.JR_FORM_ID, formid);
        } else {
            throw new IllegalArgumentException(
                    Collect.getInstance().getString(R.string.xform_parse_error,
                            formDefFile.getName(), "id"));
        }
        String version = fields.get(FileUtils.VERSION);
        if (version != null) {
            updateValues.put(FormsColumns.JR_VERSION, version);
        }
        String submission = fields.get(FileUtils.SUBMISSIONURI);
        if (submission != null) {
            if (Validator.isUrlValid(submission)) {
                updateValues.put(FormsColumns.SUBMISSION_URI, submission);
            } else {
                throw new IllegalArgumentException(
                        Collect.getInstance().getString(R.string.xform_parse_error,
                                formDefFile.getName(), "submission url"));
            }
        }
        String base64RsaPublicKey = fields.get(FileUtils.BASE64_RSA_PUBLIC_KEY);
        if (base64RsaPublicKey != null) {
            updateValues.put(FormsColumns.BASE64_RSA_PUBLIC_KEY, base64RsaPublicKey);
        }
        updateValues.put(FormsColumns.AUTO_DELETE, fields.get(FileUtils.AUTO_DELETE));
        updateValues.put(FormsColumns.AUTO_SEND, fields.get(FileUtils.AUTO_SEND));
        // Note, the path doesn't change here, but it needs to be included so the
        // update will automatically update the .md5 and the cache path.
        updateValues.put(FormsColumns.FORM_FILE_PATH, formDefFile.getAbsolutePath());

        return updateValues;
    }

    public void setDiskSyncListener(DiskSyncListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (listener != null) {
            listener.syncComplete(result);
        }
    }

    private static class UriFile {
        public final Uri uri;
        public final File file;

        UriFile(Uri uri, File file) {
            this.uri = uri;
            this.file = file;
        }
    }
}
