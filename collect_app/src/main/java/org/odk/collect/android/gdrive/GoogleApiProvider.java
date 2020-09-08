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

import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;

import java.util.Collections;

public class GoogleApiProvider {

    private final PreferencesProvider preferencesProvider;

    public GoogleApiProvider(PreferencesProvider preferencesProvider) {
        this.preferencesProvider = preferencesProvider;
    }

    public SheetsApi getSheetsApi(Context context) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        String account = preferencesProvider
                .getGeneralSharedPreferences()
                .getString(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT, "");

        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());
        googleAccountCredential.setSelectedAccountName(account);

        return new GoogleSheetsApi(new Sheets.Builder(transport, jsonFactory, googleAccountCredential)
                .setApplicationName("ODK-Collect")
                .build());
    }

    public DriveApi getDriveApi(Context context) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        String account = preferencesProvider
                .getGeneralSharedPreferences()
                .getString(GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT, "");

        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());
        googleAccountCredential.setSelectedAccountName(account);

        return new GoogleDriveApi(new Drive.Builder(transport, jsonFactory, googleAccountCredential)
                .setApplicationName("ODK-Collect")
                .build());
    }
}
