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
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * SpinnerWidget handles select-one fields. Instead of a list of buttons it uses a spinner, wherein
 * the user clicks a button and the choices pop up in a dialogue box. The goal is to be more
 * compact. If images, audio, or video are specified in the select answers they are ignored.
 * 
 * @author Jeff Beorse (jeff@beorse.net)
 */
public class SpinnerWidget extends QuestionWidget {
    Vector<SelectChoice> mItems;
    Spinner spinner;
    String[] choices;


    public SpinnerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mItems = prompt.getSelectChoices();
        spinner = new Spinner(context);
        choices = new String[mItems.size()];

        for (int i = 0; i < mItems.size(); i++) {
            choices[i] = prompt.getSelectChoiceText(mItems.get(i));
        }

        // The spinner requires a custom adapter. It is defined below
        SpinnerAdapter adapter =
            new SpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, choices,
                    TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);

        spinner.setAdapter(adapter);
        spinner.setPrompt(prompt.getQuestionText());
        spinner.setEnabled(!prompt.isReadOnly());
        spinner.setFocusable(!prompt.isReadOnly());

        // Fill in previous answer
        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (s != null) {
            for (int i = 0; i < mItems.size(); ++i) {
                String sMatch = mItems.get(i).getValue();
                if (sMatch.equals(s)) {
                    spinner.setSelection(i);
                }

            }
        }

        addView(spinner);

    }


    @Override
    public IAnswerData getAnswer() {
        int i = spinner.getSelectedItemPosition();
        if (i == -1) {
            return null;
        } else {
            SelectChoice sc = mItems.elementAt(i); // - RANDOM_BUTTON_ID);
            return new SelectOneData(new Selection(sc));
        }
    }


    @Override
    public void clearAnswer() {
        // It seems that spinners cannot return a null answer. This resets the answer
        // to its original value, but it is not null.
        spinner.setSelection(0);

    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }

    // Defines how to display the select answers
    private class SpinnerAdapter extends ArrayAdapter<String> {
        Context context;
        String[] items = new String[] {};
        int textUnit;
        float textSize;


        public SpinnerAdapter(final Context context, final int textViewResourceId,
                final String[] objects, int textUnit, float textSize) {
            super(context, textViewResourceId, objects);
            this.items = objects;
            this.context = context;
            this.textUnit = textUnit;
            this.textSize = textSize;
        }


        @Override
        // Defines the text view parameters for the drop down list entries
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.custom_spinner_item, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position]);
            tv.setTextSize(textUnit, textSize);
            tv.setPadding(10, 10, 10, 10); // Are these values OK?
            return convertView;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position]);
            tv.setTextSize(textUnit, textSize);
            return convertView;
        }

    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        spinner.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        spinner.cancelLongPress();
    }

}
