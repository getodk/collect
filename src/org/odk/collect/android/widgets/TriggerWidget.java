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
import org.odk.collect.android.views.AbstractFolioView;

import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Widget that allows user to scan barcodes and add them to the form.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class TriggerWidget extends AbstractQuestionWidget {

    private CheckBox mActionButton;
    private TextView mStringAnswer;
    private static String mOK = "OK";

    public TriggerWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
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
    protected void buildViewBodyImpl() {
        
        mActionButton = new CheckBox(getContext());
        mActionButton.setText(getContext().getString(R.string.trigger));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, AbstractFolioView.APPLICATION_FONTSIZE);
        //mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!prompt.isReadOnly());

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
                if (mActionButton.isChecked()) {
                    mStringAnswer.setText(mOK);
                } else {
                    mStringAnswer.setText(null);
                }
                signalDescendant(true);
            }
        });

        mStringAnswer = new TextView(getContext());
        mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PX, AbstractFolioView.APPLICATION_FONTSIZE);
        mStringAnswer.setGravity(Gravity.CENTER);

        // finish complex layout
        addView(mActionButton);
    }

    protected void updateViewAfterAnswer() {
		String s = prompt.getAnswerText();
		mStringAnswer.setText(s);
    	if ( s != null ) {
            if (s.equals(mOK)) {
                mActionButton.setChecked(true);
            } else {
                mActionButton.setChecked(false);
            }
        } else {
        	mActionButton.setChecked(false);
        }
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    	mActionButton.setEnabled(isEnabled && !prompt.isReadOnly());
    }
}
