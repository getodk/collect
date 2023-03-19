package org.odk.collect.android.gdrive;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;

import org.odk.collect.android.gdrive.sheets.DriveApi;
import org.odk.collect.android.gdrive.sheets.SheetsApi;

import java.util.Collections;

public class GoogleApiProvider {

    private final Context context;

    public GoogleApiProvider(Context context) {
        this.context = context;
    }

    public SheetsApi getSheetsApi(String account) {
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());
        googleAccountCredential.setSelectedAccountName(account);

        return new GoogleSheetsApi(new Sheets.Builder(new NetHttpTransport(), gsonFactory, googleAccountCredential)
                .setApplicationName("ODK-Collect")
                .build());
    }

    public DriveApi getDriveApi(String account) {
        GsonFactory gsonFactory = GsonFactory.getDefaultInstance();

        GoogleAccountCredential googleAccountCredential = GoogleAccountCredential
                .usingOAuth2(context, Collections.singletonList(DriveScopes.DRIVE))
                .setBackOff(new ExponentialBackOff());
        googleAccountCredential.setSelectedAccountName(account);

        return new GoogleDriveApi(new Drive.Builder(new NetHttpTransport(), gsonFactory, googleAccountCredential)
                .setApplicationName("ODK-Collect")
                .build());
    }
}
