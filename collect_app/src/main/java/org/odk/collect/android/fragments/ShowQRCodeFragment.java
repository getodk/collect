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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.fragment.app.Fragment;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.ScanQRCodeActivity;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.ViewPagerListener;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

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
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

public class ShowQRCodeFragment extends Fragment implements ViewPagerListener {

    private static final int SELECT_PHOTO = 111;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final boolean[] checkedItems = {true, true};
    private final boolean[] passwordsSet = {true, true};

    @BindView(R.id.ivQRcode)
    ImageView ivQRCode;
    @BindView(R.id.circularProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.tvPasswordWarning)
    TextView tvPasswordWarning;
    @BindView(R.id.status)
    LinearLayout passwordStatus;

    private AlertDialog dialog;

    @Inject
    public Analytics analytics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        ((CollectAbstractActivity) getActivity()).initToolbar(getString(R.string.configure_via_qr_code));
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        passwordsSet[0] = !((String) AdminSharedPreferences.getInstance().get(KEY_ADMIN_PW)).isEmpty();
        passwordsSet[1] = !((String) GeneralSharedPreferences.getInstance().get(KEY_PASSWORD)).isEmpty();
        generateCode();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        DaggerUtils.getComponent(activity).inject(this);
    }

    private void generateCode() {
        progressBar.setVisibility(VISIBLE);
        ivQRCode.setVisibility(GONE);
        setPasswordWarning();

        Disposable disposable = QRCodeUtils.getQRCodeGeneratorObservable(getSelectedPasswordKeys())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    progressBar.setVisibility(GONE);
                    ivQRCode.setVisibility(VISIBLE);
                    ivQRCode.setImageBitmap(bitmap);
                }, Timber::e);
        compositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void setPasswordWarning() {
        if (!passwordsSet[0] && !passwordsSet[1]) {
            // should not display password warning is passwords are not set
            passwordStatus.setVisibility(View.INVISIBLE);
            return;
        }

        boolean showingAdminPassword = passwordsSet[0] && checkedItems[0];
        boolean showingServerPassword = passwordsSet[1] && checkedItems[1];
        CharSequence status;
        if (showingAdminPassword && showingServerPassword) {
            status = getText(R.string.qrcode_with_both_passwords);
        } else if (showingAdminPassword) {
            status = getText(R.string.qrcode_with_admin_password);
        } else if (showingServerPassword) {
            status = getText(R.string.qrcode_with_server_password);
        } else {
            status = getText(R.string.qrcode_without_passwords);
        }
        tvPasswordWarning.setText(status);
        passwordStatus.setVisibility(VISIBLE);
    }

    @OnClick(R.id.tvPasswordWarning)
    void passwordWarningClicked() {
        if (dialog == null) {
            final String[] items = {
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

            // disable checkbox if password not set
            dialog.getListView().setOnHierarchyChangeListener(
                    new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            CharSequence text = ((AppCompatCheckedTextView)child).getText();
                            int itemIndex = Arrays.asList(items).indexOf(text);
                            if (!passwordsSet[itemIndex]) {
                                child.setEnabled(passwordsSet[itemIndex]);
                                child.setOnClickListener(null);
                            }
                        }

                        @Override
                        public void onChildViewRemoved(View view, View view1) {
                        }
                    });
        }

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    boolean qrCodeFound = false;
                    final Uri imageUri = data.getData();
                    if (imageUri != null) {
                        final InputStream imageStream = getActivity().getContentResolver()
                                .openInputStream(imageUri);

                        final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        if (bitmap != null) {
                            String response = QRCodeUtils.decodeFromBitmap(bitmap);
                            if (response != null) {
                                qrCodeFound = true;
                                ScanQRCodeActivity.applySettings(getActivity(), response);
                            }
                        }
                    }
                    if (!qrCodeFound) {
                        ToastUtils.showLongToast(R.string.qr_code_not_found);
                    }
                } catch (FormatException | NotFoundException | ChecksumException e) {
                    Timber.i(e);
                    ToastUtils.showLongToast(R.string.qr_code_not_found);
                } catch (DataFormatException | IOException | OutOfMemoryError | IllegalArgumentException e) {
                    Timber.e(e);
                    ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
                }
            } else {
                Timber.i("Choosing QR code from sdcard cancelled");
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

    @Override
    public void onPauseFragment() {

    }

    @Override
    public void onResumeFragment() {

    }
}
