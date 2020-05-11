/*
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

package org.odk.collect.android.preferences.qr;

import android.app.AlertDialog;
import android.content.Context;
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

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
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

public class ShowQRCodeFragment extends Fragment {

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
    @Inject
    public AdminSharedPreferences adminSharedPreferences;
    @Inject
    public GeneralSharedPreferences generalSharedPreferences;
    @Inject
    public QRCodeGenerator qrCodeGenerator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_qrcode_fragment, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        passwordsSet[0] = !((String) adminSharedPreferences.get(KEY_ADMIN_PW)).isEmpty();
        passwordsSet[1] = !((String) generalSharedPreferences.get(KEY_PASSWORD)).isEmpty();
        generateCode();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    private void generateCode() {
        progressBar.setVisibility(VISIBLE);
        ivQRCode.setVisibility(GONE);
        setPasswordWarning();

        Disposable disposable = qrCodeGenerator.generateQRCode(getSelectedPasswordKeys())
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
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                        checkedItems[which] = isChecked;
                        analytics.logEvent(AnalyticsEvents.CONFIGURE_QR_CODE, items[which]);
                    })
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
                            CharSequence text = ((AppCompatCheckedTextView) child).getText();
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
