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

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.exception.BadUrlException;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import timber.log.Timber;

import static org.odk.collect.android.logic.FormController.INSTANCE_ID;

/**
 * @author carlhartung (chartung@nafundi.com)
 */
public class InstanceGoogleSheetsUploader extends InstanceUploader {

    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final String GOOGLE_DRIVE_ROOT_FOLDER = "Open Data Kit";
    public static final String GOOGLE_DRIVE_SUBFOLDER = "Submissions";

    private static final String oauth_fail = "OAUTH Error: ";
    private static final String UPLOADED_MEDIA_URL = "https://drive.google.com/open?id=";

    private final SheetsHelper sheetsHelper;
    private final DriveHelper driveHelper;
    private final GoogleAccountsManager accountsManager;
    private Outcome outcome;

    private boolean hasWritePermissionToSheet;
    private boolean authFailed;

    private String mainSheetTitle;
    private String googleSheetsUrl;
    private String spreadsheetId;
    private String id;
    private String jrFormId;

    public InstanceGoogleSheetsUploader(GoogleAccountsManager accountsManager) {
        this.accountsManager = accountsManager;
        sheetsHelper = accountsManager.getSheetsHelper();
        driveHelper = accountsManager.getDriveHelper();
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

    private void uploadInstances(String selection, String[] selectionArgs, String token, int low, int instanceCount) {
        Cursor cursor = null;
        try {
            cursor = new InstancesDao().getInstancesCursor(selection, selectionArgs);
            if (cursor.getCount() > 0) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (isCancelled()) {
                        return;
                    }
                    id = cursor.getString(cursor.getColumnIndex(InstanceColumns._ID));
                    jrFormId = cursor.getString(cursor.getColumnIndex(InstanceColumns.JR_FORM_ID));
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

                    publishProgress(cursor.getPosition() + 1 + low, instanceCount);
                    String instance = cursor.getString(cursor
                            .getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));

                    if (token == null) {
                        outcome.results.put(id, oauth_fail + Collect.getInstance().getString(R.string.invalid_oauth));
                        return;
                    } else if (!uploadOneInstance(new File(instance), formFilePath, getSpreadsheetUrl(cursor))) {
                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
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
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean uploadOneInstance(File instanceFile, String formFilePath, String spreadsheetUrl) {
        try {
            TreeElement instanceElement = getInstanceElement(formFilePath, instanceFile);
            setUpSpreadsheet(spreadsheetUrl);
            TreeElement instanceIDElement = getInstanceIDElement(getChildElements(instanceElement));
            if (hasRepeatableGroups(instanceElement)) {
                if (instanceIDElement == null) {
                    outcome.results.put(id, "This form contains repeatable group so it should contain an instanceID!");
                    return false;
                }
                createSheetsIfNeeded(instanceElement);
                handleRepeatableGroups(instanceElement, instanceIDElement);
            }
            uploadOneInstance(instanceElement, instanceFile, mainSheetTitle);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void uploadOneInstance(TreeElement element, File instanceFile, String sheetTitle) throws Exception {
        insertRow(element, instanceFile, sheetTitle);

        for (int i = 0 ; i < element.getNumChildren(); i++) {
            TreeElement child = element.getChildAt(i);
            if (child.isRepeatable()) {
                uploadOneInstance(child, instanceFile, getTitle(child));
            }
        }
    }

    private boolean insertRow(TreeElement element, File instanceFile, String sheetTitle) throws Exception {
        List<String> columnTitles = getColumnTitles(element);
        if (!isNumberOfColumnsValid(columnTitles.size())) {
            return false;
        }

        List<List<Object>> sheetCells = getSheetCells(sheetTitle);
        if (sheetCells != null && !sheetCells.isEmpty()) { // we are editing an existed sheet
            if (isAnyColumnHeaderEmpty(sheetCells.get(0))) {
                fixBlankColumnHeaders(sheetTitle, columnTitles);
                sheetCells = getSheetCells(sheetTitle); // read sheet cells again to update
            }
            if (doesMissingColumnsExist(sheetCells.get(0), columnTitles)) {
                return false;
            }
        } else { // new sheet
            resizeSheet(getSheetId(sheetTitle), columnTitles.size());
            insertRow(getRowFromObjects(new ArrayList<>(columnTitles)), sheetTitle);
            sheetCells = getSheetCells(sheetTitle); // read sheet cells again to update
        }

        insertRow(getRowFromObjects(prepareListOfValues(sheetCells.get(0), columnTitles, getAnswers(getChildElements(element), instanceFile))), sheetTitle);
        return true;
    }

    private void insertRow(ValueRange row, String sheetName) throws IOException {
        try {
            sheetsHelper.insertRow(spreadsheetId, sheetName, row);
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            throw e;
        }
    }

    private String uploadMediaFile(File instanceFile, String fileName) throws IOException, MultipleFoldersFoundException {
        String filename = instanceFile.getParentFile() + "/" + fileName;
        File toUpload = new File(filename);

        // first check the local content provider
        // to see if this photo still exists at the location or not
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = {filename};
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
            uploadedFileId = driveHelper.uploadFileToDrive(fileName, folderId, toUpload);
        } catch (IOException e) {
            Timber.e(e, "Exception thrown while uploading the file to drive");
            outcome.results.put(id, e.getMessage());
            throw e;
        }

        //checking if file was successfully uploaded
        if (uploadedFileId == null) {
            outcome.results.put(id, "Unable to upload the media files. Try again");
            return null;
        }

        return UPLOADED_MEDIA_URL + uploadedFileId;
    }

    private void handleRepeatableGroups(TreeElement element, TreeElement instanceID) {
        for (int i = 0 ; i < element.getNumChildren(); i++) {
            TreeElement childElement = element.getChildAt(i);
            if (childElement.isRepeatable()) {
                childElement.addChild(instanceID);
                handleRepeatableGroups(childElement, instanceID);
            }
        }
    }

    private TreeElement getInstanceElement(String formFilePath, File instanceFile) throws FileNotFoundException {
        FormDef formDef = XFormUtils.getFormFromInputStream(new FileInputStream(new File(formFilePath)));
        FormLoaderTask.importData(instanceFile, new FormEntryController(new FormEntryModel(formDef)));

        return formDef
                .getMainInstance()
                .getRoot();
    }

    private boolean hasRepeatableGroups(TreeElement element) {
        for (int i = 0 ; i < element.getNumChildren(); i++) {
            TreeElement childElement = element.getChildAt(i);
            if (childElement.isRepeatable()) {
                return true;
            }
        }

        return false;
    }

    private void createSheetsIfNeeded(TreeElement element) throws IOException {
        Set<String> sheetTitles = getSheetTitles(element);
        for (String sheetTitle : sheetTitles) {
            if (!doesSheetExist(sheetTitle)) {
                sheetsHelper.addSheet(spreadsheetId, sheetTitle);
            }
        }
    }

    private Set<String> getSheetTitles(TreeElement element) throws IOException {
        Set<String> sheetTitles = new HashSet<>();
        for (int i = 0 ; i < element.getNumChildren(); i++) {
            TreeElement childElement = element.getChildAt(i);
            if (childElement.isRepeatable()) {
                sheetTitles.add(getTitle(childElement));
                sheetTitles.addAll(getSheetTitles(childElement));
            }
        }

        return sheetTitles;
    }

    private HashMap<String, String> getAnswers(List<TreeElement> elements, File instanceFile) throws Exception {
        HashMap<String, String> answers = new HashMap<>();
        for (TreeElement element : elements) {
            if (element.isRepeatable()) {
                answers.put(getTitle(element), getSheetUrl(getSheetId(getTitle(element))));
            } else {
                String answer = element.getValue() != null ? element.getValue().getDisplayText() : "";
                if (new File(instanceFile.getParentFile() + "/" + answer).isFile()) {
                    answers.put(getTitle(element), uploadMediaFile(instanceFile, answer));
                } else {
                    answers.put(getTitle(element), answer);
                }
            }
        }
        return answers;
    }

    private List<String> getColumnTitles(TreeElement element) {
        List<String> columnTitles = new ArrayList<>();
        for (TreeElement child : getChildElements(element)) {
            columnTitles.add(getTitle(child));
        }

        return columnTitles;
    }

    private TreeElement getInstanceIDElement(List<TreeElement> elements) {
        for (TreeElement element : elements) {
            if (element.getName().equals(INSTANCE_ID)) {
                return element;
            }
        }
        return null;
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

    private String getTitle(AbstractTreeElement element) {
        StringBuilder elementTitle = new StringBuilder();
        while (element != null && element.getName() != null) {
            elementTitle.insert(0, element.getName() + "-");
            element = element.getParent();
        }

        return elementTitle
                .deleteCharAt(elementTitle.length() - 1)
                .toString();
    }

    private List<TreeElement> getChildElements(TreeElement element) {
        List<TreeElement> columnElements = new ArrayList<>();
        TreeElement prior = null;
        for (int i = 0; i < element.getNumChildren(); ++i) {
            TreeElement current = element.getChildAt(i);
            if ((prior != null) && (prior.getName().equals(current.getName()))) { // avoid duplicated elements
                prior = current;
            } else {
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
                            columnElements.addAll(getChildElements(current));
                        }
                        break;
                }
                prior = current;
            }
        }
        return columnElements;
    }

    private List<Object> prepareListOfValues(List<Object> columnHeaders, List<String> columnNames,
                                             HashMap<String, String> answersToUpload) {
        List<Object> list = new ArrayList<>();
        for (Object path : columnHeaders) {
            String answer = "";
            if (!path.equals(" ") && columnNames.contains(path.toString())) {
                if (answersToUpload.containsKey(path.toString())) {
                    answer = answersToUpload.get(path.toString());
                    // Check to see if answer is a location, if so, get rid of accuracy
                    // and altitude
                    // try to match a fairly specific pattern to determine
                    // if it's a location
                    // [-]#.# [-]#.# #.# #.#

                    if (isLocationValid(answer)) {
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

    private List<List<Object>> getSheetCells(String sheetTitle) throws IOException {
        List<List<Object>> sheetCells;
        try {
            sheetCells = sheetsHelper.getHeaderFeed(spreadsheetId, sheetTitle);
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            throw e;
        }

        return sheetCells;
    }

    private boolean doesMissingColumnsExist(List<Object> columnHeaders, List<String> columnTitles) {
        for (String columnTitle : columnTitles) {
            if (!columnHeaders.contains(columnTitle)) {
                outcome.results.put(id, Collect.getInstance().getString(R.string.google_sheets_missing_columns, columnTitle));
                return true;
            }
        }
        return false;
    }

    // Insert a header row again to fill empty cells
    private void fixBlankColumnHeaders(String sheetTitle, List columnTitles) throws IOException {
        List<Object> list = new ArrayList<>();
        for (Object columnTitle : columnTitles) {
            list.add(columnTitle.toString().isEmpty() ? " " : columnTitle);
        }
        insertRow(getRowFromObjects(list), sheetTitle + "!A1:1");
    }

    private boolean isAnyColumnHeaderEmpty(List columnHeaders) {
        for (Object columnHeader : columnHeaders) {
            if (columnHeader.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private ValueRange getRowFromObjects(List<Object> list) {
        return new ValueRange().setValues(Collections.singletonList(list));
    }

    private void setUpSpreadsheet(String urlString) throws IOException, BadUrlException {
        if (!hasWritePermissionToSheet || !urlString.equals(googleSheetsUrl)) {
            try {
                spreadsheetId = UrlUtils.getSpreadsheetID(urlString);
                Spreadsheet spreadsheet = sheetsHelper.getSpreadsheet(spreadsheetId);
                mainSheetTitle = spreadsheet
                        .getSheets()
                        .get(0)
                        .getProperties()
                        .getTitle();
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

    private void resizeSheet(int sheetId, int expectedSize) throws IOException {
        try {
            sheetsHelper.resizeSpreadSheet(spreadsheetId, sheetId, expectedSize);
        } catch (IOException e) {
            Timber.e(e);
            outcome.results.put(id, e.getMessage());
            throw e;
        }
    }

    private boolean isNumberOfColumnsValid(int numberOfColumns) {
        if (numberOfColumns == 0) {
            outcome.results.put(id, "No columns found in the form to upload");
            return false;
        }

        if (numberOfColumns > 255) {
            outcome.results.put(id, Collect.getInstance().getString(R.string.sheets_max_columns,
                    String.valueOf(numberOfColumns)));
            return false;
        }
        return true;
    }

    private String getSheetUrl(int sheetId) {
        return googleSheetsUrl.substring(0, googleSheetsUrl.lastIndexOf('/') + 1) + "edit#gid=" + sheetId;
    }

    private String getSpreadsheetUrl(Cursor cursor) {
        int subIdx = cursor.getColumnIndex(InstanceColumns.SUBMISSION_URI);
        String urlString = cursor.isNull(subIdx) ? null : cursor.getString(subIdx);
        // if we didn't find one in the content provider, try to get from settings
        return urlString == null
                ? (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_GOOGLE_SHEETS_URL)
                : urlString;
    }

    public static boolean isLocationValid(String answer) {
        return Pattern
                .compile("^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\"
                        + ".[0-9]+\\s[0-9]+\\.[0-9]+$")
                .matcher(answer)
                .matches();
    }

    public boolean isAuthFailed() {
        return authFailed;
    }

    public void setAuthFailedForFalse() {
        authFailed = false;
    }
}