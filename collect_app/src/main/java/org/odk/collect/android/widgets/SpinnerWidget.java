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
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.views.ScrolledToTopSpinner;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.util.List;

/**
 * SpinnerWidget handles select-one fields. Instead of a list of buttons it uses a spinner, wherein
 * the user clicks a button and the choices pop up in a dialogue box. The goal is to be more
 * compact. If images, audio, or video are specified in the select answers they are ignored.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
@SuppressLint("ViewConstructor")
public class SpinnerWidget extends QuestionWidget implements MultiChoiceWidget {
    List<SelectChoice> items;
    ScrolledToTopSpinner spinner;
    String[] choices;

    public SpinnerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(
                prompt.getAppearanceHint());
        if (xpathFuncExpr != null) {
            items = ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr);
        } else {
            items = prompt.getSelectChoices();
        }

        View view = inflate(context, R.layout.spinner_layout, null);

        spinner = view.findViewById(R.id.spinner);
        choices = new String[items.size() + 1];
        for (int i = 0; i < items.size(); i++) {
            choices[i] = prompt.getSelectChoiceText(items.get(i));
        }
        choices[items.size()] = getContext().getString(R.string.select_one);

        // The spinner requires a custom adapter. It is defined below
        SpinnerAdapter adapter =
                new SpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, choices,
                        TypedValue.COMPLEX_UNIT_DIP, getQuestionFontSize());

        spinner.setAdapter(adapter);
        spinner.setPrompt(prompt.getQuestionText());
        spinner.setEnabled(!prompt.isReadOnly());
        spinner.setFocusable(!prompt.isReadOnly());

        // Fill in previous answer
        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        spinner.setSelection(items.size());
        if (s != null) {
            for (int i = 0; i < items.size(); ++i) {
                String match = items.get(i).getValue();
                if (match.equals(s)) {
                    spinner.setSelection(i);
                }
            }
        }

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (position == items.size()) {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this,
                            "onCheckedChanged.clearValue",
                            "", getFormEntryPrompt().getIndex());
                } else {
                    Collect.getInstance().getActivityLogger().logInstanceAction(this,
                            "onCheckedChanged",
                            items.get(position).getValue(), getFormEntryPrompt().getIndex());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        addAnswerView(view);
    }


    @Override
    public IAnswerData getAnswer() {
        clearFocus();
        int i = spinner.getSelectedItemPosition();
        if (i == -1 || i == items.size()) {
            return null;
        } else {
            SelectChoice sc = items.get(i);
            return new SelectOneData(new Selection(sc));
        }
    }

    @Override
    public void clearAnswer() {
        // It seems that spinners cannot return a null answer. This resets the answer
        // to its original value, but it is not null.
        spinner.setSelection(items.size());
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

    @Override
    public int getChoiceCount() {
        return items.size();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            spinner.setSelection(choiceIndex);

        } else if (spinner.getSelectedItemPosition() == choiceIndex) {

            clearAnswer();
        }
    }

    // Defines how to display the select answers
    private class SpinnerAdapter extends ArrayAdapter<String> {
        Context context;
        String[] items = new String[]{};
        int textUnit;
        float textSize;

        SpinnerAdapter(final Context context, final int textViewResourceId,
                       final String[] objects, int textUnit, float textSize) {
            super(context, textViewResourceId, objects);
            this.items = objects;
            this.context = context;
            this.textUnit = textUnit;
            this.textSize = textSize;
        }

        @Override
        // Defines the text view parameters for the drop down list entries
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setTextSize(textUnit, textSize);
            tv.setPadding(20, 10, 10, 10);

            if (themeUtils.isDarkTheme()) {
                convertView.setBackgroundColor(getResources().getColor(R.color.darkPopupDialogColor));
            }

            if (position == items.length - 1) {
                tv.setText(parent.getContext().getString(R.string.clear_answer));
            } else {
                tv.setText(items[position]);
            }

            if (position == (items.length - 1) && spinner.getSelectedItemPosition() == position) {
                tv.setEnabled(false);
            } else {
                tv.setTextColor(spinner.getSelectedItemPosition() == position ? themeUtils.getAccentColor() : themeUtils.getPrimaryTextColor());
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setTextSize(textUnit, textSize);
            tv.setPadding(10, 10, 10, 10);
            tv.setText(items[position]);

            return convertView;
        }
    }
}
