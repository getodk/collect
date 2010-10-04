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

import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.IAVTLayout;
import org.odk.collect.android.views.QuestionView;

import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectMultiWidget extends LinearLayout implements IQuestionWidget {
    private final static int CHECKBOX_ID = 100;
    private boolean mCheckboxInit = true;
    Vector<SelectChoice> mItems;


    public SelectMultiWidget(Context context) {
        super(context);
    }


    @Override
    public void clearAnswer() {
        int j = mItems.size();
        for (int i = 0; i < j; i++) {

            // no checkbox group so find by id + offset
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                c.setChecked(false);
            }
        }
    }


    @Override
    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        for (int i = 0; i < mItems.size(); i++) {
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                vc.add(new Selection(mItems.get(i).getValue()));
            }

        }

        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }

    }


    @SuppressWarnings("unchecked")
    @Override
    public void buildView(final FormEntryPrompt prompt) {
        mItems = prompt.getSelectChoices();

        setOrientation(LinearLayout.VERTICAL);

        Vector<Selection> ve = new Vector<Selection>();
        if (prompt.getAnswerValue() != null) {
            ve = (Vector<Selection>) prompt.getAnswerValue().getValue();
        }

        if (prompt.getSelectChoices() != null) {
            for (int i = 0; i < mItems.size(); i++) {
                // no checkbox group so id by answer + offset
                CheckBox c = new CheckBox(getContext());

                // when clicked, check for readonly before toggling
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mCheckboxInit && prompt.isReadOnly()) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                            } else {
                                buttonView.setChecked(true);
                            }
                        }
                    }
                });

                c.setId(CHECKBOX_ID + i);
                c.setText(prompt.getSelectChoiceText(mItems.get(i)));
                c.setTextSize(TypedValue.COMPLEX_UNIT_PX, QuestionView.APPLICATION_FONTSIZE);
                c.setFocusable(!prompt.isReadOnly());
                c.setEnabled(!prompt.isReadOnly());
                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (mItems.get(i).getValue().equals(ve.elementAt(vi).getValue())) {
                        c.setChecked(true);
                        break;
                    }

                }

                String audioURI = null;
                audioURI =
                    prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        FormEntryCaption.TEXT_FORM_AUDIO);

                String imageURI = null;
                imageURI =
                    prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        FormEntryCaption.TEXT_FORM_IMAGE);

                String videoURI = null;
                videoURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), "video");
                
                String bigImageURI = null;
                bigImageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), "big-image");

                IAVTLayout mediaLayout = new IAVTLayout(getContext());
                mediaLayout.setAVT(c, audioURI, imageURI, videoURI, bigImageURI);
                addView(mediaLayout);

                // Last, add the dividing line between elements (except for the last element)
                ImageView divider = new ImageView(getContext());
                divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                if (i != mItems.size() - 1) {
                    addView(divider);
                }

            }
        }

        mCheckboxInit = false;
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

}
