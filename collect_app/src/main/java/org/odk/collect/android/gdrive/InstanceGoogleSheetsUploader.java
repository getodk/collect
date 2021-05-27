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

package org.odk.collect.android.gdrive;

import androidx.annotation.NonNull;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.javarosa.core.model.Constants;
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
import org.odk.collect.android.exception.BadUrlException;
import org.odk.collect.android.exception.MultipleFoldersFoundException;
import org.odk.collect.android.gdrive.sheets.DriveApi;
import org.odk.collect.android.gdrive.sheets.DriveHelper;
import org.odk.collect.android.gdrive.sheets.SheetsApi;
import org.odk.collect.android.gdrive.sheets.SheetsHelper;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.upload.InstanceUploader;
import org.odk.collect.android.upload.UploadException;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.StringUtils;
import org.odk.collect.android.utilities.TranslationHandler;
import org.odk.collect.android.utilities.UrlUtils;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;

import java.io.File;
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

import static org.odk.collect.android.javarosawrapper.FormController.INSTANCE_ID;

public class InstanceGoogleSheetsUploader extends InstanceUploader {
    private static final String PARENT_KEY = "PARENT_KEY";
    private static final String KEY = "KEY";

    private static final String UPLOADED_MEDIA_URL = "https://drive.google.com/open?id=";

    private static final String ALTITUDE_TITLE_POSTFIX = "-altitude";
    private static final String ACCURACY_TITLE_POSTFIX = "-accuracy";

    private final DriveHelper driveHelper;
    private final SheetsHelper sheetsHelper;

    private Spreadsheet spreadsheet;

    public InstanceGoogleSheetsUploader(DriveApi driveApi, SheetsApi sheetsApi) {
        driveHelper = new DriveHelper(driveApi);
        sheetsHelper = new SheetsHelper(sheetsApi);
    }

    @Override
    public String uploadOneSubmission(Instance instance, String spreadsheetUrl) throws UploadException {
        File instanceFile = new File(instance.getInstanceFilePath());
        if (!instanceFile.exists()) {
            throw new UploadException(FAIL + "instance XML file does not exist!");
        }

        // Get corresponding blank form and verify there is exactly 1
        List<Form> forms = new FormsRepositoryProvider(Collect.getInstance()).get().getAllByFormIdAndVersion(instance.getFormId(), instance.getFormVersion());

        try {
            if (forms.size() != 1) {
                throw new UploadException(TranslationHandler.getString(Collect.getInstance(), R.string.not_exactly_one_blank_form_for_this_form_id));
            }

            Form form = forms.get(0);
            if (form.getBASE64RSAPublicKey() != null) {
                submissionComplete(instance, false);
                throw new UploadException(TranslationHandler.getString(Collect.getInstance(), R.string.google_sheets_encrypted_message));
            }

            String formFilePath = new StoragePathProvider().getAbsoluteFormFilePath(form.getFormFilePath());

            TreeElement instanceElement = getInstanceElement(formFilePath, instanceFile);
            setUpSpreadsheet(spreadsheetUrl);
            sheetsHelper.updateSpreadsheetLocaleForNewSpreadsheet(spreadsheet.getSpreadsheetId(), spreadsheet.getSheets().get(0).getProperties().getTitle());
            if (hasRepeatableGroups(instanceElement)) {
                createSheetsIfNeeded(instanceElement);
            }
            String key = getInstanceID(getChildElements(instanceElement, false));
            if (key == null) {
                key = PropertyUtils.genUUID();
            }
            insertRows(instance, instanceElement, null, key, instanceFile, spreadsheet.getSheets().get(0).getProperties().getTitle());
        } catch (UploadException e) {
            submissionComplete(instance, false);
            throw e;
        } catch (GoogleJsonResponseException e) {
            submissionComplete(instance, false);
            throw new UploadException(getErrorMessageFromGoogleJsonResponseException(e));
        }

        submissionComplete(instance, true);
        // Google Sheets can't provide a custom success message
        return null;
    }

    private String getErrorMessageFromGoogleJsonResponseException(GoogleJsonResponseException e) {
        String message = e.getMessage();
        if (e.getDetails() != null) {
            switch (e.getDetails().getCode()) {
                case 403:
                    message = TranslationHandler.getString(Collect.getInstance(), R.string.google_sheets_access_denied);
                    break;
                case 429:
                    message = FAIL + "Too many requests per 100 seconds";
                    break;
            }
        }
        return message;
    }

    @Override
    @NonNull
    public String getUrlToSubmitTo(Instance instance, String deviceId, String overrideURL, String urlFromSettings) {
        String urlString = instance.getSubmissionUri();

        // if we didn't find one in the content provider, try to get from settings
        return urlString == null
                ? urlFromSettings
                : urlString;
    }

    private void insertRows(Instance instance, TreeElement element, String parentKey, String key, File instanceFile, String sheetTitle)
            throws UploadException {
        insertRow(instance, element, parentKey, key, instanceFile, StringUtils.ellipsizeBeginning(sheetTitle));

        int repeatIndex = 0;
        for (TreeElement child : getChildElements(element, true)) {
            if (child.isRepeatable() && child.getMultiplicity() != TreeReference.INDEX_TEMPLATE) {
                insertRows(instance, child, key, getKeyBasedOnParentKey(key, child.getName(), repeatIndex++), instanceFile, getElementTitle(child));
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

    private void insertRow(Instance instance, TreeElement element, String parentKey, String key, File instanceFile, String sheetTitle)
            throws UploadException {
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

            HashMap<String, String> answers = getAnswers(instance, element, columnTitles, instanceFile, parentKey, key);

            if (shouldRowBeInserted(answers)) {
                sheetsHelper.insertRow(spreadsheet.getSpreadsheetId(), sheetTitle,
                        new ValueRange().setValues(Collections.singletonList(prepareListOfValues(sheetCells.get(0), columnTitles, answers))));
            }
        } catch (GoogleJsonResponseException e) {
            throw new UploadException(getErrorMessageFromGoogleJsonResponseException(e));
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
     *                     the sheetHeaders
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
    // by Javarosa https://github.com/getodk/javarosa/issues/266
    private boolean shouldRowBeInserted(HashMap<String, String> answers) {
        for (String answer : answers.values()) {
            if (answer != null && !answer.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private String uploadMediaFile(Instance instance, String fileName) throws UploadException {
        File instanceFile = new File(instance.getInstanceFilePath());
        String filePath = instanceFile.getParentFile() + "/" + fileName;
        File toUpload = new File(filePath);

        if (!new File(filePath).exists()) {
            throw new UploadException(Collect.getInstance()
                    .getString(R.string.media_upload_error, filePath));
        }

        String folderId;
        try {
            folderId = driveHelper.createOrGetIDOfSubmissionsFolder();
        } catch (IOException | MultipleFoldersFoundException e) {
            Timber.e(e);
            throw new UploadException(e);
        }

        String uploadedFileId;

        // file is ready to be uploaded
        try {
            uploadedFileId = driveHelper.uploadFileToDrive(filePath, folderId, toUpload);
        } catch (IOException e) {
            Timber.e(e, "Exception thrown while uploading the file to drive");
            throw new UploadException(e);
        }

        // checking if file was successfully uploaded
        if (uploadedFileId == null) {
            throw new UploadException("Unable to upload the media files. Try again");
        }
        return UPLOADED_MEDIA_URL + uploadedFileId;
    }

    private TreeElement getInstanceElement(String formFilePath, File instanceFile) throws UploadException {
        FormDef formDef;

        File formXml = new File(formFilePath);
        String lastSavedSrc = FileUtils.getOrCreateLastSavedSrc(formXml);

        try {
            formDef = XFormUtils.getFormFromFormXml(formFilePath, lastSavedSrc);
            FormLoaderTask.importData(instanceFile, new FormEntryController(new FormEntryModel(formDef)));
        } catch (IOException | RuntimeException e) {
            throw new UploadException(e);
        }
        return formDef.getMainInstance().getRoot();
    }

    private boolean hasRepeatableGroups(TreeElement element) {
        for (TreeElement childElement : getChildElements(element, false)) {
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
        for (TreeElement childElement : getChildElements(element, false)) {
            if (childElement.isRepeatable()) {
                sheetTitles.add(StringUtils.ellipsizeBeginning(getElementTitle(childElement)));
                sheetTitles.addAll(getSheetTitles(childElement));
            }
        }
        return sheetTitles;
    }

    private HashMap<String, String> getAnswers(Instance instance, TreeElement element, List<Object> columnTitles, File instanceFile, String parentKey, String key)
            throws UploadException {
        HashMap<String, String> answers = new HashMap<>();
        for (TreeElement childElement : getChildElements(element, false)) {
            String elementTitle = getElementTitle(childElement);
            if (childElement.isRepeatable()) {
                answers.put(elementTitle, getHyperlink(getSheetUrl(getSheetId(StringUtils.ellipsizeBeginning(elementTitle))), elementTitle));
            } else {
                String answer = getFormattingResistantAnswer(childElement);

                if (new File(instanceFile.getParentFile() + "/" + answer).isFile()) {
                    String mediaUrl = uploadMediaFile(instance, answer);
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

    public static String getFormattingResistantAnswer(TreeElement childElement) {
        String answer = childElement.getValue() != null ? childElement.getValue().getDisplayText() : "";

        if (!answer.isEmpty() && (childElement.getDataType() == Constants.DATATYPE_TEXT
                || childElement.getDataType() == Constants.DATATYPE_MULTIPLE_ITEMS
                || childElement.getDataType() == Constants.DATATYPE_BARCODE)) {
            answer = "'" + answer;
        }

        return answer;
    }

    /**
     * Strips the Altitude and Accuracy from a location String and adds them as separate columns if
     * the column titles exist.
     *
     * @param columnTitles - A List of column titles on the sheet
     * @param elementTitle - The title of the geo data to parse. e.g. "data-Point"
     * @param geoData      - A space (" ") separated string that contains "Lat Long Altitude Accuracy"
     * @return a Map of fields containing Lat/Long and Accuracy, Altitude (if the respective column
     * titles exist in the columnTitles parameter).
     */
    private @NonNull
    Map<String, String> parseGeopoint(@NonNull List<Object> columnTitles, @NonNull String elementTitle, @NonNull String geoData) {
        Map<String, String> geoFieldsMap = new HashMap<>();

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
        for (TreeElement child : getChildElements(element, false)) {
            final String elementTitle = getElementTitle(child);
            columnTitles.add(elementTitle);
            if (newSheet && child.getDataType() == Constants.DATATYPE_GEOPOINT) {
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
                throw new UploadException(TranslationHandler.getString(Collect.getInstance(), R.string.google_sheets_missing_columns, columnTitle));
            }
        }
    }

    /**
     * This method builds a column name by joining all of the containing group names using "-" as a separator
     */
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

    private List<TreeElement> getChildElements(TreeElement element, boolean includeAllRepeats) {
        List<TreeElement> elements = new ArrayList<>();
        TreeElement prior = null;
        for (int i = 0; i < element.getNumChildren(); ++i) {
            TreeElement current = element.getChildAt(i);
            if (includeAllRepeats || !nextInstanceOfTheSameRepeatableGroup(prior, current)) {
                switch (current.getDataType()) {
                    case Constants.DATATYPE_TEXT:
                    case Constants.DATATYPE_INTEGER:
                    case Constants.DATATYPE_DECIMAL:
                    case Constants.DATATYPE_DATE:
                    case Constants.DATATYPE_TIME:
                    case Constants.DATATYPE_DATE_TIME:
                    case Constants.DATATYPE_CHOICE:
                    case Constants.DATATYPE_CHOICE_LIST:
                    case Constants.DATATYPE_BOOLEAN:
                    case Constants.DATATYPE_GEOPOINT:
                    case Constants.DATATYPE_BARCODE:
                    case Constants.DATATYPE_BINARY:
                    case Constants.DATATYPE_LONG:
                    case Constants.DATATYPE_GEOSHAPE:
                    case Constants.DATATYPE_GEOTRACE:
                    case Constants.DATATYPE_UNSUPPORTED:
                        elements.add(current);
                        break;
                    case Constants.DATATYPE_NULL:
                        if (current.isRepeatable()) { // repeat group
                            elements.add(current);
                        } else if (current.getNumChildren() == 0) { // assume fields that don't have children are string fields
                            elements.add(current);
                        } else { // one or more children - this is a group
                            elements.addAll(getChildElements(current, includeAllRepeats));
                        }
                        break;
                }
                prior = current;
            }
        }
        return elements;
    }

    private boolean nextInstanceOfTheSameRepeatableGroup(TreeElement prior, TreeElement current) {
        return prior != null && prior.getName().equals(current.getName());
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
            // https://github.com/getodk/collect/issues/931
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

    private void setUpSpreadsheet(String urlString) throws UploadException, GoogleJsonResponseException {
        if (spreadsheet == null || spreadsheet.getSpreadsheetUrl() == null || !urlString.equals(spreadsheet.getSpreadsheetUrl())) {
            try {
                spreadsheet = sheetsHelper.getSpreadsheet(UrlUtils.getSpreadsheetID(urlString));
                spreadsheet.setSpreadsheetUrl(urlString);
            } catch (GoogleJsonResponseException e) {
                Timber.i(e);
                throw e;
            } catch (IOException | BadUrlException e) {
                Timber.i(e);
                throw new UploadException(e);
            }
        }
    }

    private void ensureNumberOfColumnsIsValid(int numberOfColumns) throws UploadException {
        if (numberOfColumns == 0) {
            throw new UploadException(TranslationHandler.getString(Collect.getInstance(), R.string.no_columns_to_upload));
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

    public static boolean isLocationValid(String answer) {
        return Pattern
                .compile("^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s[0-9]+\\.[0-9]+$")
                .matcher(answer)
                .matches();
    }
}
