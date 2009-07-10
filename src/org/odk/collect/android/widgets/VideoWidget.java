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
 * Widget that allows user to record video clips and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class VideoWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {
    // private static final String t = "VideoWidget";

    // TODO: move files to/from the answer folder using form handler.
    // TODO: Videos are currently in /sdcard/dcim/Camera/

    private Button mCaptureButton;
    private Button mPlayButton;
    private String mStringAnswer;
    private TextView mDisplayText;


    public VideoWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        deleteFile();
        mPlayButton.setEnabled(false);
        mCaptureButton.setText(getContext().getString(R.string.capture_video));
        mDisplayText.setText(getContext().getString(R.string.no_capture));
    }


    private void deleteFile() {
        Cursor c =
                getContext().getContentResolver().query(
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                        "_display_name='" + mStringAnswer + "'", null, null);
        c.moveToFirst();

        int id = c.getInt(c.getColumnIndex("_id"));
        String path = c.getString(c.getColumnIndex("_data"));

        File f = new File(path);
        f.delete();
        getContext().getContentResolver().delete(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "_id='" + id + "'",
                null);
        mStringAnswer = null;

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
        mCaptureButton.setText(getContext().getString(R.string.capture_video));

        mCaptureButton
                .setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadonly());

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
                ((Activity) getContext()).startActivityForResult(i, SharedConstants.VIDEO_CAPTURE);

            }
        });

        mPlayButton = new Button(getContext());
        mPlayButton.setText(getContext().getString(R.string.play_video));
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mPlayButton.setPadding(20, 20, 20, 20);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                Cursor c =
                        getContext().getContentResolver().query(
                                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                                "_display_name='" + mStringAnswer + "'", null, null);
                c.moveToFirst();
                int id = c.getInt(c.getColumnIndex("_id"));
                i.setDataAndType(Uri
                        .parse(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/"
                                + id), "video/3gp");
                ((Activity) getContext()).startActivity(i);


            }
        });

        mStringAnswer = prompt.getAnswerText();
        mPlayButton.setEnabled(mStringAnswer != null);
        if (mStringAnswer != null) {
            mCaptureButton.setText(getContext().getString(R.string.replace_video));
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
