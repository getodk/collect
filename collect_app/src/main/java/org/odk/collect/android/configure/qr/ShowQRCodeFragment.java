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

package org.odk.collect.android.configure.qr;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_PASSWORD;
import static org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_ADMIN_PW;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.odk.collect.android.R;
import org.odk.collect.android.databinding.ShowQrcodeFragmentBinding;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.async.Scheduler;
import org.odk.collect.settings.SettingsProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

public class ShowQRCodeFragment extends Fragment {
    ShowQrcodeFragmentBinding binding;

    private final boolean[] checkedItems = {true, true};
    private final boolean[] passwordsSet = {true, true};

    private AlertDialog dialog;

    @Inject
    public QRCodeGenerator qrCodeGenerator;

    @Inject
    public SettingsProvider settingsProvider;

    @Inject
    public Scheduler scheduler;

    @Inject
    AppConfigurationGenerator appConfigurationGenerator;

    private QRCodeViewModel qrCodeViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = ShowQrcodeFragmentBinding.inflate(inflater);
        binding.tvPasswordWarning.setOnClickListener(v -> passwordWarningClicked());

        setHasOptionsMenu(true);
        passwordsSet[0] = !settingsProvider.getProtectedSettings().getString(KEY_ADMIN_PW).isEmpty();
        passwordsSet[1] = !settingsProvider.getUnprotectedSettings().getString(KEY_PASSWORD).isEmpty();

        qrCodeViewModel.getBitmap().observe(this.getViewLifecycleOwner(), bitmap -> {
            if (bitmap != null) {
                binding.circularProgressBar.setVisibility(GONE);
                binding.ivQRcode.setVisibility(VISIBLE);
                binding.ivQRcode.setImageBitmap(bitmap);
            } else {
                binding.circularProgressBar.setVisibility(VISIBLE);
                binding.ivQRcode.setVisibility(GONE);
            }
        });

        qrCodeViewModel.getWarning().observe(this.getViewLifecycleOwner(), warning -> {
            if (warning != null) {
                binding.tvPasswordWarning.setText(warning);
                binding.status.setVisibility(VISIBLE);
            } else {
                binding.status.setVisibility(GONE);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        qrCodeViewModel = new ViewModelProvider(
                requireActivity(),
                new QRCodeViewModel.Factory(qrCodeGenerator, appConfigurationGenerator, settingsProvider, scheduler)
        ).get(QRCodeViewModel.class);
    }

    private void passwordWarningClicked() {
        if (dialog == null) {
            final String[] items = {
                    getString(R.string.admin_password),
                    getString(R.string.server_password)};

            dialog = new MaterialAlertDialogBuilder(getActivity())
                    .setTitle(R.string.include_password_dialog)
                    .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                    .setCancelable(false)
                    .setPositiveButton(R.string.generate, (dialog, which) -> {
                        qrCodeViewModel.setIncludedKeys(getSelectedPasswordKeys());
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
