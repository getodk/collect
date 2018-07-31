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

package org.odk.collect.android.widgets;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.List;

public class SelectMultipleAutocompleteWidget extends SelectMultiWidget implements CompoundButton.OnCheckedChangeListener, AudioPlayListener {
    public SelectMultipleAutocompleteWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    @Override
    protected void addButtonsToLayout(List<Integer> tagList) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (tagList == null || tagList.contains(i)) {
                answerLayout.addView(checkBoxes.get(i));
                answerLayout.setDividerDrawable(getResources().getDrawable(themeUtils.getDivider()));
                answerLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            }
        }
    }

    @Override
    public void setFocus(Context context) {
        SoftKeyboardUtils.showSoftKeyboard(searchStr);
    }

    @Override
    protected void createLayout() {
        readItems();

        if (items != null) {
            SpacesInUnderlyingValuesWarning.forQuestionWidget(this).renderWarningIfNecessary(items);
            for (int i = 0; i < items.size(); i++) {
                checkBoxes.add(createCheckBox(i));
            }
        }

        setUpSearchBox();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }
}