package org.odk.collect.android.configure.qr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.configure.SettingsImporter;
import org.odk.collect.android.utilities.ActivityResultDelegate;

import java.io.FileNotFoundException;
import java.io.InputStream;

import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;
import static org.odk.collect.android.configure.qr.QRCodeMenuDelegate.SELECT_PHOTO;

public class QRCodeActivityResultDelegate implements ActivityResultDelegate {

    private final Activity activity;
    private final SettingsImporter settingsImporter;
    private final QRCodeDecoder qrCodeDecoder;

    public QRCodeActivityResultDelegate(Activity activity, SettingsImporter settingsImporter, QRCodeDecoder qrCodeDecoder) {
        this.activity = activity;
        this.settingsImporter = settingsImporter;
        this.qrCodeDecoder = qrCodeDecoder;
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
                    if (response != null) {
                        if (settingsImporter.fromJSON(response)) {
                            showToast(R.string.successfully_imported_settings);
                            startActivityAndCloseAllOthers(activity, MainMenuActivity.class);
                        } else {
                            showToast(R.string.invalid_qrcode);
                        }
                    }

                } catch (QRCodeDecoder.InvalidException e) {
                    Timber.e(e);
                    showToast(R.string.invalid_qrcode);
                } catch (QRCodeDecoder.NotFoundException e) {
                    Timber.e(e);
                    showToast(R.string.qr_code_not_found);
                }
            }
        }
    }

    private void showToast(int string) {
        Toast.makeText(activity, activity.getString(string), Toast.LENGTH_LONG).show();
    }
}

