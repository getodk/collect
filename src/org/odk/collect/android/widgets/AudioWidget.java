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
import android.database.Cursor;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.PromptElement;
import org.odk.collect.android.R;
import org.odk.collect.android.SharedConstants;

import java.io.File;


/**
 * Widget that allows user to record audio clips and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class AudioWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {

    // TODO: move files from root to answer folder
    // TODO: change answers in xml from content uri to filepath

    private Button mCaptureButton;
    private Button mPlayButton;
    private String mStringAnswer;
    private TextView mDisplayText;


    public AudioWidget(Context context) {
        super(context);
    }


    private void deleteFile() {
        Cursor c =
                getContext().getContentResolver().query(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                        "_display_name='" + mStringAnswer + "'", null, null);
        c.moveToFirst();
        
        int id = c.getInt(c.getColumnIndex("_id"));
        String path = c.getString(c.getColumnIndex("_data"));

        File f = new File(path);
        f.delete();
        getContext().getContentResolver().delete(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                "_id='" + id + "'", null);
        mStringAnswer = null;

    }


    public void clearAnswer() {
        deleteFile();
        mPlayButton.setEnabled(false);
        mCaptureButton.setText(getContext().getString(R.string.capture_audio));
        mDisplayText.setText(getContext().getString(R.string.no_capture));
    }


    public IAnswerData getAnswer() {
        if (mStringAnswer != null)
            return new StringData(mStringAnswer);
        else
            return null;
    }


    public void buildView(PromptElement prompt) {
        this.setOrientation(LinearLayout.VERTICAL);

        mCaptureButton = new Button(getContext());
        mCaptureButton.setText(getContext().getString(R.string.capture_audio));
        mCaptureButton
                .setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadonly());

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.AUDIO_CAPTURE);
            }
        });

        mPlayButton = new Button(getContext());
        mPlayButton.setText(getContext().getString(R.string.play_audio));
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mPlayButton.setPadding(20, 20, 20, 20);


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                Cursor c =
                        getContext().getContentResolver().query(
                                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                                "_display_name='" + mStringAnswer + "'", null, null);
                c.moveToFirst();
                int id = c.getInt(c.getColumnIndex("_id"));
                i.setDataAndType(Uri
                        .parse(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                                + id), "audio/3gp");
                ((Activity) getContext()).startActivity(i);
            }
        });

        mStringAnswer = prompt.getAnswerText();
        mPlayButton.setEnabled(mStringAnswer != null);
        if (mStringAnswer != null) {
            mCaptureButton.setText(getContext().getString(R.string.replace_audio));
        }

        mDisplayText = new TextView(getContext());
        if (mStringAnswer == null) {
            mDisplayText.setText(getContext().getString(R.string.no_capture));
        } else {
            mDisplayText.setText(getContext().getString(R.string.one_capture));
        }

        // finish complex layout
        this.addView(mDisplayText);
        this.addView(mCaptureButton);
        this.addView(mPlayButton);
    }


    public void setBinaryData(Object answer) {
        if (mStringAnswer != null) {
            deleteFile();
        }
        String str = (String) answer;
        Cursor c = getContext().getContentResolver().query(Uri.parse(str), null, null, null, null);
        c.moveToFirst();
        mStringAnswer = c.getString(c.getColumnIndex("_display_name"));
    }

}
