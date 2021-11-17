/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.external;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.analytics.AnalyticsUtils;
import org.odk.collect.android.formmanagement.BlankFormsListViewModel;
import org.odk.collect.android.formmanagement.BlankFormsListViewModel.BlankForm;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.SettingsProvider;

import java.util.List;

import javax.inject.Inject;

/**
 * Allows the user to create desktop shortcuts to any form currently available to Collect
 *
 * @author ctsims
 * @author carlhartung (modified for ODK)
 */
public class AndroidShortcutsActivity extends AppCompatActivity {

    @Inject
    BlankFormsListViewModel.Factory blankFormsListViewModelFactory;

    @Inject
    SettingsProvider settingsProvider;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        DaggerUtils.getComponent(this).inject(this);
        BlankFormsListViewModel blankFormsListViewModel = new ViewModelProvider(this, blankFormsListViewModelFactory).get(BlankFormsListViewModel.class);
        List<BlankForm> forms = blankFormsListViewModel.getForms();

        showFormListDialog(forms);
    }

    private void showFormListDialog(List<BlankForm> forms) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.select_odk_shortcut)
                .setItems(forms.stream().map(BlankForm::getName).toArray(String[]::new), (dialog, item) -> {
                    AnalyticsUtils.logServerEvent(AnalyticsEvents.CREATE_SHORTCUT, settingsProvider.getUnprotectedSettings());

                    Intent intent = getShortcutIntent(forms, item);
                    setResult(RESULT_OK, intent);
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .create()
                .show();
    }

    @NotNull
    private Intent getShortcutIntent(List<BlankForm> forms, int item) {
        Intent shortcutIntent = new Intent(Intent.ACTION_EDIT);
        shortcutIntent.setData(forms.get(item).getContentUri());

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, forms.get(item).getName());
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.notes);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        return intent;
    }
}
