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
import org.odk.collect.android.views.AbstractFolioView;
import org.odk.collect.android.widgets.AbstractQuestionWidget.OnDescendantRequestFocusChangeListener.FocusChangeState;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class AudioWidget extends AbstractQuestionWidget implements IBinaryWidget {

    private final static String t = "AudioWidget";

    private Button mCaptureButton;
    private Button mPlayButton;

    private String mBinaryName;
    private TextView mDisplayText;

    private Uri mExternalUri;
    private String mCaptureIntent;
    private String mInstanceFolder;
    private int mRequestCode;
    private int mCaptureText;
    private int mReplaceText;
    private int mPlayText;


    public AudioWidget(Handler handler, Context context, FormEntryPrompt prompt, String instancePath) {
        super(handler, context, prompt);
        initialize(instancePath);
    }

    private void initialize(String instancePath) {
        mInstanceFolder = instancePath.substring(0, instancePath.lastIndexOf("/") + 1);

        mExternalUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        mCaptureIntent = android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION;
        mRequestCode = FormEntryActivity.AUDIO_CAPTURE;
        mCaptureText = R.string.capture_audio;
        mReplaceText = R.string.replace_audio;
        mPlayText = R.string.play_audio;
    }

    @Override
	public IAnswerData getAnswer() {
        if (mBinaryName != null) {
            return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }

    @Override
	protected void buildViewBodyImpl() {
        // setup capture button
        mCaptureButton = new Button(getContext());
        mCaptureButton.setText(getContext().getString(mCaptureText));
        mCaptureButton
                .setTextSize(TypedValue.COMPLEX_UNIT_DIP, AbstractFolioView.APPLICATION_FONTSIZE);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	// focus change for buttons is not fired in touch mode
            	if ( signalDescendant(FocusChangeState.DIVERGE_VIEW_FROM_MODEL) ) {
	                Intent i = new Intent(mCaptureIntent);
	                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mExternalUri.toString());
	                ((Activity) getContext()).startActivityForResult(i, mRequestCode);
            	}
            }
        });

        // setup play button
        mPlayButton = new Button(getContext());
        mPlayButton.setText(getContext().getString(mPlayText));
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AbstractFolioView.APPLICATION_FONTSIZE);
        mPlayButton.setPadding(20, 20, 20, 20);

        // on play, launch the appropriate viewer
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
            	// focus change for buttons is not fired in touch mode
            	if ( signalDescendant(FocusChangeState.DIVERGE_VIEW_FROM_MODEL) ) {
	                Intent i = new Intent("android.intent.action.VIEW");
	                File f = new File(mInstanceFolder + "/" + mBinaryName);
	                i.setDataAndType(Uri.fromFile(f), "audio/*");
	                ((Activity) getContext()).startActivity(i);
            	}
            }
        });

        // retrieve answer from data model and update ui
        mDisplayText = new TextView(getContext());
        mDisplayText.setPadding(5, 0, 0, 0);

        // finish complex layout
        addView(mCaptureButton);
        addView(mPlayButton);
    }

    private void deleteMedia() {
    	// done?
    	if ( mBinaryName == null ) return;
    	
        // get the file path and delete the file
        File f = new File(mInstanceFolder + "/" + mBinaryName);
        if (!f.delete()) {
            Log.i(t, "Failed to delete " + f);
        }

        // clean up variables
        mBinaryName = null;
    }

    protected void updateViewAfterAnswer() {
    	
    	String newAnswer = prompt.getAnswerText();
    	if ( mBinaryName != null && !mBinaryName.equals(newAnswer) ) {
    		deleteMedia();
    	}
        mBinaryName = newAnswer;
        
        if (mBinaryName != null) {
            mPlayButton.setEnabled(true);
            mCaptureButton.setText(getContext().getString(mReplaceText));
            mDisplayText.setText(getContext().getString(R.string.one_capture));
        } else {
            mPlayButton.setEnabled(false);
            mCaptureButton.setText(getContext().getString(mCaptureText));
            mDisplayText.setText(getContext().getString(R.string.no_capture));
        }
    }

    private Uri getUriFromPath(String path) {
        // find entry in content provider
        Cursor c =
            getContext().getContentResolver().query(mExternalUri, null, "_data='" + path + "'",
                null, null);
        c.moveToFirst();

        // create uri from path
        String newPath = mExternalUri + "/" + c.getInt(c.getColumnIndex("_id"));
        c.close();
        return Uri.parse(newPath);
    }


    private String getPathFromUri(Uri uri) {
        // find entry in content provider
        Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();

        // get data path
        String colString = c.getString(c.getColumnIndex("_data"));
        c.close();
        return colString;
    }


    @Override
	public void setBinaryData(Object binaryuri) {
        // you are replacing an answer. remove the media.
        if (mBinaryName != null) {
            deleteMedia();
        }

        // get the file path and move the file
        String binarypath = getPathFromUri((Uri) binaryuri);
        File f = new File(binarypath);
        String s = mInstanceFolder + "/" + binarypath.substring(binarypath.lastIndexOf('/') + 1);
        if (!f.renameTo(new File(s))) {
            Log.i(t, "Failed to rename " + f.getAbsolutePath());
        }

        // remove the database entry and update the name
        getContext().getContentResolver().delete(getUriFromPath(binarypath), null, null);
        mBinaryName = s.substring(s.lastIndexOf('/') + 1);
        saveAnswer(true); // and evaluate constraints and trigger UI update...
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        if (mBinaryName != null) {
            mPlayButton.setEnabled(isEnabled);
            mCaptureButton.setEnabled(isEnabled && !prompt.isReadOnly());
        } else {
            mPlayButton.setEnabled(false);
            mCaptureButton.setEnabled(isEnabled && !prompt.isReadOnly());
        }
    }
}
