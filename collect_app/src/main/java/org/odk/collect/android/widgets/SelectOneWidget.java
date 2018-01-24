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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.TextUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class SelectOneWidget
        extends SelectTextWidget
        implements OnCheckedChangeListener, AudioPlayListener, MultiChoiceWidget {

    protected List<RadioButton> buttons;
    protected String selectedValue;

    public SelectOneWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        buttons = new ArrayList<>();

        if (prompt.getAnswerValue() != null) {
            selectedValue = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        createLayout();
    }

    @Override
    public void clearAnswer() {
        for (RadioButton button : this.buttons) {
            if (button.isChecked()) {
                button.setChecked(false);
                clearNextLevelsOfCascadingSelect();
                break;
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        int i = getCheckedId();
        return i == -1 ? null : new SelectOneData(new Selection(items.get(i)));
    }

    public int getCheckedId() {
        for (int i = 0; i < buttons.size(); ++i) {
            RadioButton button = buttons.get(i);
            if (button.isChecked()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            for (RadioButton button : buttons) {
                if (button.isChecked() && !(buttonView == button)) {
                    button.setChecked(false);
                    clearNextLevelsOfCascadingSelect();
                }
            }
        }
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (RadioButton r : buttons) {
            r.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (RadioButton button : this.buttons) {
            button.cancelLongPress();
        }
    }

    protected RadioButton createRadioButton(int index) {
        String choiceName = getFormEntryPrompt().getSelectChoiceText(items.get(index));
        CharSequence choiceDisplayName;
        if (choiceName != null) {
            choiceDisplayName = TextUtils.textToHtml(choiceName);
        } else {
            choiceDisplayName = "";
        }

        RadioButton radioButton = new RadioButton(getContext());
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        radioButton.setText(choiceDisplayName);
        radioButton.setMovementMethod(LinkMovementMethod.getInstance());
        radioButton.setTag(index);
        radioButton.setId(ViewIds.generateViewId());
        radioButton.setEnabled(!getFormEntryPrompt().isReadOnly());
        radioButton.setFocusable(!getFormEntryPrompt().isReadOnly());

        if (items.get(index).getValue().equals(selectedValue)) {
            radioButton.setChecked(true);
        }

        radioButton.setOnCheckedChangeListener(this);

        return radioButton;
    }

    protected void createLayout() {
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                RadioButton radioButton = createRadioButton(i);
                buttons.add(radioButton);

                answerLayout.addView(createMediaLayout(i, radioButton));
            }
            addAnswerView(answerLayout);
        }
    }

    public List<RadioButton> getButtons() {
        return buttons;
    }

    @Override
    public int getChoiceCount() {
        return buttons.size();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        for (RadioButton button : buttons) {
            button.setChecked(false);
        }

        RadioButton button = buttons.get(choiceIndex);
        button.setChecked(isSelected);

        onCheckedChanged(button, isSelected);
    }

}
