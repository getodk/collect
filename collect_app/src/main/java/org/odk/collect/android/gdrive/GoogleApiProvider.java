package org.odk.collect.android.gdrive;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;

public class GoogleApiProvider {

    public SheetsApi getSheetsApi(HttpRequestInitializer httpRequestInitializer) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new GoogleSheetsApi(new Sheets.Builder(transport, jsonFactory, httpRequestInitializer)
                .setApplicationName("ODK-Collect")
                .build());
    }

    public DriveApi getDriveApi(HttpRequestInitializer httpRequestInitializer) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new GoogleDriveApi(new Drive.Builder(transport, jsonFactory, httpRequestInitializer)
                .setApplicationName("ODK-Collect")
                .build());
    }
}
