package org.odk.collect.android.gdrive.sheets;

import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;

public interface SheetsApi {

    void batchUpdate(String spreadsheetId, List<Request> requests) throws IOException;

    void insertRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException;

    void updateRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException;

    ValueRange getSpreadsheet(String spreadsheetId, String sheetName) throws IOException;

    Spreadsheet getSpreadsheet(String spreadsheetId) throws IOException;
}
