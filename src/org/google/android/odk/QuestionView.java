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

package org.google.android.odk;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.google.android.odk.widgets.IBinaryWidget;
import org.google.android.odk.widgets.IQuestionWidget;
import org.google.android.odk.widgets.WidgetFactory;
import org.javarosa.core.model.data.IAnswerData;


/**
 * Responsible for using a {@link PromptElement} and based on the question type
 * and answer type, displaying the appropriate widget. The class also sets (but
 * does not save) and gets the answers to questions.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */

public class QuestionView extends ScrollView {

    private final static String t = "QuestionView";

    IQuestionWidget mQuestionWidget;
    PromptElement mPrompt;
    private LinearLayout mView;
    private final static int TEXTSIZE = 10;


    public QuestionView(Context context, PromptElement prompt) {
        super(context);

        Log.i(t, "calling constructor");
        this.mPrompt = prompt;
    }


    public PromptElement getPrompt() {
        return mPrompt;
    }


    /**
     * Create the appropriate view given your prompt.
     */
    public void buildView() {
        mView = new LinearLayout(getContext());
        mView.setOrientation(LinearLayout.VERTICAL);
        setPadding(10, 10, 10, 10);

        // display which group you are in as well as the question
        AddGroupText();
        AddQuestionText();
        AddHelpText();

        // if question or answer type is not supported, use text widget
        mQuestionWidget = WidgetFactory.createWidgetFromPrompt(mPrompt, getContext());

        mView.addView((View) mQuestionWidget);
        addView(mView);
    }


    public IAnswerData getAnswer() {
        return mQuestionWidget.getAnswer();
    }


    public void setBinaryData(Object answer) {
        if (mQuestionWidget instanceof IBinaryWidget)
            ((IBinaryWidget) mQuestionWidget).setBinaryData(answer);
        else
            Log.e(t, "Attempted to setBinaryData() on a non-binary widget ");
    }


    public void clearAnswer() {
        if (!mPrompt.isReadonly()) mQuestionWidget.clearAnswer();
    }


    /**
     * Add a TextView containing the hierarchy of groups to which the question
     * belongs.
     */
    private void AddGroupText() {
        String s = "";

        // list all groups in one string
        for (GroupElement g : mPrompt.getGroups()) {
            int i = g.getRepeatCount() + 1;
            s += g.getGroupText();
            if (i > 0) {
                s += " (" + i + ")";
            }
            s += " > ";
        }

        // build view
        if (!s.equals("")) {
            TextView tv = new TextView(getContext());
            tv.setText(s.substring(0, s.length() - 3));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, TEXTSIZE - 3);
            tv.setPadding(0, 0, 0, 5);
            mView.addView(tv);
        }
    }


    /**
     * Add a TextView containing the question text.
     */
    private void AddQuestionText() {
        TextView tv = new TextView(getContext());
        tv.setText(mPrompt.getQuestionText());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, TEXTSIZE);
        tv.setPadding(0, 0, 0, 5);

        // wrap to the widget of view
        tv.setHorizontallyScrolling(false);
        mView.addView(tv);
    }


    /**
     * Add a TextView containing the help text.
     */
    private void AddHelpText() {
        TextView tv = new TextView(getContext());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, TEXTSIZE - 3);
        tv.setPadding(0, 0, 0, 7);
        // wrap to the widget of view
        tv.setHorizontallyScrolling(false);

        String s = mPrompt.getHelpText();
        if (mPrompt.isReadonly()) {
            if (s == null) {
                s = getContext().getString(R.string.readonly_question);
            } else {
                s = getContext().getString(R.string.readonly_question) + " " + s;
            }
        }
        if (s != null && !s.equalsIgnoreCase("")) {
            tv.setText(s);
            mView.addView(tv);
        }
    }
}
