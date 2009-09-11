/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.PromptElement;

import java.util.Enumeration;
import java.util.Vector;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectMultiWidget extends LinearLayout implements IQuestionWidget {

    private final static int CHECKBOX_ID = 100;
    private boolean mCheckboxInit = true;
    OrderedHashtable mItems;


    public SelectMultiWidget(Context context) {
        super(context);
    }


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


    @SuppressWarnings("unchecked")
    public IAnswerData getAnswer() {
        Vector<Selection> ve = new Vector<Selection>();
        Enumeration en = mItems.keys();
        String k = null;
        String v = null;

        // counter for offset
        int i = 0;
        while (en.hasMoreElements()) {
            k = (String) en.nextElement();
            v = (String) mItems.get(k);
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                ve.add(new Selection(v));
            }
            i++;
        }

        if (ve.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(ve);
        }
    }


    @SuppressWarnings("unchecked")
    public void buildView(final PromptElement prompt) {
        mItems = prompt.getSelectItems();

        setOrientation(LinearLayout.VERTICAL);

        Vector ve = new Vector();
        if (prompt.getAnswerValue() != null) {
            ve = (Vector) prompt.getAnswerObject();
        }

        if (prompt.getSelectItems() != null) {
            OrderedHashtable h = prompt.getSelectItems();
            Enumeration en = h.keys();
            String k = null;
            String v = null;

            // counter for offset
            int i = 0;

            while (en.hasMoreElements()) {

                k = (String) en.nextElement();
                v = (String) h.get(k);

                // no checkbox group so id by answer + offset
                CheckBox c = new CheckBox(getContext());

                // when clicked, check for readonly before toggling
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mCheckboxInit && prompt.isReadonly()) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                            } else {
                                buttonView.setChecked(true);
                            }
                        }
                    }
                });

                c.setId(CHECKBOX_ID + i);
                c.setText(k);
                c.setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);

                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (v.equals(((Selection) ve.elementAt(vi)).getValue())) {
                        c.setChecked(true);
                        break;
                    }
                }

                c.setFocusable(!prompt.isReadonly());
                c.setEnabled(!prompt.isReadonly());
                addView(c);
                i++;
            }
        }

        mCheckboxInit = false;
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }

}
