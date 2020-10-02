package org.odk.collect.android.configure.qr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.utilities.ActivityResultDelegate;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;
import static org.odk.collect.android.configure.qr.QRCodeMenuDelegate.SELECT_PHOTO;

public class QRCodeActivityResultDelegate implements ActivityResultDelegate {

    private final Activity activity;
    private final SettingsImporter settingsImporter;
    private final QRCodeDecoder qrCodeDecoder;
    private final Analytics analytics;

    public QRCodeActivityResultDelegate(Activity activity, SettingsImporter settingsImporter, QRCodeDecoder qrCodeDecoder, Analytics analytics) {
        this.activity = activity;
        this.settingsImporter = settingsImporter;
        this.qrCodeDecoder = qrCodeDecoder;
        this.analytics = analytics;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            final Uri imageUri = data.getData();
            if (imageUri != null) {
                final InputStream imageStream;

                try {
                    imageStream = activity.getContentResolver().openInputStream(imageUri);
                } catch (FileNotFoundException e) {
                    // Not sure how this could happen? If you work it out: write a test!
                    return;
                }

                try {
                    String response = qrCodeDecoder.decode(imageStream);
                    String responseHash = FileUtils.getMd5Hash(new ByteArrayInputStream(response.getBytes()));
                    if (response != null) {
                        if (settingsImporter.fromJSON(response)) {
                            showToast(R.string.successfully_imported_settings);
                            analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR_IMAGE, "Success", responseHash);
                            startActivityAndCloseAllOthers(activity, MainMenuActivity.class);
                        } else {
                            showToast(R.string.invalid_qrcode);
                            analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR_IMAGE, "No valid settings", responseHash);
                        }
                    }

                } catch (QRCodeDecoder.InvalidException e) {
                    showToast(R.string.invalid_qrcode);
                    analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR_IMAGE, "Invalid exception", "none");
                } catch (QRCodeDecoder.NotFoundException e) {
                    showToast(R.string.qr_code_not_found);
                    analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR_IMAGE, "No QR code", "none");
                }
            }
        }
    }

    private void showToast(int string) {
        Toast.makeText(activity, activity.getString(string), Toast.LENGTH_LONG).show();
    }
}

