/*
 * Copyright (C) 2017 Shobhit
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

import android.view.View;
import android.widget.Button;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;

/**
 * Displays the structure of a form along with the answers for the current instance. Disables all
 * features that allow the user to edit the form instance.
 */
public class ViewOnlyFormHierarchyActivity extends FormHierarchyActivity {
    /**
     * Hides buttons to jump to the beginning and to the end of the form instance to edit it. Adds
     * an extra exit button that exits this activity.
     */
    @Override
    void configureButtons(FormController formController) {
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        exitButton.setVisibility(View.VISIBLE);

        jumpBeginningButton.setVisibility(View.GONE);
        jumpEndButton.setVisibility(View.GONE);
    }

    @Override
    protected void showDeleteButton(boolean shouldShow) {
        // Disabled.
    }

    @Override
    protected void showAddButton(boolean shouldShow) {
        // Disabled.
    }

    /**
     * Prevents the user from clicking on individual questions to jump into the form-filling view.
     */
    @Override
    void onQuestionClicked(FormIndex index) {
        // Do nothing
    }

    /**
     * Prevents logging an audit event when the user exits the activity.
     */
    @Override
    public void onBackPressed() {
        onBackPressedWithoutLogger();
    }
}
