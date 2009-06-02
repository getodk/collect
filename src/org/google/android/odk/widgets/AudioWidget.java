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

package org.google.android.odk.widgets;

import org.google.android.odk.PromptElement;
import org.google.android.odk.SharedConstants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Widget that allows user to record audio clips and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class AudioWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {

/*
 * TODO:  This works, but needs to get cleaned up a bit, and we need to add it to Javarosa
 */
    private Button mRecordButton;
    private Button mPlayButton;
    private String mStringAnswer;
    private TextView mDisplayText;

    public AudioWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        mStringAnswer = null;
        mPlayButton.setEnabled(false);
        mRecordButton.setText("Record");
        mDisplayText.setText("Nothing recorded yet...");
    }


    public IAnswerData getAnswer() {
        if (mStringAnswer != null)
            return new StringData(mStringAnswer);
        else
            return null;
    }


    public void buildView(PromptElement prompt) {
        this.setOrientation(LinearLayout.VERTICAL);

        mRecordButton = new Button(getContext());
        mRecordButton.setText("Record");
        mRecordButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mRecordButton.setPadding(20, 20, 20, 20);
        mRecordButton.setEnabled(!prompt.isReadonly());

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.provider.MediaStore.RECORD_SOUND");
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.AUDIO_CAPTURE);
            }
        });
        
        mPlayButton = new Button(getContext());
        mPlayButton.setText("Play");
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mPlayButton.setPadding(20, 20, 20, 20);


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                i.setData(Uri.parse(mStringAnswer));
                ((Activity) getContext()).startActivity(i);
            }
        });
        
        mStringAnswer = prompt.getAnswerText();
        mPlayButton.setEnabled(mStringAnswer != null);
        if (mStringAnswer != null) {
            mRecordButton.setText("Rerecord");
        }

        mDisplayText = new TextView(getContext());
        if (mStringAnswer == null) {
            mDisplayText.setText("Nothing recorded yet...");
        } else {
            mDisplayText.setText("One recording saved");
        }
        
        // finish complex layout
        this.addView(mDisplayText);
        this.addView(mRecordButton);
        this.addView(mPlayButton);
    }


    public void setBinaryData(Object answer) {
        mStringAnswer = (String)answer;
    }


}      