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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Xml;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
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
import java.util.List;

import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * @author carlhartung (chartung@nafundi.com)
 */
public class InstanceGoogleSheetsUploader extends InstanceUploader {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1002;
    protected static final String GOOGLE_DRIVE_ROOT_FOLDER = "Open Data Kit";
    private static final String oauth_fail = "OAUTH Error: ";
    private static final String UPLOADED_MEDIA_URL = "https://drive.google.com/open?id=";
    private static final String GOOGLE_DRIVE_SUBFOLDER = "Submissions";


    // needed in case of rate limiting
    private static final int GOOGLE_SLEEP_TIME = 1000;
    protected Outcome outcome;
    private String spreadsheetName;
    private String spreadsheetId;
    protected com.google.api.services.sheets.v4.Sheets sheetsService = null;
    protected com.google.api.services.drive.Drive driveService = null;
    private boolean hasWritePermissonToSheet = false;
    private String spreadsheetFileName;
    private Integer sheetId;

    protected GoogleAccountCredential credential;

    private Context context;

    private boolean authFailed;

    public InstanceGoogleSheetsUploader(GoogleAccountCredential credential, Context context) {
        this.credential = credential;
        this.context = context;

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        sheetsService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("ODK-Collect")
                .build();
        driveService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("ODK-Collect")
                .build();
    }

    protected void uploadInstances(String selection, String[] selectionArgs, String token, int low, int instanceCount) {
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
            outcome.results.put(id, oauth_fail + Collect.getInstance().getString(R.string.invalid_oauth));
            return false;
        }

        // get spreadsheet id
        if (spreadsheetId == null) {
            try {
                spreadsheetId = UrlUtils.getSpreadsheetID(id);
            } catch (BadUrlException e) {
                Timber.i(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }
        }

        // checking for write permissions to the spreadsheet
        if (!hasWritePermissonToSheet) {
            try {
                spreadsheetName = getSpreadSheetName();

                //// TODO: 22/3/17 Find a better way to check the write permissions
                List<Request> requests = new ArrayList<>();
                requests.add(new Request()
                        .setUpdateSpreadsheetProperties(new UpdateSpreadsheetPropertiesRequest()
                                .setProperties(new SpreadsheetProperties()
                                        .setTitle(spreadsheetFileName))
                                .setFields("title")));

                sheetsService.spreadsheets()
                        .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest()
                                .setRequests(requests))
                        .execute();
            } catch (GoogleJsonResponseException e) {
                String message = e.getMessage();
                if (e.getDetails() != null && e.getDetails().getCode() == 403) {
                    message = Collect.getInstance().getString(R.string.google_sheets_access_denied);
                }
                outcome.results.put(id, message);
                return false;
            } catch (IOException e) {
                outcome.results.put(id, e.getMessage());
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
                    folderId = createOrGetIDOfFolderWithName(jrFormId);
                } catch (IOException | MultipleFoldersFoundException e) {
                    Timber.e(e);
                    outcome.results.put(id, e.getMessage());
                    return false;
                }

                String uploadedFileId;

                // file is ready to be uploaded
                try {
                    uploadedFileId = uploadFileToDrive(mediaToUpload.get(key),
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
            values = getHeaderFeed(spreadsheetId, spreadsheetName);
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

            //resizing the spreadsheet
            SheetProperties sheetProperties = new SheetProperties()
                    .setSheetId(sheetId)
                    .setGridProperties(new GridProperties()
                            .setColumnCount(columnNames.size()));

            List<Request> requests = new ArrayList<>();
            requests.add(new Request()
                    .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
                            .setProperties(sheetProperties)
                            .setFields("gridProperties.columnCount")));

            try {
                sheetsService.spreadsheets()
                        .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest()
                                .setRequests(requests))
                        .execute();
            } catch (IOException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }

            //adding the headers
            ArrayList<Object> list = new ArrayList<>();
            for (String column : columnNames) {
                list.add(column);
            }

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
                sheetsService.spreadsheets().values()
                        .append(spreadsheetId, spreadsheetName, row)
                        .setIncludeValuesInResponse(true)
                        .setValueInputOption("USER_ENTERED").execute();
            } catch (IOException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }
        }

        // we may have updated the feed, so get a new one
        // update the feed

        try {
            values = getHeaderFeed(spreadsheetId, spreadsheetName);
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
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, spreadsheetName + "!A1:1", row)
                        .setValueInputOption("USER_ENTERED").execute();
            } catch (IOException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                return false;
            }
        }

        // we may have updated the feed, so get a new one
        // update the feed

        try {
            values = getHeaderFeed(spreadsheetId, spreadsheetName);
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
            String missingString = "";
            for (int i = 0; i < missingColumns.size(); i++) {
                missingString += missingColumns.get(i);
                if (i < missingColumns.size() - 1) {
                    missingString += ", ";
                }
            }
            outcome.results.put(id, Collect.getInstance().getString(
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

                    if (isValidLocation(answer)) {
                        // get rid of everything after the second space
                        int firstSpace = answer.indexOf(" ");
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
            sheetsService.spreadsheets().values()
                    .append(spreadsheetId, spreadsheetName, row)
                    .setValueInputOption("USER_ENTERED").execute();
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            return false;
        }

        outcome.results.put(id, Collect.getInstance().getString(R.string.success));
        return true;
    }

    private String getSpreadSheetName() throws IOException {
        Spreadsheet response;
        response = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .setIncludeGridData(false)
                .execute();

        spreadsheetFileName = response.getProperties().getTitle();
        sheetId = response.getSheets().get(0).getProperties().getSheetId();
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
        file = driveService.files().create(fileMetadata, mediaContent)
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
        folder = driveService.files().create(fileMetadata)
                .setFields("id")
                .execute();

        //adding the permissions to folder
        Permission sharePermission = new Permission()
                .setType("anyone")
                .setRole("reader");

        driveService.permissions().create(folder.getId(), sharePermission)
                .setFields("id")
                .execute();

        return folder;
    }

    private ArrayList<com.google.api.services.drive.model.File> getFilesFromDrive(
            String folderName,
            String parentId) throws IOException {

        ArrayList<com.google.api.services.drive.model.File> files = new ArrayList<>();
        FileList fileList;
        String pageToken;
        do {
            if (parentId == null) {
                fileList = driveService.files().list()
                        .setQ("name = '" + folderName + "' and "
                                + "mimeType = 'application/vnd.google-apps.folder'"
                                + " and trashed=false")
                        .execute();
            } else {
                fileList = driveService.files().list()
                        .setQ("name = '" + folderName + "' and "
                                + "mimeType = 'application/vnd.google-apps.folder'"
                                + " and '" + parentId + "' in parents" + " and trashed=false")
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

    /**
     * Fetches the spreadsheet with the provided spreadsheetId
     * <p>
     * get(sheetId, range) method requires two parameters
     * <p>
     * since we want to search the whole sheet so we provide only the sheet name as range
     * <p>
     * range is in A1 notation
     * eg. Sheet1!A1:G7
     * <p>
     * For more info   :   https://developers.google.com/sheets/api/reference/rest/
     */
    private List<List<Object>> getHeaderFeed(String spreadsheetId, String spreadsheetName)
            throws IOException {
        ValueRange response = sheetsService.spreadsheets()
                .values()
                .get(spreadsheetId, spreadsheetName)
                .execute();
        return response.getValues();
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

                String token = credential.getToken();
                //Immediately invalidate so we get a different one if we have to try again
                GoogleAuthUtil.invalidateToken(context, token);

                getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null);
                uploadInstances(selection, selectionArgs, token, low, values.length);
                counter++;
            }
        } catch (UserRecoverableAuthException e) {
            outcome = null;
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
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

    public void setAuthFailed(boolean authFailed) {
        this.authFailed = authFailed;
    }

}


