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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.OrderedHashtable;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.logic.PromptElement;

import java.util.Enumeration;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectOneWidget extends RadioGroup implements IQuestionWidget {

    private int mRadioChecked = -1;
    OrderedHashtable mItems;


    public SelectOneWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        clearCheck();
    }


    public IAnswerData getAnswer() {
        int i = getCheckedRadioButtonId();
        if (i == -1) {
            return null;
        } else {
            String s = (String) mItems.elementAt(i - 1);
            return new SelectOneData(new Selection(s));
        }
    }


    @SuppressWarnings("unchecked")
    public void buildView(final PromptElement prompt) {
        mItems = prompt.getSelectItems();

        setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mRadioChecked != -1 && prompt.isReadonly()) {
                    SelectOneWidget.this.check(mRadioChecked);
                }
            }
        });

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerObject()).getValue();
        }

        if (prompt.getSelectItems() != null) {
            OrderedHashtable h = prompt.getSelectItems();
            Enumeration e = h.keys();
            String k = null;
            String v = null;

            // android radio ids start at 1, not 0
            int i = 1;
            while (e.hasMoreElements()) {
                k = (String) e.nextElement();
                v = (String) h.get(k);

                RadioButton r = new RadioButton(getContext());
                r.setText(k);
                r.setTextSize(TypedValue.COMPLEX_UNIT_PT, GlobalConstants.APPLICATION_FONTSIZE);
                r.setId(i);
                r.setEnabled(!prompt.isReadonly());
                r.setFocusable(!prompt.isReadonly());
                addView(r);

                if (v.equals(s)) {
                    r.setChecked(true);
                    mRadioChecked = i;
                }

                i++;
            }
        }
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }

}
