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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.PromptElement;
import org.odk.collect.android.R;
import org.odk.collect.android.SharedConstants;


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


    public void clearAnswer() {
        mStringAnswer.setText(null);
    }


    public IAnswerData getAnswer() {
        String s = mStringAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            return new StringData(s);
        }
    }


    public void buildView(PromptElement prompt) {
        this.setOrientation(LinearLayout.VERTICAL);

        mActionButton = new Button(getContext());
        mActionButton.setText(getContext().getString(R.string.get_barcode));
        mActionButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mActionButton.setPadding(20, 20, 20, 20);
        mActionButton.setEnabled(!prompt.isReadonly());

        // launch image capture intent on click
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("com.google.zxing.client.android.SCAN");
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.BARCODE_CAPTURE);
            }
        });
        
        
        mStringAnswer = new TextView(getContext());
        String s = prompt.getAnswerText();
        if (s != null) {
            mStringAnswer.setText(s);
            mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
            mStringAnswer.setGravity(Gravity.CENTER);
        }

        // finish complex layout
        this.addView(mActionButton);
        this.addView(mStringAnswer);

    }


    public void setBinaryData(Object answer) {
        mStringAnswer.setText((String)answer);
        
    }


}      