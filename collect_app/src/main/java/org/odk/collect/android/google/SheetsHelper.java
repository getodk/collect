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

package org.odk.collect.android.google;


import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.sheets.v4.Sheets;
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
import java.util.List;

public class SheetsHelper {
    private Sheets sheets;

    SheetsHelper(GoogleAccountCredential credential, HttpTransport transport, JsonFactory jsonFactory) {
        // Initialize sheets service
        sheets = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("ODK-Collect")
                .build();
    }

    public void resizeSpreadSheet(String spreadsheetId, int sheetId, int columnSize) throws IOException {

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
        sheets.spreadsheets()
                .batchUpdate(
                        spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(requests)
                ).execute();
    }

    /**
     * Inserts a new row in the given sheet of the spreadsheet
     */
    public void insertRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
        sheets.spreadsheets().values()
                .append(spreadsheetId, sheetName, row)
                .setIncludeValuesInResponse(true)
                .setValueInputOption("USER_ENTERED").execute();
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
    public List<List<Object>> getHeaderFeed(String spreadsheetId, String sheetName) throws IOException {
        ValueRange response = sheets.spreadsheets()
                .values()
                .get(spreadsheetId, sheetName)
                .execute();
        return response.getValues();
    }

    /**
     * Checks whether the selected google account has sufficient permissions
     * to modify the given spreadsheetId. If yes, then returns complete spreadsheet
     * otherwise throws exception
     */
    public Spreadsheet checkPermissions(String spreadsheetId) throws IOException {

        /*
         * Read permission check
         */

        // fetching the google spreadsheet
        Spreadsheet spreadsheet = sheets.spreadsheets()
                .get(spreadsheetId)
                .setIncludeGridData(false)
                .execute();

        String spreadsheetFileName = spreadsheet.getProperties().getTitle();

        /*
         * Write permission check
         *
         * Todo 22/3/17 Find a better way to check the write permissions
         */

        // creating a request to update name of spreadsheet
        SpreadsheetProperties sheetProperties = new SpreadsheetProperties()
                .setTitle(spreadsheetFileName);

        List<Request> requests = new ArrayList<>();
        requests.add(
                new Request().setUpdateSpreadsheetProperties
                        (new UpdateSpreadsheetPropertiesRequest()
                                .setProperties(sheetProperties)
                                .setFields("title")));

        // updating the spreadsheet with the given id
        sheets.spreadsheets()
                .batchUpdate(spreadsheetId, new BatchUpdateSpreadsheetRequest()
                        .setRequests(requests))
                .execute();
        return spreadsheet;
    }
}
