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
import android.util.Log;
import android.util.Xml;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.util.XFormUtils;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Outcome outcome;

    private boolean hasWritePermissionToSheet;
    private boolean authFailed;

    private String mainSheetTitle;
    private String googleSheetsUrl;
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
                .compile("^[a-zA-Z0-9\\-]+$")
                .matcher(name)
                .matches();
    }

    public static boolean isValidLocation(String answer) {
        return Pattern
                .compile("^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
                        + ".[0-9]+\\s[0-9]+\\.[0-9]+$")
                .matcher(answer)
                .matches();
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
                    String jrFormId = c.getString(c.getColumnIndex(InstanceColumns.JR_FORM_ID));
                    Uri toUpdate = Uri.withAppendedPath(InstanceColumns.CONTENT_URI, id);
                    ContentValues cv = new ContentValues();

                    Cursor formCursor = new FormsDao().getFormsCursorForFormId(jrFormId);
                    String md5 = null;
                    String formFilePath = null;
                    if (formCursor.getCount() > 0) {
                        formCursor.moveToFirst();
                        md5 = formCursor
                                .getString(formCursor.getColumnIndex(FormsColumns.MD5_HASH));
                        formFilePath = formCursor.getString(formCursor
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

                    if (token == null) {
                        outcome.results.put(id, oauth_fail + Collect.getInstance().getString(R.string.invalid_oauth));
                    } else if (!uploadOneSubmission(id, new File(instance), jrFormId, formFilePath, urlString)) {
                        cv.put(InstanceColumns.STATUS,
                                InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                        return;
                    } else {
                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                        outcome.results.put(id, Collect.getInstance().getString(R.string.success));
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private boolean uploadOneSubmission(String id, File instanceFile, String jrFormId,
                                        String formFilePath, String urlString) {
        try {
            checkWritePermissions(id, urlString);

            FormDef formDefFromXml = XFormUtils.getFormFromInputStream(new FileInputStream(new File(formFilePath)));

            List<TreeElement> mainLevelColumnElements = getColumnElements(formDefFromXml.getMainInstance().getRoot());
            List<List<TreeElement>> repeatColumnElements = new ArrayList<>();
            List<String> repeatSheetTitles = new ArrayList<>();
            for (TreeElement mainLevelColumnElement : mainLevelColumnElements) {
                if (mainLevelColumnElement.isRepeatable()) {
                    List<TreeElement> elements = getColumnElements(mainLevelColumnElement);
                    elements.add(0, mainLevelColumnElements.get(0));
                    repeatColumnElements.add(elements);
                    repeatSheetTitles.add(getElementTitle(mainLevelColumnElement));
                }
            }

            if (!repeatColumnElements.isEmpty()) {
                for (List<TreeElement> repeatGroup : repeatColumnElements) {
                    if (repeatGroup.size() > 1) {
                        String sheetTitle = getElementTitle(repeatGroup.get(1).getParent());
                        if (!doesSheetExist(sheetTitle)) {
                            sheetsHelper.addSheet(spreadsheetId, sheetTitle);
                        }
                        fillSheet(repeatGroup, sheetTitle, id, instanceFile, jrFormId, null);
                    }
                }
            }

            fillSheet(mainLevelColumnElements, mainSheetTitle, id, instanceFile, jrFormId, repeatSheetTitles);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void fillSheet(List<TreeElement> columnElements, String sheetTitle,
                           String id, File instanceFile, String jrFormId, List<String> repeatSheetTitles) throws Exception {
        List<String> columnTitles = new ArrayList<>();
        Multimap<String, String> answersToUpload = ArrayListMultimap.create();
        Multimap<String, String> mediaToUpload = ArrayListMultimap.create();
        Multimap<String, String> uploadedMedia = ArrayListMultimap.create();
        List<List<Object>> sheetCells = new ArrayList<>();
        List headerRow;
        List<String> sheetColumns = new ArrayList<>();

        readColumnNames(columnTitles, columnElements);
        validColumnLength(columnTitles, id);
        validColumnNames(columnTitles, id);
        readAnswers(columnTitles, instanceFile, answersToUpload, mediaToUpload, id, repeatSheetTitles);
        sleepThread();
        if (!mediaToUpload.isEmpty()) {
            uploadMedia(mediaToUpload, instanceFile, jrFormId, id, uploadedMedia);
        }
        readSheetCells(sheetTitle, sheetCells, id);
        if (!sheetCells.isEmpty()) {
            headerRow = sheetCells.get(0);
        } else { // new sheet
            resizeSheet(getSheetId(sheetTitle), columnTitles.size(), id);
            addHeaders(sheetTitle, columnTitles, id);
            readSheetCells(sheetTitle, sheetCells, id); // read sheet cells again to update
            headerRow = sheetCells.get(0);
        }
        if (isAnyColumnEmpty(headerRow)) {
            fixBlankColumnNames(sheetTitle, headerRow, id);
            readSheetCells(sheetTitle, sheetCells, id); // read sheet cells again to update
            headerRow = sheetCells.get(0);
        }
        getSheetColumns(sheetColumns, headerRow, id);
        checkForMissingColumns(sheetColumns, columnTitles, id);
        addPhotos(answersToUpload, uploadedMedia);

        Collection<String> values = answersToUpload.get(columnTitles.get(1));
        for (int i = 0; i < values.size(); i++) {
            HashMap<String, String> answers = new HashMap<>();
            for (int j = 0; j < answersToUpload.asMap().size(); j++) {
                answers.put(columnTitles.get(j), Iterables.get(answersToUpload.get(columnTitles.get(j)), j == 0 ? 0 : i));
            }
            insertRow(getRowFromList(prepareListOfValues(sheetColumns, columnTitles, answers)), id, sheetTitle);
        }
    }

    private void readColumnNames(List<String> columnNames, List<TreeElement> elements) {
        for (TreeElement element : elements) {
            columnNames.add(getElementTitle(element));
        }
    }

    private boolean doesSheetExist(String sheetTitle) throws IOException {
        for (Sheet sheet : sheetsHelper.getSpreadsheet(spreadsheetId).getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetTitle)) {
                return true;
            }
        }
        return false;
    }

    private Integer getSheetId(String sheetTitle) throws Exception {
        for (Sheet sheet : sheetsHelper.getSpreadsheet(spreadsheetId).getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetTitle)) {
                return sheet
                        .getProperties()
                        .getSheetId();
            }
        }
        return null;
    }

    private String getElementTitle(AbstractTreeElement element) {
        StringBuilder elementTitle = new StringBuilder();
        while (element!= null && element.getName() != null) {
            elementTitle.insert(0, element.getName() + "-");
            element = element.getParent();
        }

        return elementTitle
                .deleteCharAt(elementTitle.length() - 1)
                .toString();
    }

    private List<TreeElement> getColumnElements(TreeElement element) {
        List<TreeElement> columnElements = new ArrayList<>();
        for (int i = 0; i < element.getNumChildren(); ++i) {
            TreeElement current = element.getChildAt(i);
            switch (current.getDataType()) {
                case org.javarosa.core.model.Constants.DATATYPE_TEXT:
                case org.javarosa.core.model.Constants.DATATYPE_INTEGER:
                case org.javarosa.core.model.Constants.DATATYPE_DECIMAL:
                case org.javarosa.core.model.Constants.DATATYPE_DATE:
                case org.javarosa.core.model.Constants.DATATYPE_TIME:
                case org.javarosa.core.model.Constants.DATATYPE_DATE_TIME:
                case org.javarosa.core.model.Constants.DATATYPE_CHOICE:
                case org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST:
                case org.javarosa.core.model.Constants.DATATYPE_BOOLEAN:
                case org.javarosa.core.model.Constants.DATATYPE_GEOPOINT:
                case org.javarosa.core.model.Constants.DATATYPE_BARCODE:
                case org.javarosa.core.model.Constants.DATATYPE_BINARY:
                case org.javarosa.core.model.Constants.DATATYPE_LONG:
                case org.javarosa.core.model.Constants.DATATYPE_GEOSHAPE:
                case org.javarosa.core.model.Constants.DATATYPE_GEOTRACE:
                case org.javarosa.core.model.Constants.DATATYPE_UNSUPPORTED:
                    columnElements.add(current);
                    break;
                case org.javarosa.core.model.Constants.DATATYPE_NULL:
                    if (current.isRepeatable()) { // repeat group
                        columnElements.add(current);
                    } else if (current.getNumChildren() == 0) { // assume fields that don't have children are string fields
                        columnElements.add(current);
                    } else { // one or more children - this is a group
                        columnElements.addAll(getColumnElements(current));
                    }
                    break;
            }
        }
        return columnElements;
    }

    private void sleepThread() {
        try {
            Thread.sleep(GOOGLE_SLEEP_TIME);
        } catch (InterruptedException e3) {
            Timber.d(e3);
        }
    }

    private List<Object> prepareListOfValues(List<String> sheetCols, List<String> columnNames,
                                             HashMap<String, String> answersToUpload) {
        List<Object> list = new ArrayList<>();
        for (String path : sheetCols) {
            String answer = "";
            if (!path.equals(" ") && columnNames.contains(path)) {
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

        return list;
    }

    private void getSheetColumns(List<String> sheetCols, List headerFeed, String id) throws Exception {
        if (headerFeed != null) {
            for (Object column : headerFeed) {
                sheetCols.add(column.toString());
            }
        } else {
            outcome.results.put(id, "couldn't get header feed");
            throw new Exception();
        }
    }

    private void readSheetCells(String sheetTitle, List<List<Object>> values, String id) throws IOException {
        try {
            values.clear();
            List<List<Object>> headerFeed = sheetsHelper.getHeaderFeed(spreadsheetId, sheetTitle);
            if (headerFeed != null && !headerFeed.isEmpty()) {
                values.addAll(headerFeed);
            }
            if (values.isEmpty()) {
                outcome.results.put(id, "No data found");
            }
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            throw e;
        }
    }

    private void addPhotos(Multimap<String, String> answersToUpload, Multimap<String, String> uploadedMedia) {
        for (Map.Entry entry : uploadedMedia.entries()) {
            answersToUpload.put(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    private void checkForMissingColumns(List<String> sheetCols, List<String> columnNames, String id) throws Exception {
        for (String col : columnNames) {
            if (!sheetCols.contains(col)) {
                outcome.results.put(id, Collect.getInstance().getString(R.string.google_sheets_missing_columns, col));
                throw new Exception();
            }
        }
    }

    private void fixBlankColumnNames(String sheetTitle, List headerFeed, String id) throws IOException {
        List<Object> list = new ArrayList<>();
        for (Object column : headerFeed) {
            if (column.equals("")) {
                list.add(" ");
            } else {
                list.add(column);
            }
        }
        insertRow(new ValueRange().setValues(Collections.singletonList(list)), id, sheetTitle + "!A1:1");
    }

    private boolean isAnyColumnEmpty(List headerFeed) {
        for (Object column : headerFeed) {
            if (column.equals("")) {
                return true;
            }
        }
        return false;
    }

    private ValueRange getRowFromList(List<Object> list) {
        return new ValueRange()
                .setValues(Collections.singletonList(list));
    }

    private void insertRow(ValueRange row, String id, String sheetName) throws IOException {
        try {
            sheetsHelper.insertRow(spreadsheetId, sheetName, row);
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            throw e;
        }
    }

    private void validColumnNames(List<String> columnNames, String id) throws Exception {
        for (String columnName : columnNames) {
            if (!isValidGoogleSheetsString(columnName)) {
                outcome.results.put(id,
                        Collect.getInstance().getString(R.string.google_sheets_invalid_column_form,
                                columnName));
                throw new Exception();
            }
        }
    }

    private void checkWritePermissions(String id, String urlString) throws IOException, BadUrlException {
        if (!hasWritePermissionToSheet || !urlString.equals(googleSheetsUrl)) {
            try {
                spreadsheetId = UrlUtils.getSpreadsheetID(urlString);
                Spreadsheet spreadsheet = sheetsHelper.getSpreadsheet(spreadsheetId);
                mainSheetTitle = spreadsheet.getSheets().get(0).getProperties().getTitle();
            } catch (GoogleJsonResponseException e) {
                String message = e.getMessage();
                if (e.getDetails() != null && e.getDetails().getCode() == 403) {
                    message = Collect.getInstance().getString(R.string.google_sheets_access_denied);
                }
                outcome.results.put(id, message);
                throw e;
            } catch (BadUrlException | IOException e) {
                Timber.i(e);
                outcome.results.put(id, e.getMessage());
                throw e;
            }
            hasWritePermissionToSheet = true;
            googleSheetsUrl = urlString;
        }
    }

    /*  # NOTE #
    *  Media files are uploaded to Google Drive of user
    *  All media files are currently saved under folder "Open Data Kit/Submissions/formID/"
    */

    // if we have any media files to upload,
    // get the folder or create a new one
    // then upload the media files
    private void uploadMedia(Multimap<String, String> mediaToUpload, File instanceFile,
                             String jrFormId, String id, Multimap<String, String> uploadedMedia) throws Exception {
        for (Map.Entry entry : mediaToUpload.entries()) {
            String filename = instanceFile.getParentFile() + "/" + entry.getValue();
            File toUpload = new File(filename);

            // first check the local content provider
            // to see if this photo still exists at the location or not
            String selection = MediaStore.Images.Media.DATA + "=?";
            String[] selectionArgs = {
                    filename
            };
            Cursor cursor = Collect.getInstance().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, selection,
                            selectionArgs, null);
            if (cursor != null && cursor.getCount() != 1) {
                cursor.close();
                try {
                    throw new FileNotFoundException(Collect.getInstance()
                            .getString(R.string.media_upload_error, filename));
                } catch (FileNotFoundException e) {
                    Timber.e(e);
                }
            }

            String folderId;
            try {
                folderId = driveHelper.createOrGetIDOfFolderWithName(jrFormId);
            } catch (IOException | MultipleFoldersFoundException e) {
                Timber.e(e);
                outcome.results.put(id, e.getMessage());
                throw e;
            }

            String uploadedFileId;

            // file is ready to be uploaded
            try {
                uploadedFileId = driveHelper.uploadFileToDrive(entry.getValue().toString(),
                        folderId, toUpload);
            } catch (IOException e) {
                Timber.e(e, "Exception thrown while uploading the file to drive");
                outcome.results.put(id, e.getMessage());
                throw e;
            }

            //checking if file was successfully uploaded
            if (uploadedFileId == null) {
                outcome.results.put(id, "Unable to upload the media files. Try again");
                throw new Exception();
            }

            // uploadedPhotos keeps track of the uploaded URL
            uploadedMedia.put(entry.getKey().toString(), UPLOADED_MEDIA_URL + uploadedFileId);
        }
    }

    private void resizeSheet(int sheetId, int size, String id) throws IOException {
        try {
            sheetsHelper.resizeSpreadSheet(spreadsheetId, sheetId, size);
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            throw e;
        }
    }

    private void addHeaders(String sheetName, List<String> columnNames, String id) throws IOException {
        List<Object> list = new ArrayList<>();
        list.addAll(columnNames);

        List<List<Object>> content = new ArrayList<>();
        content.add(list);

        /*
         *  Appends the data at the last row
         *
         *  append(spreadsheetId, range, ValueRange)
         *
         *  spreadsheetId   :   Unique sheet id
         *  range           :   A1 notation range. It specifies the range within which the
         *                      spreadsheet should be searched.
         *              hint   "Giving only mainSheetTitle in range searches in the complete sheet"
         *  ValueRange      :   Content that needs to be appended  (List<List<Object>>)
         *
         *  For more info   :   https://developers.google.com/sheets/api/reference/rest/
         */
        insertRow(new ValueRange().setValues(content), id, sheetName);
    }

    private void validColumnLength(List<String> columnNames, String id) throws Exception {
        if (columnNames.size() == 0) {
            outcome.results.put(id, "No columns found in the form to upload");
            throw new Exception();
        }

        if (columnNames.size() > 255) {
            outcome.results.put(id, Collect.getInstance().getString(R.string.sheets_max_columns,
                    String.valueOf(columnNames.size())));
            throw new Exception();
        }
    }

    private void readAnswers(List<String> columnTitles, File instanceFile, Multimap<String, String> answersToUpload,
                             Multimap<String, String> mediaToUpload, String id, List<String> repeatSheetTitles) throws Exception {
        try {
            processInstanceXML(columnTitles, instanceFile, answersToUpload, mediaToUpload, repeatSheetTitles);
        } catch (FormException e) {
            outcome.results.put(id, Collect.getInstance().getString(R.string.google_repeat_error));
            throw e;
        } catch (Exception e) {
            Timber.e(e, "Exception thrown while parsing the file");
            outcome.results.put(id, e.getMessage());
            throw e;
        }
    }

    private void processInstanceXML(List<String> columnTitles, File instanceFile,
                                    Multimap<String, String> answersToUpload,
                                    Multimap<String, String> mediaToUpload,
                                    List<String> repeatSheetTitles) throws Exception {

        FileInputStream in = new FileInputStream(instanceFile);
        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        readInstanceFeed(columnTitles, parser, answersToUpload, mediaToUpload, instanceFile.getParentFile(), repeatSheetTitles);
        in.close();
    }

    private void readInstanceFeed(List<String> columnTitles, XmlPullParser parser,
                                  Multimap<String, String> answersToUpload,
                                  Multimap<String, String> mediaToUpload, File instanceFolder,
                                  List<String> repeatSheetTitles) throws Exception {

        List<String> path = new ArrayList<>();
        int event = parser.next();
        boolean emptyAnswer = false;
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_TAG:
                    emptyAnswer = true;
                    path.add(parser.getName());
                    String columnTitle = getPath(path);
                    if (repeatSheetTitles != null && repeatSheetTitles.contains(columnTitle)) { // that means it's a repeat group
                        Integer sheetId = getSheetId(columnTitle);
                        if (sheetId != null && !answersToUpload.values().contains(getSheetUrl(sheetId))) {
                            emptyAnswer = false;
                            answersToUpload.put(columnTitle, getSheetUrl(sheetId));
                        }
                    }
                    break;
                case XmlPullParser.TEXT:
                    emptyAnswer = false;
                    columnTitle = getPath(path);
                    if (columnTitles.contains(columnTitle)) {
                        String answer = parser.getText();
                        if (new File(instanceFolder + "/" + answer).isFile()) {
                            mediaToUpload.put(columnTitle, answer);
                        } else {
                            answersToUpload.put(columnTitle, answer);
                        }
                    }

                    break;
                case XmlPullParser.END_TAG:
                    columnTitle = getPath(path);
                    if (emptyAnswer && columnTitles.contains(columnTitle)
                            && (repeatSheetTitles == null || !repeatSheetTitles.contains(columnTitle))) {
                        answersToUpload.put(columnTitle, "");
                    }
                    path.remove(path.size() - 1);
                    break;
                default:
                    Timber.i("DEFAULTING: %s :: %d", parser.getName(), parser.getEventType());
                    break;
            }
            event = parser.next();
        }
    }

    private String getSheetUrl(int sheetId) throws Exception {
        return googleSheetsUrl.substring(0,
                googleSheetsUrl.lastIndexOf("/") + 1) + "edit#gid=" + sheetId;
    }

    private String getPath(List<String> path) {
        StringBuilder currentPath = new StringBuilder();
        for (String node : path) {
            if (path.indexOf(node) == 0) {
                currentPath = new StringBuilder(node);
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

    public void setAuthFailedForFalse() {
        authFailed = false;
    }
}