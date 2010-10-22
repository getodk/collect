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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.views.QuestionView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class BarcodeWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {

    private Button mActionButton;
    private TextView mStringAnswer;


    public BarcodeWidget(Context context) {
        super(context);
    }


    @Override
	public void clearAnswer() {
        mStringAnswer.setText(null);
        mActionButton.setText(getContext().getString(R.string.get_barcode));
    }


    @Override
	public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    @Override
	public void buildView(FormEntryPrompt prompt) {
        setOrientation(LinearLayout.VERTICAL);

        // set button formatting
        mActionButton = new Button(getContext());
        mActionButton.setText(getContext().getString(R.string.get_barcode));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionView.APPLICATION_FONTSIZE);
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!prompt.isReadOnly());

        // launch barcode capture intent on click
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
                Intent i = new Intent("com.google.zxing.client.android.SCAN");
                try {
                    ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.BARCODE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(),
                        getContext().getString(R.string.barcode_scanner_error), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // set text formatting
        mStringAnswer = new TextView(getContext());
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, QuestionView.APPLICATION_FONTSIZE);
        mStringAnswer.setGravity(Gravity.CENTER);

        String s = prompt.getAnswerText();
        if (s != null) {
            mActionButton.setText(getContext().getString(R.string.replace_barcode));
            mStringAnswer.setText(s);
        }
        // finish complex layout
        addView(mActionButton);
        addView(mStringAnswer);
    }


    /**
     * Allows answer to be set externally in {@Link FormEntryActivity}.
     */
    @Override
	public void setBinaryData(Object answer) {
        mStringAnswer.setText((String) answer);
    }


    @Override
	public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

}
