/*
 * Copyright (C) 2011 University of Washington
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

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalDataUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * SpinnerMultiWidget, like SelectMultiWidget handles multiple selection fields using checkboxes,
 * but the user clicks a button to see the checkboxes. The goal is to be more compact. If images,
 * audio, or video are specified in the select answers they are ignored. WARNING: There is a bug in
 * android versions previous to 2.0 that affects this widget. You can find the report here:
 * http://code.google.com/p/android/issues/detail?id=922 This bug causes text to be white in alert
 * boxes, which makes the select options invisible in this widget. For this reason, this widget
 * should not be used on phones with android versions lower than 2.0.
 * 
 * @author Jeff Beorse (jeff@beorse.net)
 */
public class SpinnerMultiWidget extends QuestionWidget {

    List<SelectChoice> mItems;

    // The possible select answers
    CharSequence[] answer_items;

    // The button to push to display the answers to choose from
    Button button;

    // Defines which answers are selected
    boolean[] selections;

    // The alert box that contains the answer selection view
    AlertDialog.Builder alert_builder;

    // Displays the current selections below the button
    TextView selectionText;


    @SuppressWarnings("unchecked")
    public SpinnerMultiWidget(final Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }

        mPrompt = prompt;

        selections = new boolean[mItems.size()];
        answer_items = new CharSequence[mItems.size()];
        alert_builder = new AlertDialog.Builder(context);
        button = new Button(context);
        selectionText = new TextView(getContext());

        // Build View
        for (int i = 0; i < mItems.size(); i++) {
            answer_items[i] = prompt.getSelectChoiceText(mItems.get(i));
        }

        selectionText.setText(context.getString(R.string.selected));
        selectionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
        selectionText.setVisibility(View.GONE);

        button.setText(context.getString(R.string.select_answer));
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
        button.setPadding(0, 0, 0, 7);

        // Give the button a click listener. This defines the alert as well. All the
        // click and selection behavior is defined here.
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                alert_builder.setTitle(mPrompt.getQuestionText()).setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            boolean first = true;
                            selectionText.setText("");
                            for (int i = 0; i < selections.length; i++) {
                                if (selections[i]) {

                                    if (first) {
                                        first = false;
                                        selectionText.setText(context.getString(R.string.selected)
                                                + answer_items[i].toString());
                                        selectionText.setVisibility(View.VISIBLE);
                                    } else {
                                        selectionText.setText(selectionText.getText() + ", "
                                                + answer_items[i].toString());
                                    }
                                }
                            }
                        }
                    });

                alert_builder.setMultiChoiceItems(answer_items, selections,
                    new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            selections[which] = isChecked;
                        }
                    });
                AlertDialog alert = alert_builder.create();
                alert.show();
            }
        });

        // Fill in previous answers
        List<Selection> ve = new ArrayList<Selection>();
        if (prompt.getAnswerValue() != null) {
            ve = (List<Selection>) prompt.getAnswerValue().getValue();
        }

        if (ve != null) {
            boolean first = true;
            for (int i = 0; i < selections.length; ++i) {

                String value = mItems.get(i).getValue();
                boolean found = false;
                for (Selection s : ve) {
                    if (value.equals(s.getValue())) {
                        found = true;
                        break;
                    }
                }

                selections[i] = found;

                if (found) {
                    if (first) {
                        first = false;
                        selectionText.setText(context.getString(R.string.selected)
                                + answer_items[i].toString());
                        selectionText.setVisibility(View.VISIBLE);
                    } else {
                        selectionText.setText(selectionText.getText() + ", "
                                + answer_items[i].toString());
                    }
                }

            }
        }

        addView(button);
        addView(selectionText);

    }


    @Override
    public IAnswerData getAnswer() {
    	clearFocus();
        List<Selection> vc = new ArrayList<Selection>();
        for (int i = 0; i < mItems.size(); i++) {
            if (selections[i]) {
                SelectChoice sc = mItems.get(i);
                vc.add(new Selection(sc));
            }
        }
        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }

    }


    @Override
    public void clearAnswer() {
        selectionText.setText(R.string.selected);
        selectionText.setVisibility(View.GONE);
        for (int i = 0; i < selections.length; i++) {
            selections[i] = false;
        }
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

    }


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        button.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        button.cancelLongPress();
    }

}
