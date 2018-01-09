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

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;

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
