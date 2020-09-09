package org.odk.collect.android.gdrive;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.odk.collect.android.gdrive.sheets.SheetsApi;

import java.io.IOException;
import java.util.List;

/**
 * This class only makes API calls using the sheets API and does not contain any business logic
 *
 * @author Shobhit Agarwal
 */

public class GoogleSheetsApi implements SheetsApi {

    private final Sheets sheets;

    public GoogleSheetsApi(Sheets sheets) {
        this.sheets = sheets;
    }

    @Override
    public void batchUpdate(String spreadsheetId, List<Request> requests) throws IOException {
        sheets.spreadsheets()
                .batchUpdate(
                        spreadsheetId,
                        new BatchUpdateSpreadsheetRequest().setRequests(requests)
                ).execute();
    }

    @Override
    public void insertRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
        sheets.spreadsheets().values()
                .append(spreadsheetId, sheetName, row)
                .setIncludeValuesInResponse(true)
                .setInsertDataOption("INSERT_ROWS")
                .setValueInputOption("USER_ENTERED").execute();
    }

    @Override
    public void updateRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
        sheets.spreadsheets().values()
                .update(spreadsheetId, sheetName, row)
                .setIncludeValuesInResponse(true)
                .setValueInputOption("USER_ENTERED").execute();
    }

    @Override
    public ValueRange getSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
        return sheets.spreadsheets()
                .values()
                .get(spreadsheetId, sheetName)
                .execute();
    }

    @Override
    public Spreadsheet getSpreadsheet(String spreadsheetId) throws IOException {
        return sheets.spreadsheets()
                .get(spreadsheetId)
                .setIncludeGridData(false)
                .execute();
    }
}
