package org.odk.collect.android.support;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.odk.collect.android.gdrive.sheets.DriveApi;
import org.odk.collect.android.gdrive.sheets.SheetsApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class FakeGoogleApi implements DriveApi, SheetsApi {

    private final Map<String, List<List<Object>>> spreadsheets = new HashMap<>();

    private String account;
    private String attemptAccount;

    public void setAccount(String deviceAccount) {
        this.account = deviceAccount;
    }

    public void setAttemptAccount(String attemptAccount) {
        this.attemptAccount = attemptAccount;
    }

    @Override
    public String getFileId(String fileId, String fields) throws IOException {
        return null;
    }

    @Override
    public Drive.Files.List generateRequest(String query, String fields) throws IOException {
        return null;
    }

    @Override
    public void downloadFile(String fileId, File file) throws IOException {

    }

    @Override
    public String uploadFile(com.google.api.services.drive.model.File metadata, FileContent fileContent, String fields) throws IOException {
        return null;
    }

    @Override
    public String createFile(com.google.api.services.drive.model.File file, String fields) throws IOException {
        return null;
    }

    @Override
    public void setPermission(String folderId, String fields, Permission permission) throws IOException {

    }

    @Override
    public void fetchAllFiles(Drive.Files.List request, List<com.google.api.services.drive.model.File> files) throws IOException {

    }

    @Override
    public void fetchFilesForCurrentPage(Drive.Files.List request, List<com.google.api.services.drive.model.File> files) throws IOException {

    }

    @Override
    public void batchUpdate(String spreadsheetId, List<Request> requests) throws IOException {

    }

    @Override
    public void insertRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {
        if (!attemptAccount.equals(account)) {
            throw new IOException();
        }

        List<List<Object>> rows = spreadsheets.getOrDefault(spreadsheetId, new ArrayList<>());
        rows.add(row.getValues().get(0));

        spreadsheets.put(spreadsheetId, rows);
    }

    @Override
    public void updateRow(String spreadsheetId, String sheetName, ValueRange row) throws IOException {

    }

    @Override
    public ValueRange getSpreadsheet(String spreadsheetId, String sheetName) throws IOException {
        if (!attemptAccount.equals(account)) {
            throw new IOException();
        }

        if (spreadsheets.containsKey(spreadsheetId)) {
            List<List<Object>> rows = spreadsheets.get(spreadsheetId);
            ValueRange valueRange = new ValueRange();
            valueRange.setValues(rows);
            return valueRange;
        } else {
            return new ValueRange();
        }
    }

    @Override
    public Spreadsheet getSpreadsheet(String spreadsheetId) throws IOException {
        if (!attemptAccount.equals(account)) {
            throw new IOException();
        }

        Spreadsheet spreadsheet = new Spreadsheet();
        spreadsheet.setSpreadsheetId(spreadsheetId);

        SpreadsheetProperties spreadsheetProperties = new SpreadsheetProperties();
        spreadsheetProperties.setTitle("Blah");
        spreadsheet.setProperties(spreadsheetProperties);

        Sheet sheet = new Sheet();
        SheetProperties sheetProperties = new SheetProperties();
        sheetProperties.setTitle("Blah");
        sheet.setProperties(sheetProperties);
        spreadsheet.setSheets(asList(sheet));

        return spreadsheet;
    }
}
