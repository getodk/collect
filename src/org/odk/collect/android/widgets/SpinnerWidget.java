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
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import org.odk.collect.android.external.ExternalDataUtil;

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
    private static final int BROWN = 0xFF936931;


    public SpinnerWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }

        spinner = new Spinner(context);
        choices = new String[mItems.size()+1];
        for (int i = 0; i < mItems.size(); i++) {
            choices[i] = prompt.getSelectChoiceText(mItems.get(i));
        }
        choices[mItems.size()] = getContext().getString(R.string.select_one);

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

        spinner.setSelection(mItems.size());
        if (s != null) {
            for (int i = 0; i < mItems.size(); ++i) {
                String sMatch = mItems.get(i).getValue();
                if (sMatch.equals(s)) {
                    spinner.setSelection(i);
                }
            }
        }

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if ( position == mItems.size() ) {
					Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged.clearValue", 
		    			"", mPrompt.getIndex());
				} else {
					Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged", 
			    			mItems.get(position).getValue(), mPrompt.getIndex());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}});
        
        addView(spinner);

    }


    @Override
    public IAnswerData getAnswer() {
    	clearFocus();
        int i = spinner.getSelectedItemPosition();
        if (i == -1 || i == mItems.size()) {
            return null;
        } else {
            SelectChoice sc = mItems.elementAt(i);
            return new SelectOneData(new Selection(sc));
        }
    }


    @Override
    public void clearAnswer() {
        // It seems that spinners cannot return a null answer. This resets the answer
        // to its original value, but it is not null.
        spinner.setSelection(mItems.size());
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
            tv.setTextSize(textUnit, textSize);
            tv.setBackgroundColor(Color.WHITE);
        	tv.setPadding(10, 10, 10, 10); // Are these values OK?
            if (position == items.length-1) {
            	tv.setText(parent.getContext().getString(R.string.clear_answer));
            	tv.setTextColor(BROWN);
        		tv.setTypeface(null, Typeface.NORMAL);
            	if (spinner.getSelectedItemPosition() == position) {
            		tv.setBackgroundColor(Color.LTGRAY);
            	}
            } else {
                tv.setText(items[position]);
                tv.setTextColor(Color.BLACK);
            	tv.setTypeface(null, (spinner.getSelectedItemPosition() == position) 
            							? Typeface.BOLD : Typeface.NORMAL);
            }
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
            tv.setTextColor(Color.BLACK);
        	tv.setTypeface(null, Typeface.BOLD);
            if (position == items.length-1) {
            	tv.setTextColor(BROWN);
            	tv.setTypeface(null, Typeface.NORMAL);
            }
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
