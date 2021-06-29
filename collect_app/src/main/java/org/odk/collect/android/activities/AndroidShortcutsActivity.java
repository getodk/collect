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

package org.odk.collect.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.formmanagement.BlankFormsListViewModel;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.provider.FormsProviderAPI;
import org.odk.collect.forms.Form;

import java.util.ArrayList;
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
    CurrentProjectProvider currentProjectProvider;

    @Inject
    BlankFormsListViewModel.Factory blankFormsListViewModelFactory;

    private Uri[] commands;
    private String[] names;
    private BlankFormsListViewModel blankFormsListViewModel;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        DaggerUtils.getComponent(this).inject(this);
        blankFormsListViewModel = new ViewModelProvider(this, blankFormsListViewModelFactory).get(BlankFormsListViewModel.class);

        buildMenuList();
    }

    /**
     * Builds a list of shortcuts
     */
    private void buildMenuList() {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Uri> commands = new ArrayList<>();

        List<Form> forms = blankFormsListViewModel.getForms();
        for (Form form : forms) {
            String formName = form.getDisplayName();
            names.add(formName);
            Uri uri = FormsProviderAPI.getUri(currentProjectProvider.getCurrentProject().getUuid(), form.getDbId());
            commands.add(uri);
        }

        this.names = names.toArray(new String[0]);
        this.commands = commands.toArray(new Uri[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_odk_shortcut);

        builder.setItems(this.names, (dialog, item) -> returnShortcut(this.names[item], this.commands[item]));

        builder.setOnCancelListener(dialog -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Returns the results to the calling intent.
     */
    private void returnShortcut(String name, Uri command) {
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setData(command);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.notes);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        // Now, return the result to the launcher

        setResult(RESULT_OK, intent);
        finish();
    }

}
