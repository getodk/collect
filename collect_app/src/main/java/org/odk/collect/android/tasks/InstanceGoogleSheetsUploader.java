/*
 * Copyright (C) 2017 Nafundi
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

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Xml;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.exception.BadUrlException;
import org.odk.collect.android.exception.FormException;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.utilities.gdrive.DriveHelper;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;
import org.odk.collect.android.utilities.gdrive.SheetsHelper;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.UrlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * @author carlhartung (chartung@nafundi.com)
 */
public class InstanceGoogleSheetsUploader extends InstanceUploader {

    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final String GOOGLE_DRIVE_ROOT_FOLDER = "Open Data Kit";
    public static final String GOOGLE_DRIVE_SUBFOLDER = "Submissions";

    private static final String oauth_fail = "OAUTH Error: ";
    private static final String UPLOADED_MEDIA_URL = "https://drive.google.com/open?id=";

    // needed in case of rate limiting
    private static final int GOOGLE_SLEEP_TIME = 1000;

    private final SheetsHelper sheetsHelper;
    private final DriveHelper driveHelper;

    private final GoogleAccountsManager accountsManager;
    private Integer sheetId;
    private String sheetName;
    private Outcome outcome;
    private boolean hasWritePermissionToSheet = false;
    private boolean authFailed;
    private String googleSheetsUrl = "";
    private String spreadsheetId;

    public InstanceGoogleSheetsUploader(GoogleAccountsManager accountsManager) {
        this.accountsManager = accountsManager;
        sheetsHelper = accountsManager.getSheetsHelper();
        driveHelper = accountsManager.getDriveHelper();
    }

    /**
     * Google sheets currently only allows a-zA-Z0-9 and dash
     */

    public static boolean isValidGoogleSheetsString(String name) {
        return Pattern
                .compile("^[a-zA-Z0-9\\-]+$").matcher(name).matches();
    }

    public static boolean isValidLocation(String answer) {
        return Pattern
                .compile("^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
                        + ".[0-9]+\\s[0-9]+\\.[0-9]+$").matcher(answer).matches();
    }

    private String getGoogleSheetsUrl(Cursor cursor) {
        int subIdx = cursor.getColumnIndex(InstanceColumns.SUBMISSION_URI);
        String urlString = cursor.isNull(subIdx) ? null : cursor.getString(subIdx);
        // if we didn't find one in the content provider,
        // try to get from settings
        if (urlString == null) {
            urlString = (String) GeneralSharedPreferences.getInstance()
                    .get(PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
        }
        return urlString;
    }

    private void uploadInstances(String selection, String[] selectionArgs, String token, int low, int instanceCount) {
        Cursor c = null;
        try {
            c = new InstancesDao().getInstancesCursor(selection, selectionArgs);

            if (c.getCount() > 0) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if (isCancelled()) {
                        return;
                    }
                    String id = c.getString(c.getColumnIndex(InstanceColumns._ID));
                    String jrformid = c.getString(c.getColumnIndex(InstanceColumns.JR_FORM_ID));
                    Uri toUpdate = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id);
                    ContentValues cv = new ContentValues();

                    Cursor formcursor = new FormsDao().getFormsCursorForFormId(jrformid);
                    String md5 = null;
                    String formFilePath = null;
                    if (formcursor.getCount() > 0) {
                        formcursor.moveToFirst();
                        md5 = formcursor
                                .getString(formcursor.getColumnIndex(FormsColumns.MD5_HASH));
                        formFilePath = formcursor.getString(formcursor
                                .getColumnIndex(FormsColumns.FORM_FILE_PATH));
                    }

                    if (md5 == null) {
                        // fail and exit
                        Timber.e("no md5");
                        return;
                    }

                    publishProgress(c.getPosition() + 1 + low, instanceCount);
                    String instance = c.getString(c
                            .getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
                    String urlString = getGoogleSheetsUrl(c);

                    if (!uploadOneSubmission(id, instance, jrformid, token, formFilePath, urlString)) {
                        cv.put(InstanceColumns.STATUS,
                                InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                        return;
                    } else {
                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private boolean uploadOneSubmission(String id, String instanceFilePath, String jrFormId,
                                        String token, String formFilePath, String urlString) {
        // if the token is null fail immediately
        if (token == null) {
            outcome.results.put(id, oauth_fail + Collect.getInstance().getString(R.string.invalid_oauth));
            return false;
        }

        // checking for write permissions to the spreadsheet
        if (!hasWritePermissionToSheet || !urlString.equals(googleSheetsUrl)) {
            try {
                spreadsheetId = UrlUtils.getSpreadsheetID(urlString);

                Spreadsheet spreadsheet = sheetsHelper.getSpreadsheet(spreadsheetId);
                sheetId = spreadsheet.getSheets().get(0).getProperties().getSheetId();
                sheetName = spreadsheet.getSheets().get(0).getProperties().getTitle();
            } catch (GoogleJsonResponseException e) {
                String message = e.getMessage();
                if (e.getDetails() != null && e.getDetails().getCode() == 403) {
                    message = Collect.getInstance().getString(R.string.google_sheets_access_denied);
                }
                outcome.results.put(id, message);
                return false;
            } catch (BadUrlException | IOException e) {
                Timber.i(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }
            hasWritePermissionToSheet = true;
            googleSheetsUrl = urlString;
        }

        // get instance file
        File instanceFile = new File(instanceFilePath);

        // first check to see how many columns we have:
        ArrayList<String> columnNames = new ArrayList<String>();
        try {
            getColumns(formFilePath, columnNames);
        } catch (XmlPullParserException | IOException | FormException e2) {
            Timber.e(e2, "Exception thrown while getting columns from form file");
            outcome.results.put(id, e2.getMessage());
            return false;
        }

        if (columnNames.size() == 0) {
            outcome.results.put(id, "No columns found in the form to upload");
            return false;
        }

        if (columnNames.size() > 255) {
            outcome.results.put(id, Collect.getInstance().getString(R.string.sheets_max_columns,
                    String.valueOf(columnNames.size())));
            return false;
        }

        // make sure column names are legal
        for (String n : columnNames) {
            if (!isValidGoogleSheetsString(n)) {
                outcome.results.put(id,
                        Collect.getInstance().getString(R.string.google_sheets_invalid_column_form,
                                n));
                return false;
            }
        }

        // parses the instance file and populates the answers and photos
        // hashmaps.
        HashMap<String, String> answersToUpload = new HashMap<>();
        HashMap<String, String> mediaToUpload = new HashMap<>();

        try {
            processInstanceXML(instanceFile, answersToUpload, mediaToUpload);
        } catch (FormException e) {
            outcome.results.put(id,
                    Collect.getInstance().getString(R.string.google_repeat_error));
            return false;
        } catch (XmlPullParserException | IOException e) {
            Timber.e(e, "Exception thrown while parsing the file");
            outcome.results.put(id, e.getMessage());
            return false;
        }

        try {
            Thread.sleep(GOOGLE_SLEEP_TIME);
        } catch (InterruptedException e3) {
            Timber.d(e3);
        }

        // make sure column names in submission are legal (may be different than form)
        for (String n : answersToUpload.keySet()) {
            if (!isValidGoogleSheetsString(n)) {
                outcome.results.put(id, Collect.getInstance()
                        .getString(R.string.google_sheets_invalid_column_instance, n));
                return false;
            }
        }

        /*  # NOTE #
         *  Media files are uploaded to Google Drive of user
         *  All media files are currently saved under folder "Open Data Kit/Submissions/formID/"
         */

        // if we have any media files to upload,
        // get the folder or create a new one
        // then upload the media files
        HashMap<String, String> uploadedMedia = new HashMap<>();
        if (mediaToUpload.size() > 0) {

            for (String key : mediaToUpload.keySet()) {
                String filename = instanceFile.getParentFile() + "/" + mediaToUpload.get(key);
                File toUpload = new File(filename);

                // first check the local content provider
                // to see if this photo still exists at the location or not
                String selection = MediaStore.Images.Media.DATA + "=?";
                String[] selectionArgs = {
                        filename
                };
                Cursor c = Collect.getInstance().getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection,
                                selectionArgs, null);
                if (c.getCount() != 1) {
                    c.close();
                    try {
                        throw new FileNotFoundException(Collect.getInstance()
                                .getString(R.string.media_upload_error, filename));
                    } catch (FileNotFoundException e) {
                        Timber.e(e);
                    }
                }
                c.close();

                String folderId;
                try {
                    folderId = driveHelper.createOrGetIDOfFolderWithName(jrFormId);
                } catch (IOException | MultipleFoldersFoundException e) {
                    Timber.e(e);
                    outcome.results.put(id, e.getMessage());
                    return false;
                }

                String uploadedFileId;

                // file is ready to be uploaded
                try {
                    uploadedFileId = driveHelper.uploadFileToDrive(mediaToUpload.get(key),
                            folderId, toUpload);
                } catch (IOException e) {
                    Timber.e(e, "Exception thrown while uploading the file to drive");
                    outcome.results.put(id, e.getMessage());
                    return false;
                }

                //checking if file was successfully uploaded
                if (uploadedFileId == null) {
                    outcome.results.put(id, "Unable to upload the media files. Try again");
                    return false;
                }

                // uploadedPhotos keeps track of the uploaded URL
                uploadedMedia.put(key, UPLOADED_MEDIA_URL + uploadedFileId);
            }
        }
        // All photos have been sent to Google Drive (if there were any)
        // now upload data to Google Sheet

        List<List<Object>> values;
        List headerFeed = null;

        try {
            values = sheetsHelper.getHeaderFeed(spreadsheetId, sheetName);
            if (values == null || values.size() == 0) {
                outcome.results.put(id, "No data found");
            } else {
                headerFeed = values.get(0);
            }
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            return false;
        }

        // check the headers....
        boolean emptyheaders = true;

        // go through headers
        // if they're empty, resize and add
        if (headerFeed != null) {
            for (Object c : headerFeed) {
                if (c != null) {
                    emptyheaders = false;
                    break;
                }
            }
        }

        if (emptyheaders) {
            // if the headers were empty, resize the spreadsheet
            // and add the headers

            try {
                sheetsHelper.resizeSpreadSheet(spreadsheetId, sheetId, columnNames.size());
            } catch (IOException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }

            //adding the headers
            ArrayList<Object> list = new ArrayList<>();
            list.addAll(columnNames);

            ArrayList<List<Object>> content = new ArrayList<>();
            content.add(list);
            ValueRange row = new ValueRange();
            row.setValues(content);

            /*
             *  Appends the data at the last row
             *
             *  append(spreadsheetId, range, ValueRange)
             *
             *  spreadsheetId   :   Unique sheet id
             *  range           :   A1 notation range. It specifies the range within which the
             *                      spreadsheet should be searched.
             *              hint   "Giving only sheetName in range searches in the complete sheet"
             *  ValueRange      :   Content that needs to be appended  (List<List<Object>>)
             *
             *  For more info   :   https://developers.google.com/sheets/api/reference/rest/
             */

            // Send the new row to the API for insertion.
            // write the headers
            try {
                sheetsHelper.insertRow(spreadsheetId, sheetName, row);
            } catch (IOException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }
        }

        // we may have updated the feed, so get a new one
        // update the feed

        try {
            values = sheetsHelper.getHeaderFeed(spreadsheetId, sheetName);
            if (values == null || values.size() == 0) {
                outcome.results.put(id, "No data found");
                return false;
            } else {
                headerFeed = values.get(0);
            }
        } catch (IOException e) {
            Timber.e(e, "Exception thrown while getting the header feed");
            outcome.results.put(id, e.getMessage());
            return false;
        }

        //check if any column name is blank

        boolean hasEmptyColumn = false;
        for (Object column : headerFeed) {
            if (column.equals("")) {
                hasEmptyColumn = true;
                break;
            }
        }

        // replace blank column name with a single space

        if (hasEmptyColumn) {
            ArrayList<Object> list = new ArrayList<>();
            for (Object column : headerFeed) {
                if (column.equals("")) {
                    list.add(" ");
                } else {
                    list.add(column);
                }
            }

            ArrayList<List<Object>> content = new ArrayList<>();
            content.add(list);
            ValueRange row = new ValueRange();
            row.setValues(content);

            try {
                sheetsHelper.insertRow(spreadsheetId, sheetName + "!A1:1", row);
            } catch (IOException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }
        }

        // we may have updated the feed, so get a new one
        // update the feed

        try {
            values = sheetsHelper.getHeaderFeed(spreadsheetId, sheetName);
            if (values == null || values.size() == 0) {
                outcome.results.put(id, "No data found");
                return false;
            } else {
                headerFeed = values.get(0);
            }
        } catch (IOException e) {
            Timber.e(e, "Exception thrown while getting the header feed");
            outcome.results.put(id, e.getMessage());
            return false;
        }

        // first, get all the columns in the spreadsheet
        ArrayList<String> sheetCols = new ArrayList<>();
        if (headerFeed != null) {
            for (Object column : headerFeed) {
                sheetCols.add(column.toString());
            }
        } else {
            outcome.results.put(id, "couldn't get header feed");
            return false;
        }

        ArrayList<String> missingColumns = new ArrayList<>();
        for (String col : columnNames) {
            if (!sheetCols.contains(col)) {
                missingColumns.add(col);
            }
        }

        if (missingColumns.size() > 0) {
            // we had some missing columns, so error out
            StringBuilder missingString = new StringBuilder();
            for (int i = 0; i < missingColumns.size(); i++) {
                missingString.append(missingColumns.get(i));
                if (i < missingColumns.size() - 1) {
                    missingString.append(", ");
                }
            }
            outcome.results.put(id, Collect.getInstance().getString(
                    R.string.google_sheets_missing_columns, missingString.toString()));
            return false;
        }

        // if we get here.. all has matched
        // so write the values

        // add photos to answer set
        for (String key : uploadedMedia.keySet()) {
            String url = uploadedMedia.get(key);
            answersToUpload.put(key, url);
        }

        ArrayList<Object> list = new ArrayList<>();

        for (String path : sheetCols) {
            String answer = "";
            if (path.equals(" ") || !columnNames.contains(path)) {
                //ignores the blank fields and extra fields
            } else if (columnNames.contains(path)) { // if column present in sheet
                if (answersToUpload.containsKey(path)) {
                    answer = answersToUpload.get(path);
                    // Check to see if answer is a location, if so, get rid of accuracy
                    // and altitude
                    // try to match a fairly specific pattern to determine
                    // if it's a location
                    // [-]#.# [-]#.# #.# #.#

                    if (isValidLocation(answer)) {
                        // get rid of everything after the second space
                        int firstSpace = answer.indexOf(' ');
                        int secondSpace = answer.indexOf(" ", firstSpace + 1);
                        answer = answer.substring(0, secondSpace);
                        answer = answer.replace(' ', ',');
                    }
                }
            }
            // https://github.com/opendatakit/collect/issues/931
            list.add(answer.isEmpty() ? " " : answer);
        }
        ArrayList<List<Object>> content = new ArrayList<>();
        content.add(list);

        ValueRange row = new ValueRange();
        row.setValues(content);


        // Send the new row to the API for insertion.
        try {
            sheetsHelper.insertRow(spreadsheetId, sheetName, row);
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            return false;
        }

        outcome.results.put(id, Collect.getInstance().getString(R.string.success));
        return true;
    }

    private void getColumns(String filePath, ArrayList<String> columns)
            throws XmlPullParserException, IOException, FormException {
        File formFile = new File(filePath);
        FileInputStream in;

        in = new FileInputStream(formFile);
        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        readFormFeed(parser, columns);
        in.close();
    }

    private void readFormFeed(XmlPullParser parser, ArrayList<String> columns)
            throws XmlPullParserException, IOException, FormException {
        ArrayList<String> path = new ArrayList<String>();

        // we put path names in here as we go, and if we hit a duplicate we
        // blow up
        boolean getPaths = false;
        boolean inBody = false;
        boolean isPrimaryInstanceIdentified = false;
        int event = parser.next();
        int depth = 0;
        int lastpush = 0;
        while (event != XmlPullParser.END_DOCUMENT && !isPrimaryInstanceIdentified) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    if (parser.getName().equalsIgnoreCase("body")
                            || parser.getName().equalsIgnoreCase("h:body")) {
                        inBody = true;
                    } else if (inBody && parser.getName().equalsIgnoreCase("repeat")) {
                        throw new FormException(Collect.getInstance().getString(
                                R.string.google_repeat_error));
                    }
                    if (getPaths) {
                        path.add(parser.getName());
                        depth++;
                        lastpush = depth;
                    }
                    if (parser.getName().equals("instance")) {
                        getPaths = true;
                    }
                    break;
                case XmlPullParser.TEXT:
                    // skip it
                    break;
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("body") || parser.getName().equals("h:body")) {
                        inBody = false;
                    }
                    if (parser.getName().equals("instance")) {

                        /*
                         *  A <model> can have multiple instances as <childnodes>.
                         *  The first and required <instance> is called the <primary instance>
                         *  and represents the data structure of the record that will be created
                         *  and submitted with the form.
                         *  Additional instances are called <secondary instances>.
                         *  So, we are breaking the loop after discovering the <primary instance> so that
                         *  <secondary instances> don't get counted as field columns.
                         *
                         *  For more info read [this](https://opendatakit.github.io/xforms-spec/#instance)
                         *
                         *  https://github.com/opendatakit/collect/issues/1444
                         */

                        isPrimaryInstanceIdentified = true;
                        break;
                    }
                    if (getPaths) {
                        if (depth == lastpush) {
                            columns.add(getPath(path));
                        } else {
                            lastpush--;
                        }
                        path.remove(path.size() - 1);
                        depth--;
                    }
                    break;
                default:
                    Timber.i("DEFAULTING: %s :: %d", parser.getName(), parser.getEventType());
                    break;
            }
            event = parser.next();
        }
    }

    private void processInstanceXML(File instanceFile,
                                    HashMap<String, String> answersToUpload,
                                    HashMap<String, String> mediaToUpload)
            throws XmlPullParserException, IOException,
            FormException {
        FileInputStream in;

        in = new FileInputStream(instanceFile);
        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        readInstanceFeed(parser, answersToUpload, mediaToUpload, instanceFile.getParentFile());
        in.close();
    }

    private void readInstanceFeed(XmlPullParser parser,
                                  HashMap<String, String> answersToUpload,
                                  HashMap<String, String> mediaToUpload, File instanceFolder)
            throws XmlPullParserException, IOException,
            FormException {

        ArrayList<String> path = new ArrayList<String>();

        int event = parser.next();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    path.add(parser.getName());
                    break;
                case XmlPullParser.TEXT:
                    String answer = parser.getText();

                    String filename = instanceFolder + "/" + answer;
                    File file = new File(filename);

                    if (file.isFile()) {
                        mediaToUpload.put(getPath(path), answer);
                    } else {
                        answersToUpload.put(getPath(path), answer);
                    }

                    break;
                case XmlPullParser.END_TAG:
                    path.remove(path.size() - 1);
                    break;
                default:
                    Timber.i("DEFAULTING: %s :: %d", parser.getName(), parser.getEventType());
                    break;
            }
            event = parser.next();
        }
    }

    private String getPath(ArrayList<String> path) {
        StringBuilder currentPath = new StringBuilder();
        boolean first = true;
        for (String node : path) {
            if (first) {
                currentPath = new StringBuilder(node);
                first = false;
            } else {
                currentPath.append('-').append(node);
            }
        }
        return currentPath.toString();
    }

    @Override
    protected Outcome doInBackground(Long... values) {
        outcome = new Outcome();
        int counter = 0;

        try {
            while (counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER < values.length) {
                int low = counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
                int high = (counter + 1) * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
                if (high > values.length) {
                    high = values.length;
                }

                StringBuilder selectionBuf = new StringBuilder(InstanceColumns._ID + " IN (");
                String[] selectionArgs = new String[high - low];
                for (int i = 0; i < (high - low); i++) {
                    if (i > 0) {
                        selectionBuf.append(",");
                    }
                    selectionBuf.append("?");
                    selectionArgs[i] = values[i + low].toString();
                }

                selectionBuf.append(")");
                String selection = selectionBuf.toString();

                String token = accountsManager.getCredential().getToken();

                //Immediately invalidate so we get a different one if we have to try again
                GoogleAuthUtil.invalidateToken(accountsManager.getContext(), token);

                // check if root folder exists, if not then create one
                driveHelper.getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null, true);

                uploadInstances(selection, selectionArgs, token, low, values.length);
                counter++;
            }
        } catch (UserRecoverableAuthException e) {
            outcome = null;
            Activity activity = accountsManager.getActivity();
            if (activity != null) {
                activity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }
        } catch (IOException | GoogleAuthException e) {
            Timber.e(e);
            authFailed = true;
        } catch (MultipleFoldersFoundException e) {
            Timber.e(e);
        }
        return outcome;
    }

    public boolean isAuthFailed() {
        return authFailed;
    }

    public void setAuthFailed() {
        authFailed = false;
    }
}


