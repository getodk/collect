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

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.ActionListener;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.utilities.CompressionUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.SharedPreferencesUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;
import static org.odk.collect.android.utilities.PermissionUtils.requestCameraPermission;
import static org.odk.collect.android.utilities.QRCodeUtils.QR_CODE_FILEPATH;


public class ShowQRCodeFragment extends Fragment {

    private static final int SELECT_PHOTO = 111;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final boolean[] checkedItems = new boolean[]{true, true};

    @BindView(R.id.ivQRcode)
    ImageView ivQRCode;
    @BindView(R.id.circularProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.tvPasswordWarning)
    TextView tvPasswordWarning;

    private Intent shareIntent;
    private AlertDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        ButterKnife.bind(this, view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.import_export_settings));
        ((AdminPreferencesActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        generateCode();
        return view;
    }

    private void generateCode() {
        shareIntent = null;
        progressBar.setVisibility(VISIBLE);
        ivQRCode.setVisibility(GONE);
        addPasswordStatusString();

        Disposable disposable = QRCodeUtils.getQRCodeGeneratorObservable(getSelectedPasswordKeys())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    progressBar.setVisibility(GONE);
                    ivQRCode.setVisibility(VISIBLE);
                    ivQRCode.setImageBitmap(bitmap);
                }, Timber::e, this::updateShareIntent);
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
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
        tvPasswordWarning.setText(status);
    }

    private void updateShareIntent() {
        // Initialize the intent to share QR Code
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + QR_CODE_FILEPATH));
    }

    @OnClick(R.id.btnScan)
    void scanButtonClicked() {
        requestCameraPermission(getActivity(), new PermissionListener() {
            @Override
            public void granted() {
                IntentIntegrator.forFragment(ShowQRCodeFragment.this)
                        .setCaptureActivity(ScannerWithFlashlightActivity.class)
                        .setBeepEnabled(true)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                        .setOrientationLocked(false)
                        .setPrompt(getString(R.string.qrcode_scanner_prompt))
                        .initiateScan();
            }

            @Override
            public void denied() {
            }
        });
    }

    @OnClick(R.id.btnSelect)
    void chooseButtonClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        } else {
            ToastUtils.showShortToast(getString(R.string.activity_not_found, getString(R.string.choose_image)));
            Timber.w(getString(R.string.activity_not_found, getString(R.string.choose_image)));
        }
    }

    @OnClick(R.id.tvPasswordWarning)
    void passwordWarningClicked() {
        if (dialog == null) {
            final String[] items = new String[]{
                    getString(R.string.admin_password),
                    getString(R.string.server_password)};

            dialog = new AlertDialog.Builder(getActivity())
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
        dialog.show();
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
                try {
                    applySettings(CompressionUtils.decompress(result.getContents()));
                } catch (IOException | DataFormatException e) {
                    Timber.e(e);
                }
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
                } catch (FormatException | NotFoundException | ChecksumException e) {
                    Timber.i(e);
                    ToastUtils.showLongToast("QR Code not found in the selected image");
                } catch (DataFormatException | IOException e) {
                    Timber.e(e);
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
                }
            } else {
                Timber.i("Choosing QR code from sdcard cancelled");
            }
        }
    }


    private void applySettings(String content) {
        SharedPreferencesUtils.savePreferencesFromString(content, new ActionListener() {
            @Override
            public void onSuccess() {
                Collect.getInstance().initProperties();
                ToastUtils.showLongToast(Collect.getInstance().getString(R.string.successfully_imported_settings));
                getActivity().finish();
                final LocaleHelper localeHelper = new LocaleHelper();
                localeHelper.updateLocale(getActivity());
                MainMenuActivity.startActivityAndCloseAllOthers(getActivity());
            }

            @Override
            public void onFailure(Exception exception) {
                Timber.e(exception);
            }
        });
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
