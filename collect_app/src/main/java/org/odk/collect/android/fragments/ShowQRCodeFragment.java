/* Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;


public class ShowQRCodeFragment extends Fragment implements View.OnClickListener {

    private static final int SELECT_PHOTO = 111;
    private static final int QR_CODE_SIDE_LENGTH = 400; // in pixels
    private static final String QR_CODE_FILEPATH = Collect.SETTINGS + File.separator + "collect-settings.jpeg";
    private static final String MD5_CACHE_PATH = Collect.SETTINGS + File.separator + ".md5";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final boolean[] checkedItems = new boolean[]{true, true};
    private ImageView qrImageView;
    private ProgressBar progressBar;
    private Intent shareIntent;
    private TextView editQRCode;
    private AlertDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.import_export_settings));
        ((AdminPreferencesActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        qrImageView = view.findViewById(R.id.qr_iv);
        progressBar = view.findViewById(R.id.progressBar);
        editQRCode = view.findViewById(R.id.edit_qrcode);
        editQRCode.setOnClickListener(this);
        view.findViewById(R.id.btnScan).setOnClickListener(this);
        view.findViewById(R.id.btnSelect).setOnClickListener(this);
        generateCode();
        return view;
    }

    private void generateCode() {
        shareIntent = null;
        progressBar.setVisibility(VISIBLE);
        qrImageView.setVisibility(GONE);
        addPasswordStatusString();

        Disposable disposable = getQRCodeGeneratorObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    progressBar.setVisibility(GONE);
                    qrImageView.setVisibility(VISIBLE);
                    qrImageView.setImageBitmap(bitmap);
                }, Timber::e, this::updateShareIntent);
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private Observable<Bitmap> getQRCodeGeneratorObservable() {
        return Observable.create(emitter -> {
            String preferencesString = SharedPreferencesUtils.getJSONFromPreferences(getSelectedPasswordKeys());
            String md5Hash = FileUtils.getMd5Hash(preferencesString);

            boolean shouldWriteToDisk = true;
            Bitmap bitmap = null;

            File md5CacheFile = new File(MD5_CACHE_PATH);
            if (md5CacheFile.exists()) {
                String md5Cache = FileUtils.readFile(md5CacheFile);

                /*
                 * If the md5Hash generated from the preferences is equal to md5Cache
                 * then don't generate QRCode and read the one saved in disk
                 */
                if (md5Cache.trim().equals(md5Hash)) {
                    Timber.i("Loading QRCode from the disk...");
                    bitmap = BitmapFactory.decodeFile(QR_CODE_FILEPATH);
                    shouldWriteToDisk = false;
                }
            }

            // If the file is not found in the disk or md5Hash not matched
            if (bitmap == null) {
                Timber.i("Generating QRCode...");
                bitmap = QRCodeUtils.generateQRBitMap(preferencesString, QR_CODE_SIDE_LENGTH);
                shouldWriteToDisk = true;
            }

            if (bitmap != null) {
                // Send the QRCode to the observer
                emitter.onNext(bitmap);

                // Save the QRCode to disk
                if (shouldWriteToDisk) {
                    Timber.i("Saving QR Code to disk... : " + QR_CODE_FILEPATH);
                    FileUtils.saveBitmapToFile(bitmap, QR_CODE_FILEPATH);

                    // update .md5 file
                    Timber.i("Updated .md5 file contents");
                    FileUtils.writeToFile(md5CacheFile, md5Hash, false);
                }

                // Send the task completion event
                emitter.onComplete();
            }
        });
    }

    private void addPasswordStatusString() {
        String status;
        if (checkedItems[0] && checkedItems[1]) {
            status = getString(R.string.qrcode_with_both_passwords);
        } else if (checkedItems[0]) {
            status = getString(R.string.qrcode_with_admin_password);
        } else if (checkedItems[1]) {
            status = getString(R.string.qrcode_with_server_password);
        } else {
            status = getString(R.string.qrcode_without_passwords);
        }
        editQRCode.setText(status);
    }

    private void updateShareIntent() {
        // Initialize the intent to share qr code
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + QR_CODE_FILEPATH));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                IntentIntegrator.forFragment(this)
                        .setCaptureActivity(ScannerWithFlashlightActivity.class)
                        .setBeepEnabled(true)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                        .setOrientationLocked(false)
                        .setPrompt(getString(R.string.qrcode_scanner_prompt))
                        .initiateScan();
                break;

            case R.id.btnSelect:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                break;

            case R.id.edit_qrcode:
                if (dialog == null) {
                    dialog = createAlertDialog();
                }
                dialog.show();
                break;
        }
    }

    private AlertDialog createAlertDialog() {
        String[] items = new String[]{
                getString(R.string.admin_password),
                getString(R.string.server_password)};

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.include_password_dialog)
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setCancelable(false)
                .setPositiveButton(R.string.generate, (dialog, which) -> {
                    generateCode();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // request was canceled...
                Timber.i("QR code scanning cancelled");
            } else {
                applySettings(result.getContents());
                return;
            }
        }

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getActivity().getContentResolver()
                            .openInputStream(imageUri);

                    final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    String response = QRCodeUtils.decodeFromBitmap(bitmap);
                    if (response != null) {
                        applySettings(response);
                    }
                } catch (FileNotFoundException e) {
                    Timber.e(e);
                }
            } else {
                Timber.i("Choosing QR code from sdcard cancelled");
            }
        }
    }


    private void applySettings(String content) {
        try {
            String decompressedData = CompressionUtils.decompress(content);
            JSONObject jsonObject = new JSONObject(decompressedData);
            SharedPreferencesUtils prefUtils = new SharedPreferencesUtils();
            prefUtils.savePreferencesFromJSON(jsonObject);
        } catch (DataFormatException e) {
            Timber.e(e);
            ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
            return;
        } catch (IOException | JSONException e) {
            Timber.e(e);
            return;
        }

        getActivity().finish();
        final LocaleHelper localeHelper = new LocaleHelper();
        localeHelper.updateLocale(getActivity());
        ((CollectAbstractActivity) getActivity()).goToTheMainActivityAndCloseAllOthers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                if (shareIntent != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qrcode)));
                }
                return true;
            case R.id.menu_save_preferences:
                File writeDir = new File(Collect.SETTINGS);
                if (!writeDir.exists()) {
                    if (!writeDir.mkdirs()) {
                        ToastUtils.showShortToast("Error creating directory "
                                + writeDir.getAbsolutePath());
                        return false;
                    }
                }

                File dst = new File(writeDir.getAbsolutePath() + "/collect.settings");
                boolean success = AdminPreferencesActivity.saveSharedPreferencesToFile(dst, getActivity());
                if (success) {
                    ToastUtils.showLongToast("Settings successfully written to "
                            + dst.getAbsolutePath());
                } else {
                    ToastUtils.showLongToast("Error writing settings to " + dst.getAbsolutePath());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Collection<String> getSelectedPasswordKeys() {
        Collection<String> keys = new ArrayList<>();

        //adding the selected password keys
        if (checkedItems[0]) {
            keys.add(KEY_ADMIN_PW);
        }

        if (checkedItems[1]) {
            keys.add(KEY_PASSWORD);
        }
        return keys;
    }
}
