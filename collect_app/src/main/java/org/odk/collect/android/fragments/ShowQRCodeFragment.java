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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.QRCodeListener;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.utilities.QRCodeUtils.decodeFromBitmap;
import static org.odk.collect.android.utilities.QRCodeUtils.generateQRBitMap;
import static org.odk.collect.android.utilities.QRCodeUtils.saveBitmapToCache;


public class ShowQRCodeFragment extends Fragment implements View.OnClickListener, QRCodeListener {

    private static final int SELECT_PHOTO = 111;
    private boolean[] checkedItems = new boolean[]{true, true};
    private ImageView qrImageView;
    private ProgressBar progressBar;
    private Intent shareIntent;
    private TextView editQRCode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.import_export_settings));
        ((AdminPreferencesActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        qrImageView = (ImageView) view.findViewById(R.id.qr_iv);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        editQRCode = (TextView) view.findViewById(R.id.edit_qrcode);
        editQRCode.setOnClickListener(this);
        Button scan = (Button) view.findViewById(R.id.btnScan);
        scan.setOnClickListener(this);
        Button select = (Button) view.findViewById(R.id.btnSelect);
        select.setOnClickListener(this);
        generateCode();
        return view;
    }

    public void generateCode() {
        addPasswordStatusString();
        new GenerateQRCode(this).execute();
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

    private void updateShareIntent(Bitmap qrCode) throws IOException {

        // Send an intent to share saved image
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        // Save the bitmap to a file
        File shareFile = saveBitmapToCache(qrCode);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + shareFile));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnScan:
                IntentIntegrator integrator = IntentIntegrator.forFragment(this);
                integrator
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
                String[] items = new String[]{
                        getString(R.string.admin_password),
                        getString(R.string.server_password)};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.include_password_dialog)
                        .setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checkedItems[which] = isChecked;
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton(R.string.generate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                generateCode();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
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
                    String response = decodeFromBitmap(bitmap);
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
        String decompressedData;
        try {
            decompressedData = CompressionUtils.decompress(content);
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
        Intent intent = new Intent(getActivity().getBaseContext(), MainMenuActivity.class);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
        getActivity().finishAffinity();
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
                getActivity().startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qrcode)));
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


    @Override
    public void preExecute() {
        progressBar.setVisibility(VISIBLE);
        qrImageView.setVisibility(GONE);
    }

    @Override
    public void bitmapGenerated(Bitmap bitmap) {
        progressBar.setVisibility(GONE);
        qrImageView.setVisibility(VISIBLE);

        if (bitmap != null) {
            qrImageView.setImageBitmap(bitmap);
            try {
                updateShareIntent(bitmap);
            } catch (IOException e) {
                Timber.e(e);
            }
        }
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

    private class GenerateQRCode extends AsyncTask<Void, Void, Bitmap> {
        private final QRCodeListener listener;

        GenerateQRCode(QRCodeListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.preExecute();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            listener.bitmapGenerated(bitmap);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return generateQRBitMap(getSelectedPasswordKeys());
        }
    }
}
