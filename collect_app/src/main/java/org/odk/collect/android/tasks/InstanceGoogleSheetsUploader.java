/*
 * Copyright (C) 2018 Nafundi
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
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
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
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.exception.BadUrlException;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.UrlUtils;
import org.odk.collect.android.utilities.gdrive.DriveHelper;
import org.odk.collect.android.utilities.gdrive.GoogleAccountsManager;
import org.odk.collect.android.utilities.gdrive.SheetsHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import timber.log.Timber;

import static org.odk.collect.android.logic.FormController.INSTANCE_ID;
import static org.odk.collect.android.utilities.InstanceUploaderUtils.DEFAULT_SUCCESSFUL_TEXT;

/**
 * @author carlhartung (chartung@nafundi.com)
 */
public class InstanceGoogleSheetsUploader extends InstanceUploader {

    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final String GOOGLE_DRIVE_ROOT_FOLDER = "Open Data Kit";
    public static final String GOOGLE_DRIVE_SUBFOLDER = "Submissions";
    private static final String PARENT_KEY = "PARENT_KEY";
    private static final String KEY = "KEY";

    private static final String UPLOADED_MEDIA_URL = "https://drive.google.com/open?id=";

    private final SheetsHelper sheetsHelper;
    private final DriveHelper driveHelper;
    private final GoogleAccountsManager accountsManager;

    private static final String ALTITUDE_TITLE_POSTFIX = "-altitude";
    private static final String ACCURACY_TITLE_POSTFIX = "-accuracy";

    private boolean authFailed;

    private String jrFormId;

    private Spreadsheet spreadsheet;

    public InstanceGoogleSheetsUploader(GoogleAccountsManager accountsManager) {
        this.accountsManager = accountsManager;
        sheetsHelper = accountsManager.getSheetsHelper();
        driveHelper = accountsManager.getDriveHelper();
    }

    @Override
    protected Outcome doInBackground(Long... values) {
        final Outcome outcome = new Outcome();
        int counter = 0;

        try {
            while (counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER < values.length) {
                String token = accountsManager.getCredential().getToken();

                //Immediately invalidate so we get a different one if we have to try again
                GoogleAuthUtil.invalidateToken(accountsManager.getContext(), token);

                // check if root folder exists, if not then create one
                driveHelper.getIDOfFolderWithName(GOOGLE_DRIVE_ROOT_FOLDER, null, true);

                int low = counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
                int high = (counter + 1) * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER;
                if (high > values.length) {
                    high = values.length;
                }

                StringBuilder selectionBuf = new StringBuilder(InstanceColumns._ID + " IN (");
                String[] selectionArgs = new String[high - low];
                for (int i = 0; i < (high - low); i++) {
                    if (i > 0) {
                        selectionBuf.append(',');
                    }
                    selectionBuf.append('?');
                    selectionArgs[i] = values[i + low].toString();
                }

                selectionBuf.append(')');

                outcome.messagesByInstanceId.putAll(uploadInstances(selectionBuf.toString(), selectionArgs, low, values.length));
                counter++;
            }
        } catch (UserRecoverableAuthException e) {
            Activity activity = accountsManager.getActivity();
            if (activity != null) {
                activity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }
            return null;
        } catch (IOException | GoogleAuthException e) {
            Timber.e(e);
            authFailed = true;
        } catch (MultipleFoldersFoundException e) {
            Timber.e(e);
        }
        return outcome;
    }

    private Map<String, String> uploadInstances(String selection, String[] selectionArgs, int low, int instanceCount) {
        final Map<String, String> messagesByInstanceId = new HashMap<>();

        try (Cursor cursor = new InstancesDao().getInstancesCursor(selection, selectionArgs)) {
            if (cursor.getCount() > 0) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (isCancelled()) {
                        return messagesByInstanceId;
                    }
                    final String id = cursor.getString(cursor.getColumnIndex(InstanceColumns._ID));
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
                        return messagesByInstanceId;
                    }

                    publishProgress(cursor.getPosition() + 1 + low, instanceCount);
                    String instance = cursor.getString(cursor
                            .getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));

                    try {
                        uploadOneInstance(new File(instance), formFilePath, getGoogleSheetsUrl(cursor));
                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMITTED);
                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                        messagesByInstanceId.put(id, DEFAULT_SUCCESSFUL_TEXT);

                        Collect.getInstance()
                                .getDefaultTracker()
                                .send(new HitBuilders.EventBuilder()
                                        .setCategory("Submission")
                                        .setAction("HTTP-Sheets")
                                        .build());
                    } catch (UploadException e) {
                        Timber.e(e);
                        messagesByInstanceId.put(id, e.getMessage() != null ? e.getMessage() : e.getCause().getMessage());
                        cv.put(InstanceColumns.STATUS, InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
                        Collect.getInstance().getContentResolver().update(toUpdate, cv, null, null);
                    }
                }
            }
        }
        return messagesByInstanceId;
    }

    private void uploadOneInstance(File instanceFile, String formFilePath, String spreadsheetUrl) throws UploadException {
        TreeElement instanceElement = getInstanceElement(formFilePath, instanceFile);
        setUpSpreadsheet(spreadsheetUrl);
        if (hasRepeatableGroups(instanceElement)) {
            createSheetsIfNeeded(instanceElement);
        }
        String key = getInstanceID(getChildElements(instanceElement));
        if (key == null) {
            key = PropertyUtils.genUUID();
        }
        insertRows(instanceElement, null, key, instanceFile, spreadsheet.getSheets().get(0).getProperties().getTitle());
    }

    private void insertRows(TreeElement element, String parentKey, String key, File instanceFile, String sheetTitle)
            throws UploadException {
        insertRow(element, parentKey, key, instanceFile, sheetTitle);

        int repeatIndex = 0;
        for (int i = 0 ; i < element.getNumChildren(); i++) {
            TreeElement child = element.getChildAt(i);
            if (child.isRepeatable() && child.getMultiplicity() != TreeReference.INDEX_TEMPLATE) {
                insertRows(child, key, getKeyBasedOnParentKey(key, child.getName(), repeatIndex++), instanceFile, getElementTitle(child));
            }
            if (child.getMultiplicity() == TreeReference.INDEX_TEMPLATE) {
                repeatIndex = 0;
            }
        }
    }

    private String getKeyBasedOnParentKey(String parentKey, String groupName, int repeatIndex) {
        return parentKey
                + "/"
                + groupName
                + "[" + (repeatIndex + 1) + "]";
    }

    private void insertRow(TreeElement element, String parentKey, String key, File instanceFile, String sheetTitle)
            throws UploadException {

        if (isCancelled()) {
            throw new UploadException(Collect.getInstance().getString(R.string.instance_upload_cancelled));
        }

        try {
            List<List<Object>> sheetCells = getSheetCells(sheetTitle);
            boolean newSheet = sheetCells == null || sheetCells.isEmpty();
            List<Object> columnTitles = getColumnTitles(element, newSheet);
            ensureNumberOfColumnsIsValid(columnTitles.size());

            if (!newSheet) { // we are editing an existed sheet
                if (isAnyColumnHeaderEmpty(sheetCells.get(0))) {
                    // Insert a header row again to fill empty headers
                    sheetsHelper.updateRow(spreadsheet.getSpreadsheetId(), sheetTitle + "!A1",
                            new ValueRange().setValues(Collections.singletonList(columnTitles)));
                    sheetCells = getSheetCells(sheetTitle); // read sheet cells again to update
                }
                disallowMissingColumns(sheetCells.get(0), columnTitles);
                addAltitudeAndAccuracyTitles(sheetCells.get(0), columnTitles);
                ensureNumberOfColumnsIsValid(columnTitles.size());  // Call again to ensure valid number of columns

            } else { // new sheet
                Integer sheetId = getSheetId(sheetTitle);
                if (sheetId != null) {
                    sheetsHelper.resizeSpreadSheet(spreadsheet.getSpreadsheetId(), sheetId, columnTitles.size());
                }
                sheetsHelper.insertRow(spreadsheet.getSpreadsheetId(), sheetTitle,
                        new ValueRange().setValues(Collections.singletonList(columnTitles)));
                sheetCells = getSheetCells(sheetTitle); // read sheet cells again to update
            }

            HashMap<String, String> answers = getAnswers(element, columnTitles, instanceFile, parentKey, key);

            if (isCancelled()) {
                throw new UploadException(Collect.getInstance().getString(R.string.instance_upload_cancelled));
            }

            if (shouldRowBeInserted(answers)) {
                sheetsHelper.insertRow(spreadsheet.getSpreadsheetId(), sheetTitle,
                        new ValueRange().setValues(Collections.singletonList(prepareListOfValues(sheetCells.get(0), columnTitles, answers))));
            }
        } catch (IOException e) {
            throw new UploadException(e);
        }
    }

    /**
     * Adds titles ending with "-altitude" or "-accuracy" if they have been manually added to the
     * Sheet. Existing spreadsheets can start collecting altitude / accuracy from
     * Geo location fields.
     *
     * @param sheetHeaders - Headers from the spreadsheet
     * @param columnTitles - Column titles list to be updated with altitude / accuracy titles from
     *                       the sheetHeaders
     */
    private void addAltitudeAndAccuracyTitles(List<Object> sheetHeaders, List<Object> columnTitles) {
        for (Object sheetTitle : sheetHeaders) {
            String sheetTitleStr = (String) sheetTitle;
            if (sheetTitleStr.endsWith(ALTITUDE_TITLE_POSTFIX) || sheetTitleStr.endsWith(ACCURACY_TITLE_POSTFIX)) {
                if (!columnTitles.contains(sheetTitleStr)) {
                    columnTitles.add(sheetTitleStr);
                }
            }
        }
    }


    // Ignore rows with all empty answers added by a user and extra repeatable groups added
    // by Javarosa https://github.com/opendatakit/javarosa/issues/266
    private boolean shouldRowBeInserted(HashMap<String, String> answers) {
        for (String answer : answers.values()) {
            if (answer != null && !answer.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String uploadMediaFile(File instanceFile, String fileName) throws UploadException {
        String filePath = instanceFile.getParentFile() + "/" + fileName;
        File toUpload = new File(filePath);

        if (!new File(filePath).exists()) {
            throw new UploadException(Collect.getInstance()
                    .getString(R.string.media_upload_error, filePath));
        }

        String folderId;
        try {
            folderId = driveHelper.createOrGetIDOfFolderWithName(jrFormId);
        } catch (IOException | MultipleFoldersFoundException e) {
            Timber.e(e);
            throw new UploadException(e.getMessage());
        }

        String uploadedFileId;

        // file is ready to be uploaded
        try {
            uploadedFileId = driveHelper.uploadFileToDrive(filePath, folderId, toUpload);
        } catch (IOException e) {
            Timber.e(e, "Exception thrown while uploading the file to drive");
            throw new UploadException(e.getMessage());
        }

        // checking if file was successfully uploaded
        if (uploadedFileId == null) {
            throw new UploadException("Unable to upload the media files. Try again");
        }
        return UPLOADED_MEDIA_URL + uploadedFileId;
    }

    private TreeElement getInstanceElement(String formFilePath, File instanceFile) throws UploadException {
        FormDef formDef;
        try {
            formDef = XFormUtils.getFormFromInputStream(new FileInputStream(new File(formFilePath)));
        } catch (FileNotFoundException e) {
            throw new UploadException(e);
        }
        FormLoaderTask.importData(instanceFile, new FormEntryController(new FormEntryModel(formDef)));
        return formDef.getMainInstance().getRoot();
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

    private void createSheetsIfNeeded(TreeElement element) throws UploadException {
        Set<String> sheetTitles = getSheetTitles(element);

        try {
            for (String sheetTitle : sheetTitles) {
                if (!doesSheetExist(sheetTitle)) {
                    sheetsHelper.addSheet(spreadsheet.getSpreadsheetId(), sheetTitle);
                }
            }
            spreadsheet = sheetsHelper.getSpreadsheet(spreadsheet.getSpreadsheetId());
        } catch (IOException e) {
            throw new UploadException(e);
        }
    }

    private Set<String> getSheetTitles(TreeElement element) {
        Set<String> sheetTitles = new HashSet<>();
        for (int i = 0 ; i < element.getNumChildren(); i++) {
            TreeElement childElement = element.getChildAt(i);
            if (childElement.isRepeatable()) {
                sheetTitles.add(getElementTitle(childElement));
                sheetTitles.addAll(getSheetTitles(childElement));
            }
        }
        return sheetTitles;
    }

    private HashMap<String, String> getAnswers(TreeElement element, List<Object> columnTitles, File instanceFile, String parentKey, String key)
            throws UploadException {
        HashMap<String, String> answers = new HashMap<>();
        for (TreeElement childElement : getChildElements(element)) {
            String elementTitle = getElementTitle(childElement);
            if (childElement.isRepeatable()) {
                answers.put(elementTitle, getHyperlink(getSheetUrl(getSheetId(elementTitle)), elementTitle));
            } else {
                String answer = childElement.getValue() != null ? childElement.getValue().getDisplayText() : "";
                if (new File(instanceFile.getParentFile() + "/" + answer).isFile()) {
                    String mediaUrl = uploadMediaFile(instanceFile, answer);
                    answers.put(elementTitle, mediaUrl);
                } else {
                    if (isLocationValid(answer)) {
                        answers.putAll(parseGeopoint(columnTitles, elementTitle, answer));
                    } else {
                        answers.put(elementTitle, answer);
                    }
                }
            }
        }
        if (element.isRepeatable()) {
            answers.put(PARENT_KEY, parentKey);
            answers.put(KEY, key);
        } else if (hasRepeatableGroups(element)) {
            answers.put(KEY, key);
        }
        return answers;
    }

    /**
     * Strips the Altitude and Accuracy from a location String and adds them as separate columns if
     * the column titles exist.
     *
     * @param columnTitles - A List of column titles on the sheet
     * @param elementTitle - The title of the geo data to parse. e.g. "data-Point"
     * @param geoData - A space (" ") separated string that contains "Lat Long Altitude Accuracy"
     * @return a Map of fields containing Lat/Long and Accuracy, Altitude (if the respective column
     *         titles exist in the columnTitles parameter).
     */
    private @NonNull Map<String, String> parseGeopoint(@NonNull List<Object> columnTitles, @NonNull String elementTitle, @NonNull String geoData) {
        Map<String, String> geoFieldsMap = new HashMap<String, String>();

        // Accuracy
        int accuracyLocation = geoData.lastIndexOf(' ');
        String accuracyStr = geoData.substring(accuracyLocation).trim();
        geoData = geoData.substring(0, accuracyLocation).trim();
        final String accuracyTitle = elementTitle + ACCURACY_TITLE_POSTFIX;
        if (columnTitles.contains(accuracyTitle)) {
            geoFieldsMap.put(accuracyTitle, accuracyStr);
        }

        // Altitude
        int altitudeLocation = geoData.lastIndexOf(' ');
        String altitudeStr = geoData.substring(altitudeLocation).trim();
        geoData = geoData.substring(0, altitudeLocation).trim();
        final String altitudeTitle = elementTitle + ALTITUDE_TITLE_POSTFIX;
        if (columnTitles.contains(altitudeTitle)) {
            geoFieldsMap.put(altitudeTitle, altitudeStr);
        }

        geoData = geoData.replace(' ', ',');

        // Put the modified geo location (Just lat/long) into the geo fields Map
        geoFieldsMap.put(elementTitle, geoData);

        return geoFieldsMap;
    }

    private List<Object> getColumnTitles(TreeElement element, boolean newSheet) {
        List<Object> columnTitles = new ArrayList<>();
        for (TreeElement child : getChildElements(element)) {
            final String elementTitle = getElementTitle(child);
            columnTitles.add(elementTitle);
            if (newSheet && child.getDataType() == org.javarosa.core.model.Constants.DATATYPE_GEOPOINT) {
                columnTitles.add(elementTitle + ALTITUDE_TITLE_POSTFIX);
                columnTitles.add(elementTitle + ACCURACY_TITLE_POSTFIX);
            }
        }
        if (element.isRepeatable()) {
            columnTitles.add(PARENT_KEY);
            columnTitles.add(KEY);
        } else if (hasRepeatableGroups(element)) {
            columnTitles.add(KEY);
        }
        return columnTitles;
    }

    private String getInstanceID(List<TreeElement> elements) {
        for (TreeElement element : elements) {
            if (element.getName().equals(INSTANCE_ID)) {
                return element.getValue() != null ? element.getValue().getDisplayText() : null;
            }
        }
        return null;
    }

    private boolean doesSheetExist(String sheetTitle) {
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetTitle)) {
                return true;
            }
        }
        return false;
    }

    private void disallowMissingColumns(List<Object> columnHeaders, List<Object> columnTitles) throws UploadException {
        for (Object columnTitle : columnTitles) {
            if (!columnHeaders.contains(columnTitle)) {
                throw new UploadException(Collect.getInstance().getString(R.string.google_sheets_missing_columns, columnTitle));
            }
        }
    }

    /** This method builds a column name by joining all of the containing group names using "-" as a separator */
    private String getElementTitle(AbstractTreeElement element) {
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
        List<TreeElement> elements = new ArrayList<>();
        TreeElement prior = null;
        for (int i = 0; i < element.getNumChildren(); ++i) {
            TreeElement current = element.getChildAt(i);
            // avoid duplicated elements https://github.com/opendatakit/javarosa/issues/248
            if ((prior != null) && (prior.getName().equals(current.getName()))) {
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
                        elements.add(current);
                        break;
                    case org.javarosa.core.model.Constants.DATATYPE_NULL:
                        if (current.isRepeatable()) { // repeat group
                            elements.add(current);
                        } else if (current.getNumChildren() == 0) { // assume fields that don't have children are string fields
                            elements.add(current);
                        } else { // one or more children - this is a group
                            elements.addAll(getChildElements(current));
                        }
                        break;
                }
                prior = current;
            }
        }
        return elements;
    }

    private List<Object> prepareListOfValues(List<Object> columnHeaders, List<Object> columnTitles,
                                             HashMap<String, String> answers) {
        List<Object> list = new ArrayList<>();
        for (Object path : columnHeaders) {
            String answer = "";
            if (!path.equals(" ") && columnTitles.contains(path.toString())) {
                if (answers.containsKey(path.toString())) {
                    answer = answers.get(path.toString());
                }
            }
            // https://github.com/opendatakit/collect/issues/931
            list.add(answer.isEmpty() ? " " : answer);
        }
        return list;
    }

    private List<List<Object>> getSheetCells(String sheetTitle) throws IOException {
        return sheetsHelper.getSheetCells(spreadsheet.getSpreadsheetId(), sheetTitle);
    }

    private boolean isAnyColumnHeaderEmpty(List<Object> columnHeaders) {
        for (Object columnHeader : columnHeaders) {
            if (columnHeader.toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void setUpSpreadsheet(String urlString) throws UploadException {
        if (spreadsheet == null || spreadsheet.getSpreadsheetUrl() == null || !urlString.equals(spreadsheet.getSpreadsheetUrl())) {
            try {
                spreadsheet = sheetsHelper.getSpreadsheet(UrlUtils.getSpreadsheetID(urlString));
                spreadsheet.setSpreadsheetUrl(urlString);
            } catch (GoogleJsonResponseException e) {
                String message = e.getMessage();
                if (e.getDetails() != null && e.getDetails().getCode() == 403) {
                    message = Collect.getInstance().getString(R.string.google_sheets_access_denied);
                }
                throw new UploadException(message);
            } catch (IOException | BadUrlException e) {
                Timber.i(e);
                throw new UploadException(e.getMessage());
            }
        }
    }

    private void ensureNumberOfColumnsIsValid(int numberOfColumns) throws UploadException {
        if (numberOfColumns == 0) {
            throw new UploadException(Collect.getInstance().getString(R.string.no_columns_to_upload));
        }

        if (numberOfColumns > 256) {
            throw new UploadException(Collect.getInstance().getString(R.string.sheets_max_columns,
                    String.valueOf(numberOfColumns)));
        }
    }

    private Integer getSheetId(String sheetTitle) {
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetTitle)) {
                return sheet
                        .getProperties()
                        .getSheetId();
            }
        }
        return null;
    }

    private String getHyperlink(String url, String title) {
        return "=HYPERLINK(\"" + url + "\", \"" + title + "\")";
    }

    private String getSheetUrl(Integer sheetId) {
        return sheetId == null
                ? null
                : spreadsheet.getSpreadsheetUrl().substring(0, spreadsheet.getSpreadsheetUrl().lastIndexOf('/') + 1) + "edit#gid=" + sheetId;
    }

    private String getGoogleSheetsUrl(Cursor cursor) {
        int subIdx = cursor.getColumnIndex(InstanceColumns.SUBMISSION_URI);
        String urlString = cursor.isNull(subIdx) ? null : cursor.getString(subIdx);
        // if we didn't find one in the content provider, try to get from settings
        return urlString == null
                ? (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_GOOGLE_SHEETS_URL)
                : urlString;
    }

    public static boolean isLocationValid(String answer) {
        return Pattern
                .compile("^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s[0-9]+\\.[0-9]+$")
                .matcher(answer)
                .matches();
    }

    public boolean isAuthFailed() {
        return authFailed;
    }

    public void setAuthFailedToFalse() {
        authFailed = false;
    }

    /** An exception that results in the cancellation of an instance upload, and the presentation of an error to the user */
    static class UploadException extends Exception {
        UploadException(String message) {
            super(message);
        }

        UploadException(Throwable cause) {
            super(cause);
        }
    }
}