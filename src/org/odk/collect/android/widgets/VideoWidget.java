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

import org.odk.collect.android.PromptElement;
import org.odk.collect.android.R;
import org.odk.collect.android.SharedConstants;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;

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


/**
 * Widget that allows user to record video clips and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class VideoWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {
    // private static final String t = "VideoWidget";

    /*
     * TODO: This works, but needs to get cleaned up a bit TODO: Also need to
     * add features to move files to/from the answer folder, but that'll be in
     * the export in FormHandler. Videos are currently in /sdcard/dcim/Camera/
     */
    private Button mRecordButton;
    private Button mPlayButton;
    private String mStringAnswer;
    private TextView mDisplayText;


    public VideoWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        deleteCurrentVideo();
        mPlayButton.setEnabled(false);
        mRecordButton.setText(getContext().getString(R.string.record));
        mDisplayText.setText(getContext().getString(R.string.no_recording));
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
        mRecordButton.setText(getContext().getString(R.string.record));

        mRecordButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mRecordButton.setPadding(20, 20, 20, 20);
        mRecordButton.setEnabled(!prompt.isReadonly());

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*
                 * We'd like to set our own path here, which android supports,
                 * but setting MediaStore.EXTRA_OUTPUT is broken in the current
                 * build. Filed under bug:
                 */
                Intent i = new Intent("android.media.action.VIDEO_CAPTURE");
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.VIDEO_CAPTURE);
            }
        });

        mPlayButton = new Button(getContext());
        mPlayButton.setText(getContext().getString(R.string.play));
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mPlayButton.setPadding(20, 20, 20, 20);


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                Cursor c =
                        getContext().getContentResolver().query(
                                Uri.parse("content://media/external/video/media/"), null,
                                "_display_name='" + mStringAnswer + "'", null, null);
                c.moveToFirst();
                int id = c.getInt(c.getColumnIndex("_id"));
                i.setDataAndType(Uri.parse("content://media/external/video/media/" + id),
                        "video/3gp");
                ((Activity) getContext()).startActivity(i);
            }
        });

        mStringAnswer = prompt.getAnswerText();
        mPlayButton.setEnabled(mStringAnswer != null);
        if (mStringAnswer != null) {
            mRecordButton.setText(getContext().getString(R.string.rerecord));
        }

        mDisplayText = new TextView(getContext());
        if (mStringAnswer == null) {
            mDisplayText.setText(getContext().getString(R.string.no_recording));
        } else {
            mDisplayText.setText(getContext().getString(R.string.recording_saved));
        }

        // finish complex layout
        this.addView(mDisplayText);
        this.addView(mRecordButton);
        this.addView(mPlayButton);
    }


    public void setBinaryData(Object answer) {
        if (mStringAnswer != null) {
            // User has selected new video, so delete the old one to clean
            // things up.
            deleteCurrentVideo();
        }
        String str = (String) answer;
        Cursor c = getContext().getContentResolver().query(Uri.parse(str), null, null, null, null);
        c.moveToFirst();
        mStringAnswer = c.getString(c.getColumnIndex("_display_name"));
    }


    private void deleteCurrentVideo() {
        getContext().getContentResolver().delete(
                Uri.parse("content://media/external/video/media/"),
                "_display_name='" + mStringAnswer + "'", null);
        mStringAnswer = null;
    }



}
