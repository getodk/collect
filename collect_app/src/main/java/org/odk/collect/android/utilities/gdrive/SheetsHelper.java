/*
 * Copyright (C) 2017 Shobhit
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

package org.odk.collect.android.utilities.gdrive;

import android.support.annotation.NonNull;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SheetsHelper {
    private final SheetsService sheetsService;

    SheetsHelper(GoogleAccountCredential credential, HttpTransport transport, JsonFactory jsonFactory) {
        // Initialize sheets service
        Sheets sheets = new Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("ODK-Collect")
                .build();

        sheetsService = new SheetsService(sheets);
    }

    /**
     * Constructs a new SheetsHelper with the provided Sheets Service.
     * This Constructor should only be used for testing.
     */
    SheetsHelper(@NonNull SheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    /**
     * Resize the column size of the provided sheetId in the google spreadsheet
     *
     * @param spreadsheetId unique id of the spreadsheet
     * @param sheetId       the index of the sheet which is to be resized
     * @param columnSize    the new column size of the sheet
     * @throws IllegalArgumentException Providing sheetId or column size less than zero throws this exception
     *                                  Sheet Id is basically index of the sheet starting from zero
     *                                  Similarly, the value for the column size can't be negative
     * @throws IOException              Throws this exception if the google spreadsheet cannot be fetched
     *                                  due to insufficient permissions or invalid sheet id
     */
    public void resizeSpreadSheet(String spreadsheetId, int sheetId, int columnSize) throws IllegalArgumentException, IOException {
        if (sheetId < 0) {
            throw new IllegalArgumentException("Sheet Id should be greater than or equal to 0");
        }

        if (columnSize < 1) {
            throw new IllegalArgumentException("Column size should be greater than 0");
        }

        // create grid properties with the new column size
        GridProperties gridProperties = new GridProperties().setColumnCount(columnSize);

        // create sheet properties for the first sheet in the spreadsheet
        SheetProperties sheetProperties = new SheetProperties()
                .setSheetId(sheetId)
                .setGridProperties(gridProperties);

        // Updates properties of the sheet with the specified sheetId
        UpdateSheetPropertiesRequest updateSheetPropertyRequest = new UpdateSheetPropertiesRequest()
                .setProperties(sheetProperties)
                .setFields("gridProperties.columnCount");

        // generate request
        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setUpdateSheetProperties(updateSheetPropertyRequest));

        // send the API request
        sheetsService.batchUpdate(spreadsheetId, requests);
    }

    /**
     * Inserts a new row in the given sheet of the spreadsheet
     */
    public void insertRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
        if (row == null) {
            throw new IllegalArgumentException("ValueRange cannot be null");
        }

        sheetsService.insertRow(spreadsheetId, sheetName, row);
    }

    public void updateRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
        if (row == null) {
            throw new IllegalArgumentException("ValueRange cannot be null");
        }

        sheetsService.updateRow(spreadsheetId, sheetName, row);
    }

    public void addSheet(String spreadsheetId, String sheetName) throws IOException {
        AddSheetRequest addSheetRequest = new AddSheetRequest();
        addSheetRequest.setProperties(new SheetProperties().setTitle(sheetName));
        Request request = new Request();
        request.setAddSheet(addSheetRequest);
        sheetsService.batchUpdate(spreadsheetId, Collections.singletonList(request));
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
    public List<List<Object>> getSheetCells(String spreadsheetId, String sheetName) throws IOException {
        ValueRange response = sheetsService.getSpreadsheet(spreadsheetId, sheetName);
        return response.getValues();
    }

    /**
     * Checks whether the selected google account has sufficient permissions
     * to modify the given spreadsheetId. If yes, then returns complete spreadsheet
     * otherwise throws exception
     */
    public Spreadsheet getSpreadsheet(String spreadsheetId) throws IOException {

        /*
         * Read permission check
         *
         * To check read permissions, we are trying to fetch the complete spreadsheet using the
         * given spreadsheet id
         */

        // fetching the google spreadsheet
        Spreadsheet spreadsheet = sheetsService.getSpreadsheet(spreadsheetId);

        String spreadsheetFileName = spreadsheet.getProperties().getTitle();

        /*
         * Write permission check
         *
         * To check write permissions, we are trying to overwrite the name of the spreadsheet with
         * the same name
         *
         * Todo 22/3/17 Find a better way to check the write permissions
         */

        // creating a request to update name of spreadsheet
        SpreadsheetProperties sheetProperties = new SpreadsheetProperties()
                .setTitle(spreadsheetFileName);

        List<Request> requests = new ArrayList<>();
        requests.add(
                new Request().setUpdateSpreadsheetProperties(
                        new UpdateSpreadsheetPropertiesRequest()
                                .setProperties(sheetProperties)
                                .setFields("title")));

        // updating the spreadsheet with the given id
        sheetsService.batchUpdate(spreadsheetId, requests);
        return spreadsheet;
    }


    /**
     * This class only makes API calls using the sheets API and does not contain any business logic
     *
     * @author Shobhit Agarwal
     */

    public class SheetsService {
        private final Sheets sheets;

        SheetsService(Sheets sheets) {
            this.sheets = sheets;
        }

        public void batchUpdate(String spreadsheetId, List<Request> requests) throws IOException {
            sheets.spreadsheets()
                    .batchUpdate(
                            spreadsheetId,
                            new BatchUpdateSpreadsheetRequest().setRequests(requests)
                    ).execute();
        }

        public void insertRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
            sheets.spreadsheets().values()
                    .append(spreadsheetId, sheetName, row)
                    .setIncludeValuesInResponse(true)
                    .setValueInputOption("USER_ENTERED").execute();
        }

        public void updateRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
            sheets.spreadsheets().values()
                    .update(spreadsheetId, sheetName, row)
                    .setIncludeValuesInResponse(true)
                    .setValueInputOption("USER_ENTERED").execute();
        }

        ValueRange getSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
            return sheets.spreadsheets()
                    .values()
                    .get(spreadsheetId, sheetName)
                    .execute();
        }

        public Spreadsheet getSpreadsheet(String spreadsheetId) throws IOException {
            return sheets.spreadsheets()
                    .get(spreadsheetId)
                    .setIncludeGridData(false)
                    .execute();
        }
    }
}
