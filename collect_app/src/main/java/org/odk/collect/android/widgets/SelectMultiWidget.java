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
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectMultiWidget extends SelectWidget {
    private boolean checkboxInit = true;

    private ArrayList<CheckBox> checkBoxes;

    public SelectMultiWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        checkBoxes = new ArrayList<>();

        List<Selection> ve = new ArrayList<>();
        if (prompt.getAnswerValue() != null) {
            ve = (List<Selection>) prompt.getAnswerValue().getValue();
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
                // no checkbox group so id by answer + offset
                CheckBox c = new CheckBox(getContext());
                c.setTag(i);
                c.setId(QuestionWidget.newUniqueId());
                c.setText(choiceDisplayName);
                c.setMovementMethod(LinkMovementMethod.getInstance());
                c.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
                c.setFocusable(!prompt.isReadOnly());
                c.setEnabled(!prompt.isReadOnly());

                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (items.get(i).getValue().equals(ve.get(vi).getValue())) {
                        c.setChecked(true);
                        break;
                    }

                }
                checkBoxes.add(c);
                // when clicked, check for readonly before toggling
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!checkboxInit && formEntryPrompt.isReadOnly()) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                                Collect.getInstance().getActivityLogger().logInstanceAction(this,
                                        "onItemClick.deselect",
                                        items.get((Integer) buttonView.getTag()).getValue(),
                                        formEntryPrompt.getIndex());
                            } else {
                                buttonView.setChecked(true);
                                Collect.getInstance().getActivityLogger().logInstanceAction(this,
                                        "onItemClick.select",
                                        items.get((Integer) buttonView.getTag()).getValue(),
                                        formEntryPrompt.getIndex());
                            }
                        }
                    }
                });

                answerLayout.addView(createMediaLayout(i, c));
            }
            addAnswerView(answerLayout);
        }
        checkboxInit = false;
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
}
