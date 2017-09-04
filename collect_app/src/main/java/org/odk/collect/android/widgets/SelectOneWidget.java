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

import java.util.ArrayList;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectOneWidget extends SelectWidget implements OnCheckedChangeListener, AudioPlayListener {

    private ArrayList<RadioButton> buttons;

    public SelectOneWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        buttons = new ArrayList<>();

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                String choiceName = prompt.getSelectChoiceText(items.get(i));
                CharSequence choiceDisplayName;
                if (choiceName != null) {
                    choiceDisplayName = TextUtils.textToHtml(choiceName);
                } else {
                    choiceDisplayName = "";
                }
                RadioButton r = new RadioButton(getContext());
                r.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
                r.setText(choiceDisplayName);
                r.setMovementMethod(LinkMovementMethod.getInstance());
                r.setTag(i);
                r.setId(QuestionWidget.newUniqueId());
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());

                buttons.add(r);

                if (items.get(i).getValue().equals(s)) {
                    r.setChecked(true);
                }

                r.setOnCheckedChangeListener(this);
                answerLayout.addView(createMediaLayout(i, r));
            }
        }
        addAnswerView(answerLayout);
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
}
