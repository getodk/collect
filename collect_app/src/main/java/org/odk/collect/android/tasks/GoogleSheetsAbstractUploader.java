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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.exception.BadUrlException;
import org.odk.collect.android.exception.FormException;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.UrlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author carlhartung (chartung@nafundi.com)
 */
public abstract class GoogleSheetsAbstractUploader extends
        GoogleSheetsTask<Long, Integer, HashMap<String, String>> {

    protected final static String GOOGLE_DRIVE_ROOT_FOLDER = "Open Data Kit";
    private static final String oauth_fail = "OAUTH Error: ";
    private final static String TAG = "GoogleSheetsUploadTask";
    private static final String UPLOADED_MEDIA_URL = "https://drive.google.com/open?id=";

    private final static String GOOGLE_DRIVE_SUBFOLDER = "Submissions";
    // needed in case of rate limiting
    private static final int GOOGLE_SLEEP_TIME = 1000;
    protected HashMap<String, String> mResults;
    private String mSpreadsheetName;
    private String mSpreadsheetId;
    private boolean hasWritePermissonToSheet = false;
    private String mSpreadsheetFileName;
    private Integer mSheetId;

    /**
     * @param selection
     * @param selectionArgs
     * @param token
     */
    protected void uploadInstances(String selection, String[] selectionArgs, String token) {
        Cursor c = null;
        try {
            c = new InstancesDao().getInstancesCursor(selection, selectionArgs);

            if (c.getCount() > 0) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    if (isCancelled()) {
                        return;
                    }
                    String instance = c.getString(c
                            .getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
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
                        Log.e(TAG, "no md5");
                        return;
                    }

                    publishProgress(c.getPosition() + 1, c.getCount());
                    if (!uploadOneSubmission(id, instance, jrformid, token, formFilePath)) {
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
                                        String token, String formFilePath) {
        // if the token is null fail immediately
        if (token == null) {
            mResults.put(id, oauth_fail + Collect.getInstance().getString(R.string.invalid_oauth));
            return false;
        }

        // get spreadsheet id
        if (mSpreadsheetId == null) {
            try {
                mSpreadsheetId = UrlUtils.getSpreadsheetID(id);
            } catch (BadUrlException e) {
                mResults.put(id, e.getMessage());
                return false;
            }
        }

        // checking for write permissions to the spreadsheet
        if (!hasWritePermissonToSheet) {
            try {
                mSpreadsheetName = getSpreadSheetName();

                //// TODO: 22/3/17 Find a better way to check the write permissions
                List<Request> requests = new ArrayList<>();
                requests.add(new Request()
                        .setUpdateSpreadsheetProperties(new UpdateSpreadsheetPropertiesRequest()
                                .setProperties(new SpreadsheetProperties()
                                        .setTitle(mSpreadsheetFileName))
                                .setFields("title")));

                mSheetsService.spreadsheets()
                        .batchUpdate(mSpreadsheetId, new BatchUpdateSpreadsheetRequest()
                                .setRequests(requests))
                        .execute();
            } catch (GoogleJsonResponseException e) {
                Log.e(TAG, e.getMessage(), e);
                String message = e.getMessage();
                if (e.getDetails().getCode() == 403)
                    message = Collect.getInstance().getString(R.string.google_sheets_access_denied);
                mResults.put(id, message);
                return false;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                mResults.put(id, e.getMessage());
                return false;
            }
            hasWritePermissonToSheet = true;
        }

        HashMap<String, String> answersToUpload = new HashMap<>();
        HashMap<String, String> mediaToUpload = new HashMap<>();
        HashMap<String, String> uploadedMedia = new HashMap<>();

        // get instance file
        File instanceFile = new File(instanceFilePath);

        // first check to see how many columns we have:
        ArrayList<String> columnNames = new ArrayList<String>();
        try {
            getColumns(formFilePath, columnNames);
        } catch (FileNotFoundException e2) {
            mResults.put(id, e2.getMessage());
            return false;
        } catch (XmlPullParserException e2) {
            mResults.put(id, e2.getMessage());
            return false;
        } catch (IOException e2) {
            mResults.put(id, e2.getMessage());
            return false;
        } catch (FormException e2) {
            mResults.put(id, e2.getMessage());
            return false;
        }

        if (columnNames.size() == 0) {
            mResults.put(id, "No columns found in the form to upload");
            return false;
        }

        if (columnNames.size() > 255) {
            mResults.put(id, Collect.getInstance().getString(R.string.sheets_max_columns,
                    String.valueOf(columnNames.size())));
            return false;
        }

        // make sure column names are legal
        for (String n : columnNames) {
            if (!isValidGoogleSheetsString(n)) {
                mResults.put(id,
                        Collect.getInstance().getString(R.string.google_sheets_invalid_column_form,
                                n));
                return false;
            }
        }

        // parses the instance file and populates the answers and photos
        // hashmaps.
        try {
            processInstanceXML(instanceFile, answersToUpload, mediaToUpload);
        } catch (XmlPullParserException e) {
            mResults.put(id, e.getMessage());
            return false;
        } catch (FormException e) {
            mResults.put(id,
                    Collect.getInstance().getString(R.string.google_repeat_error));
            return false;
        } catch (FileNotFoundException e) {
            mResults.put(id, e.getMessage());
            return false;
        } catch (IOException e) {
            mResults.put(id, e.getMessage());
            return false;
        }

        try {
            Thread.sleep(GOOGLE_SLEEP_TIME);
        } catch (InterruptedException e3) {
        }

        // make sure column names in submission are legal (may be different than form)
        for (String n : answersToUpload.keySet()) {
            if (!isValidGoogleSheetsString(n)) {
                mResults.put(id, Collect.getInstance()
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
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection
                                , selectionArgs, null);
                if (c.getCount() != 1) {
                    c.close();
                    try {
                        throw new FileNotFoundException(Collect.getInstance()
                                .getString(R.string.media_upload_error, filename));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                c.close();

                String folderId;
                try {
                    folderId = createOrGetIDOfFolderWithName(jrFormId);
                } catch (IOException | MultipleFoldersFoundException e) {
                    Log.e(TAG, e.getMessage(), e);
                    mResults.put(id, e.getMessage());
                    return false;
                }

                String uploadedFileId;

                // file is ready to be uploaded
                try {
                    uploadedFileId = uploadFileToDrive(mediaToUpload.get(key),
                            folderId, toUpload);
                } catch (IOException e) {
                    e.printStackTrace();
                    mResults.put(id, e.getMessage());
                    return false;
                }

                //checking if file was successfully uploaded
                if (uploadedFileId == null) {
                    mResults.put(id, "Unable to upload the media files. Try again");
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
            values = getHeaderFeed(mSpreadsheetId, mSpreadsheetName);
            if (values == null || values.size() == 0) {
                mResults.put(id, "No data found");
            } else {
                headerFeed = values.get(0);
            }
        } catch (IOException e) {
            mResults.put(id, e.getMessage());
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

            //resizing the spreadsheet
            SheetProperties sheetProperties = new SheetProperties()
                    .setSheetId(mSheetId)
                    .setGridProperties(new GridProperties()
                            .setColumnCount(columnNames.size()));

            List<Request> requests = new ArrayList<>();
            requests.add(new Request()
                    .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                            .setProperties(sheetProperties)
                            .setFields("gridProperties.columnCount")));

            try {
                mSheetsService.spreadsheets()
                        .batchUpdate(mSpreadsheetId, new BatchUpdateSpreadsheetRequest()
                                .setRequests(requests))
                        .execute();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                mResults.put(id, e.getMessage());
                return false;
            }

            //adding the headers
            ArrayList<Object> list = new ArrayList<>();
            for (String column : columnNames)
                list.add(column);

            ArrayList<List<Object>> content = new ArrayList<>();
            content.add(list);
            ValueRange row = new ValueRange();
            row.setValues(content);

            /*
             *  Appends the data at the last row
             *
             *  append(mSpreadsheetId, range, ValueRange)
             *
             *  mSpreadsheetId   :   Unique sheet id
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
                mSheetsService.spreadsheets().values()
                        .append(mSpreadsheetId, mSpreadsheetName, row)
                        .setIncludeValuesInResponse(true)
                        .setValueInputOption("USER_ENTERED").execute();
            } catch (IOException e) {
                mResults.put(id, e.getMessage());
                return false;
            }
        }

        // we may have updated the feed, so get a new one
        // update the feed

        try {
            values = getHeaderFeed(mSpreadsheetId, mSpreadsheetName);
            if (values == null || values.size() == 0) {
                mResults.put(id, "No data found");
                return false;
            } else {
                headerFeed = values.get(0);
            }
        } catch (IOException e) {
            mResults.put(id, e.getMessage());
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
                mSheetsService.spreadsheets().values()
                        .update(mSpreadsheetId, mSpreadsheetName + "!A1:1", row)
                        .setValueInputOption("USER_ENTERED").execute();
            } catch (IOException e) {
                mResults.put(id, e.getMessage());
                return false;
            }
        }

        // we may have updated the feed, so get a new one
        // update the feed

        try {
            values = getHeaderFeed(mSpreadsheetId, mSpreadsheetName);
            if (values == null || values.size() == 0) {
                mResults.put(id, "No data found");
                return false;
            } else {
                headerFeed = values.get(0);
            }
        } catch (IOException e) {
            mResults.put(id, e.getMessage());
            return false;
        }

        // first, get all the columns in the spreadsheet
        ArrayList<String> sheetCols = new ArrayList<>();
        if (headerFeed != null) {
            for (Object column : headerFeed) {
                sheetCols.add(column.toString());
            }
        } else {
            mResults.put(id, "couldn't get header feed");
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
            String missingString = "";
            for (int i = 0; i < missingColumns.size(); i++) {
                missingString += missingColumns.get(i);
                if (i < missingColumns.size() - 1) {
                    missingString += ", ";
                }
            }
            mResults.put(id, Collect.getInstance().getString(
                    R.string.google_sheets_missing_columns, missingString));
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
                    Pattern p = Pattern
                            .compile(
                                    "^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
                                            + ".[0-9]+\\s[0-9]+\\.[0-9]+$");
                    Matcher m = p.matcher(answer);
                    if (m.matches()) {
                        // get rid of everything after the second space
                        int firstSpace = answer.indexOf(" ");
                        int secondSpace = answer.indexOf(" ", firstSpace + 1);
                        answer = answer.substring(0, secondSpace);
                        answer = answer.replace(' ', ',');
                    }
                }
            }
            list.add(answer);
        }
        ArrayList<List<Object>> content = new ArrayList<>();
        content.add(list);

        ValueRange row = new ValueRange();
        row.setValues(content);


        // Send the new row to the API for insertion.
        try {
            mSheetsService.spreadsheets().values()
                    .append(mSpreadsheetId, mSpreadsheetName, row)
                    .setValueInputOption("USER_ENTERED").execute();
        } catch (IOException e) {
            mResults.put(id, e.getMessage());
            return false;
        }

        mResults.put(id, Collect.getInstance().getString(R.string.success));
        return true;
    }

    private String getSpreadSheetName() throws IOException {
        Spreadsheet response;
        response = mSheetsService.spreadsheets()
                .get(mSpreadsheetId)
                .setIncludeGridData(false)
                .execute();

        mSpreadsheetFileName = response.getProperties().getTitle();
        mSheetId = response.getSheets().get(0).getProperties().getSheetId();
        return response.getSheets().get(0).getProperties().getTitle();
    }

    private String createOrGetIDOfFolderWithName(String jrFormId)
            throws IOException, MultipleFoldersFoundException {

        String submissionsFolderId = createOrGetIDOfSubmissionsFolder();
        return getIDOfFolderWithName(jrFormId, submissionsFolderId);
    }

    private String createOrGetIDOfSubmissionsFolder() throws IOException,
            MultipleFoldersFoundException {

        String rootFolderId = getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null);
        return getIDOfFolderWithName(GOOGLE_DRIVE_SUBFOLDER, rootFolderId);
    }

    protected String getIDOfFolderWithName(String name, String inFolder)
            throws IOException, MultipleFoldersFoundException {

        com.google.api.services.drive.model.File folder = null;

        // check if the folder exists
        ArrayList<com.google.api.services.drive.model.File> files =
                getFilesFromDrive(name, inFolder);

        for (com.google.api.services.drive.model.File file : files) {
            if (folder == null) {
                folder = file;
            } else {
                throw new MultipleFoldersFoundException("Multiple \"" + name
                        + "\" folders found");
            }
        }

        // if the folder is not found then create a new one
        if (folder == null) {
            folder = createFolderInDrive(name, inFolder);
        }
        return folder.getId();

    }

    private String uploadFileToDrive(String mediaName, String destinationFolderID,
                                     File toUpload) throws IOException {

        //adding meta-data to the file
        com.google.api.services.drive.model.File fileMetadata =
                new com.google.api.services.drive.model.File()
                        .setName(mediaName)
                        .setViewersCanCopyContent(true)
                        .setParents(Collections.singletonList(destinationFolderID));

        String type = FileUtils.getMimeType(toUpload.getPath());
        FileContent mediaContent = new FileContent(type, toUpload);
        com.google.api.services.drive.model.File file;
        file = mDriveService.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .setIgnoreDefaultVisibility(true)
                .execute();

        return file.getId();
    }

    private com.google.api.services.drive.model.File createFolderInDrive(String folderName,
                                                                         String parentId)
            throws IOException {

        //creating a new folder
        com.google.api.services.drive.model.File fileMetadata = new
                com.google.api.services.drive.model.File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        com.google.api.services.drive.model.File folder;
        folder = mDriveService.files().create(fileMetadata)
                .setFields("id")
                .execute();

        //adding the permissions to folder
        Permission sharePermission = new Permission()
                .setType("anyone")
                .setRole("reader");

        mDriveService.permissions().create(folder.getId(), sharePermission)
                .setFields("id")
                .execute();

        return folder;
    }

    private ArrayList<com.google.api.services.drive.model.File> getFilesFromDrive
            (String folderName,
             String parentId) throws IOException {

        ArrayList<com.google.api.services.drive.model.File> files = new ArrayList<>();
        FileList fileList;
        String pageToken;
        do {
            if (parentId == null) {
                fileList = mDriveService.files().list()
                        .setQ("name = '" + folderName + "' and " +
                                "mimeType = 'application/vnd.google-apps.folder'" +
                                " and trashed=false")
                        .execute();
            } else {
                fileList = mDriveService.files().list()
                        .setQ("name = '" + folderName + "' and " +
                                "mimeType = 'application/vnd.google-apps.folder'" +
                                " and '" + parentId + "' in parents" +
                                " and trashed=false")
                        .execute();
            }
            for (com.google.api.services.drive.model.File file : fileList.getFiles()) {
                files.add(file);
            }
            pageToken = fileList.getNextPageToken();
        } while (pageToken != null);
        return files;
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
        int event = parser.next();
        int depth = 0;
        int lastpush = 0;
        while (event != XmlPullParser.END_DOCUMENT) {
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
                        getPaths = false;
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
                    Log.i(TAG, "DEFAULTING: " + parser.getName() + " :: " + parser.getEventType());
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
                    Log.i(TAG, "DEFAULTING: " + parser.getName() + " :: " + parser.getEventType());
                    break;
            }
            event = parser.next();
        }
    }

    private String getPath(ArrayList<String> path) {
        String currentPath = "";
        boolean first = true;
        for (String node : path) {
            if (first) {
                currentPath = node;
                first = false;
            } else {
                currentPath += "-" + node;
            }
        }
        return currentPath;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> results) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.uploadingComplete(results);

                if (results != null && !results.isEmpty()) {
                    StringBuilder selection = new StringBuilder();
                    Set<String> keys = results.keySet();
                    Iterator<String> it = keys.iterator();

                    String[] selectionArgs = new String[keys.size() + 1];
                    int i = 0;
                    selection.append("(");
                    while (it.hasNext()) {
                        String id = it.next();
                        selection.append(InstanceColumns._ID + "=?");
                        selectionArgs[i++] = id;
                        if (i != keys.size()) {
                            selection.append(" or ");
                        }
                    }

                    selection.append(") and status=?");
                    selectionArgs[i] = InstanceProviderAPI.STATUS_SUBMITTED;

                    Cursor uploadResults = null;
                    try {
                        uploadResults = new InstancesDao().getInstancesCursor(selection.toString(),
                                selectionArgs);
                        if (uploadResults.getCount() > 0) {
                            Long[] toDelete = new Long[uploadResults.getCount()];
                            uploadResults.moveToPosition(-1);

                            int cnt = 0;
                            while (uploadResults.moveToNext()) {
                                toDelete[cnt] = uploadResults.getLong(uploadResults
                                        .getColumnIndex(InstanceColumns._ID));
                                cnt++;
                            }

                            boolean deleteFlag = PreferenceManager.getDefaultSharedPreferences(
                                    Collect.getInstance().getApplicationContext()).getBoolean(
                                    PreferenceKeys.KEY_DELETE_AFTER_SEND, false);
                            if (deleteFlag) {
                                DeleteInstancesTask dit = new DeleteInstancesTask();
                                dit.setContentResolver(Collect.getInstance().getContentResolver());
                                dit.execute(toDelete);
                            }

                        }
                    } finally {
                        if (uploadResults != null) {
                            uploadResults.close();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0], values[1]);
            }
        }
    }

    /**
     * Google sheets currently only allows a-zA-Z0-9 and dash
     */
    private boolean isValidGoogleSheetsString(String name) {
        Pattern p = Pattern
                .compile("^[a-zA-Z0-9\\-]+$");
        Matcher m = p.matcher(name);
        return m.matches();
    }

    /**
     * Fetches the spreadsheet with the provided mSpreadsheetId
     * <p>
     * get(sheetId, range) method requires two parameters
     * <p>
     * since we want to search the whole sheet so we provide only the sheet name as range
     * <p>
     * range is in A1 notation
     * eg. Sheet1!A1:G7
     * <p>
     * For more info   :   https://developers.google.com/sheets/api/reference/rest/
     *
     * @param spreadsheetId
     * @param mSpreadsheetName
     * @return
     * @throws IOException
     */
    private List<List<Object>> getHeaderFeed(String spreadsheetId, String mSpreadsheetName)
            throws IOException {
        ValueRange response = mSheetsService.spreadsheets()
                .values()
                .get(spreadsheetId, mSpreadsheetName)
                .execute();
        return response.getValues();
    }
}
