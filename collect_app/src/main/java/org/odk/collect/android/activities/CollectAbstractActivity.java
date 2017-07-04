/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.odk.collect.android.R;
import org.odk.collect.android.bundle.CollectDialogBundle;
import org.odk.collect.android.fragments.CollectDialogFragment;

public abstract class CollectAbstractActivity extends AppCompatActivity {
    protected FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();
    }

    public void buildDialog(CollectDialogBundle collectDialogBundle) {
        if (collectDialogBundle != null) {
            createAndShowDialog(collectDialogBundle);
        }
    }

    private void createAndShowDialog(CollectDialogBundle collectDialogBundle) {
        CollectDialogFragment dialogFragment = CollectDialogFragment.newInstance(collectDialogBundle);
        dialogFragment.show(fragmentManager, collectDialogBundle.getDialogTag());
    }

    public void buildResetSettingsFinalDialog(String message) {
        CollectDialogBundle.Builder dialogBuilder = new CollectDialogBundle.Builder();
        dialogBuilder
                .setIcon(android.R.drawable.ic_dialog_info)
                .setDialogTitle(getString(R.string.reset_app_state_result))
                .setDialogMessage(message)
                .setRightButtonText(getString(R.string.ok))
                .setRightButtonAction(CollectDialogFragment.Action.RESETTING_SETTINGS_FINISHED)
                .setCancelable(false);

        CollectDialogBundle collectDialogBundle = dialogBuilder.build();
        buildDialog(collectDialogBundle);
    }
}
