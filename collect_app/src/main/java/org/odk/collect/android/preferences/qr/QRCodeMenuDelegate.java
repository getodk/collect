package org.odk.collect.android.preferences.qr;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.android.utilities.MenuDelegate;
import org.odk.collect.android.utilities.ToastUtils;

import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

public class QRCodeMenuDelegate implements MenuDelegate {

    static final int SELECT_PHOTO = 111;

    private final Activity activity;
    private final ActivityAvailability activityAvailability;
    private final QRCodeGenerator qrCodeGenerator;
    private final FileProvider fileProvider;

    QRCodeMenuDelegate(Activity activity, ActivityAvailability activityAvailability, QRCodeGenerator qrCodeGenerator, FileProvider fileProvider) {
        this.activity = activity;
        this.activityAvailability = activityAvailability;
        this.qrCodeGenerator = qrCodeGenerator;
        this.fileProvider = fileProvider;
    }

    @Override
    public void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu) {
        menuInflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_scan_sd_card:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                if (activityAvailability.isActivityAvailable(photoPickerIntent)) {
                    activity.startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                } else {
                    ToastUtils.showShortToast(activity.getString(R.string.activity_not_found, activity.getString(R.string.choose_image)));
                    Timber.w(activity.getString(R.string.activity_not_found, activity.getString(R.string.choose_image)));
                }

                return true;

            case R.id.menu_item_share:
                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... voids) {
                        try {
                            Collection<String> keys = new ArrayList<>();
                            keys.add(KEY_ADMIN_PW);
                            keys.add(KEY_PASSWORD);
                            return qrCodeGenerator.generateQRCode(keys);
                        } catch (Exception ignored) {
                            // Ignored
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(String filePath) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, fileProvider.getURIForFile(filePath));
                        activity.startActivity(intent);
                    }
                }.execute();

                return true;
        }

        return false;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {

    }

    @Override
    public void invalidateOptionsMenu() {

    }
}
