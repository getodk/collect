package org.odk.collect.android.gdrive;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;

import java.util.Collections;

public class GoogleApiProvider {

    public GoogleAccountPicker getAccountPicker(Context context) {
        return new GoogleAccountCredentialGoogleAccountPicker(GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff()));
    }

    public SheetsApi getSheetsApi(GoogleAccountPicker googleAccountPicker) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new GoogleSheetsApi(new Sheets.Builder(transport, jsonFactory, ((GoogleAccountCredentialGoogleAccountPicker) googleAccountPicker).getGoogleAccountCredential())
                .setApplicationName("ODK-Collect")
                .build());
    }

    public DriveApi getDriveApi(GoogleAccountPicker googleAccountPicker) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new GoogleDriveApi(new Drive.Builder(transport, jsonFactory, ((GoogleAccountCredentialGoogleAccountPicker) googleAccountPicker).getGoogleAccountCredential())
                .setApplicationName("ODK-Collect")
                .build());
    }

}
