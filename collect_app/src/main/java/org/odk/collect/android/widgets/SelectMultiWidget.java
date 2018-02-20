/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class SelectMultiWidget extends SelectTextWidget implements MultiChoiceWidget {
    protected ArrayList<CheckBox> checkBoxes;
    private boolean checkboxInit = true;
    private List<Selection> ve;

    public SelectMultiWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        checkBoxes = new ArrayList<>();
        ve = new ArrayList<>();
        if (getFormEntryPrompt().getAnswerValue() != null) {
            //noinspection unchecked
            ve = (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();
        } else {
            ve = new ArrayList<>();
        }

        createLayout();
    }

    @Override
    public void clearAnswer() {
        for (CheckBox c : checkBoxes) {
            if (c.isChecked()) {
                c.setChecked(false);
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        List<Selection> vc = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); ++i) {
            CheckBox c = checkBoxes.get(i);
            if (c.isChecked()) {
                vc.add(new Selection(items.get(i)));
            }
        }

        return vc.size() == 0 ? null : new SelectMultiData(vc);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (CheckBox c : checkBoxes) {
            c.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (CheckBox c : checkBoxes) {
            c.cancelLongPress();
        }
    }

    protected CheckBox createCheckBox(int index) {
        String choiceName = getFormEntryPrompt().getSelectChoiceText(items.get(index));
        CharSequence choiceDisplayName;
        if (choiceName != null) {
            choiceDisplayName = TextUtils.textToHtml(choiceName);
        } else {
            choiceDisplayName = "";
        }
        // no checkbox group so id by answer + offset
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setTag(index);
        checkBox.setId(ViewIds.generateViewId());
        checkBox.setText(choiceDisplayName);
        checkBox.setMovementMethod(LinkMovementMethod.getInstance());
        checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        checkBox.setFocusable(!getFormEntryPrompt().isReadOnly());
        checkBox.setEnabled(!getFormEntryPrompt().isReadOnly());

        for (int vi = 0; vi < ve.size(); vi++) {
            // match based on value, not key
            if (items.get(index).getValue().equals(ve.get(vi).getValue())) {
                checkBox.setChecked(true);
                break;
            }
        }

        // when clicked, check for readonly before toggling
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!checkboxInit && getFormEntryPrompt().isReadOnly()) {
                    if (buttonView.isChecked()) {
                        buttonView.setChecked(false);
                    } else {
                        buttonView.setChecked(true);
                    }
                }
            }
        });

        return checkBox;
    }

    protected void createLayout() {
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                CheckBox checkBox = createCheckBox(i);
                checkBoxes.add(checkBox);
                answerLayout.addView(createMediaLayout(i, checkBox));
            }
            addAnswerView(answerLayout);
        }
        checkboxInit = false;
    }

    @Override
    public int getChoiceCount() {
        return checkBoxes.size();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        checkBoxes.get(choiceIndex).setChecked(isSelected);
    }

}
