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
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.listeners.AudioPlayListener;
import org.odk.collect.android.utilities.TextUtils;
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
public abstract class AbstractSelectOneWidget extends SelectTextWidget
        implements OnCheckedChangeListener, AudioPlayListener, MultiChoiceWidget, View.OnClickListener {

    @Nullable
    private AdvanceToNextListener listener;

    protected List<RadioButton> buttons;
    protected String selectedValue;

    private final boolean autoAdvance;
    private final boolean isReadOnly;
    private final int fontSize;

    public AbstractSelectOneWidget(Context context, FormEntryPrompt prompt, boolean autoAdvance) {
        super(context, prompt);

        isReadOnly = getFormEntryPrompt().isReadOnly();
        fontSize = getAnswerFontSize();

        buttons = new ArrayList<>();

        if (prompt.getAnswerValue() != null) {
            if (this instanceof ItemsetWidget) {
                selectedValue = prompt.getAnswerValue().getDisplayText();
            } else { // Regular SelectOneWidget
                selectedValue = ((Selection) prompt.getAnswerValue().getValue()).getValue();
            }
        }

        this.autoAdvance = autoAdvance;

        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }
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

        return i == -1 ? null :
                (this instanceof ItemsetWidget ? new StringData(items.get(i).getValue()) : new SelectOneData(new Selection(items.get(i))));
    }

    public int getCheckedId() {
        for (RadioButton button : buttons) {
            if (button.isChecked()) {
                return (int) button.getTag();
            }
        }
        return -1;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            for (RadioButton button : buttons) {
                if (button.isChecked() && buttonView != button) {
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
        for (RadioButton button : buttons) {
            button.cancelLongPress();
        }
    }

    protected RadioButton createRadioButton(int index) {
        SelectChoice item = items.get(index);

        String choiceName = getFormEntryPrompt().getSelectChoiceText(item);

        AppCompatRadioButton radioButton = new AppCompatRadioButton(getContext());
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        radioButton.setText(choiceName != null ? TextUtils.textToHtml(choiceName) : "");
        radioButton.setTag(index);
        radioButton.setOnClickListener(this);
        radioButton.setOnCheckedChangeListener(this);
        radioButton.setGravity(isRTL() ? Gravity.END : Gravity.START);
        if (isReadOnly) {
            radioButton.setEnabled(false);
        }

        if (item.getValue().equals(selectedValue)) {
            radioButton.setChecked(true);
        }

        return radioButton;
    }

    protected void createLayout() {
        readItems();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                answerLayout.addView(createRadioButtonLayout(inflater, i));
            }
            addAnswerView(answerLayout);
        }
    }

    protected RelativeLayout createRadioButtonLayout(LayoutInflater inflater, int index) {
        @SuppressLint("InflateParams")
        RelativeLayout thisParentLayout = (RelativeLayout) inflater.inflate(R.layout.quick_select_layout, null);

        RadioButton radioButton = createRadioButton(index);

        buttons.add(radioButton);

        ((ViewGroup) thisParentLayout.findViewById(R.id.container)).addView(createMediaLayout(index, radioButton));
        if (autoAdvance) {
            thisParentLayout.findViewById(R.id.right_arrow).setVisibility(VISIBLE);
        }

        return thisParentLayout;
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

    @Override
    public void onClick(View view) {
        if (autoAdvance && listener != null) {
            listener.advance();
        }
    }
}
