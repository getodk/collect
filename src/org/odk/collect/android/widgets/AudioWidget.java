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

import java.io.File;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class AudioWidget extends QuestionWidget implements IBinaryWidget {
	private final static String t = "MediaWidget";

	private Button mCaptureButton;
	private Button mPlayButton;
	private Button mChooseButton;

	private String mBinaryName;
	private String mInstanceFolder;

	public AudioWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);

		mInstanceFolder = Collect.getInstance().getFormController()
				.getInstancePath().getParent();

		setOrientation(LinearLayout.VERTICAL);

		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);

		// setup capture button
		mCaptureButton = new Button(getContext());
		mCaptureButton.setId(QuestionWidget.newUniqueId());
		mCaptureButton.setText(getContext().getString(R.string.capture_audio));
		mCaptureButton
				.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mCaptureButton.setPadding(20, 20, 20, 20);
		mCaptureButton.setEnabled(!prompt.isReadOnly());
		mCaptureButton.setLayoutParams(params);

		// launch capture intent on click
		mCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "captureButton", "click",
								mPrompt.getIndex());
				Intent i = new Intent(
						android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				i.putExtra(
						android.provider.MediaStore.EXTRA_OUTPUT,
						android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
								.toString());
				try {
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(mPrompt.getIndex());
					((Activity) getContext()).startActivityForResult(i,
							FormEntryActivity.AUDIO_CAPTURE);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(R.string.activity_not_found,
									"audio capture"), Toast.LENGTH_SHORT)
							.show();
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(null);
				}

			}
		});

		// setup capture button
		mChooseButton = new Button(getContext());
		mChooseButton.setId(QuestionWidget.newUniqueId());
		mChooseButton.setText(getContext().getString(R.string.choose_sound));
		mChooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mChooseButton.setPadding(20, 20, 20, 20);
		mChooseButton.setEnabled(!prompt.isReadOnly());
		mChooseButton.setLayoutParams(params);

		// launch capture intent on click
		mChooseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "chooseButton", "click",
								mPrompt.getIndex());
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("audio/*");
				try {
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(mPrompt.getIndex());
					((Activity) getContext()).startActivityForResult(i,
							FormEntryActivity.AUDIO_CHOOSER);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(R.string.activity_not_found,
									"choose audio"), Toast.LENGTH_SHORT).show();
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(null);
				}

			}
		});

		// setup play button
		mPlayButton = new Button(getContext());
		mPlayButton.setId(QuestionWidget.newUniqueId());
		mPlayButton.setText(getContext().getString(R.string.play_audio));
		mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mPlayButton.setPadding(20, 20, 20, 20);
		mPlayButton.setLayoutParams(params);

		// on play, launch the appropriate viewer
		mPlayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "playButton", "click",
								mPrompt.getIndex());
				Intent i = new Intent("android.intent.action.VIEW");
				File f = new File(mInstanceFolder + File.separator
						+ mBinaryName);
				i.setDataAndType(Uri.fromFile(f), "audio/*");
				try {
					((Activity) getContext()).startActivity(i);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(R.string.activity_not_found,
									"play audio"), Toast.LENGTH_SHORT).show();
				}

			}
		});

		// retrieve answer from data model and update ui
		mBinaryName = prompt.getAnswerText();
		if (mBinaryName != null) {
			mPlayButton.setEnabled(true);
		} else {
			mPlayButton.setEnabled(false);
		}

		// finish complex layout
		addView(mCaptureButton);
		addView(mChooseButton);
		addView(mPlayButton);

		// and hide the capture and choose button if read-only
		if (mPrompt.isReadOnly()) {
			mCaptureButton.setVisibility(View.GONE);
			mChooseButton.setVisibility(View.GONE);
		}
	}


    private void deleteMedia() {
        // get the file path and delete the file
    	String name = mBinaryName;
        // clean up variables
    	mBinaryName = null;
    	// delete from media provider
        int del = MediaUtils.deleteAudioFileFromMediaProvider(mInstanceFolder + File.separator + name);
        Log.i(t, "Deleted " + del + " rows from media content provider");
    }

	@Override
	public void clearAnswer() {
		// remove the file
		deleteMedia();

		// reset buttons
		mPlayButton.setEnabled(false);
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
	public void setBinaryData(Object binaryuri) {
		// when replacing an answer. remove the current media.
		if (mBinaryName != null) {
			deleteMedia();
		}

		// get the file path and create a copy in the instance folder
		String binaryPath = MediaUtils.getPathFromUri(this.getContext(), (Uri) binaryuri, Audio.Media.DATA);
		String extension = binaryPath.substring(binaryPath.lastIndexOf("."));
		String destAudioPath = mInstanceFolder + File.separator
				+ System.currentTimeMillis() + extension;

		File source = new File(binaryPath);
		File newAudio = new File(destAudioPath);
		FileUtils.copyFile(source, newAudio);

		if (newAudio.exists()) {
			// Add the copy to the content provier
			ContentValues values = new ContentValues(6);
			values.put(Audio.Media.TITLE, newAudio.getName());
			values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
			values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
			values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

			Uri AudioURI = getContext().getContentResolver().insert(
					Audio.Media.EXTERNAL_CONTENT_URI, values);
			Log.i(t, "Inserting AUDIO returned uri = " + AudioURI.toString());
			mBinaryName = newAudio.getName();
            Log.i(t, "Setting current answer to " + newAudio.getName());
		} else {
			Log.e(t, "Inserting Audio file FAILED");
		}

		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	@Override
	public boolean isWaitingForBinaryData() {
		return mPrompt.getIndex().equals(
				Collect.getInstance().getFormController()
						.getIndexWaitingForData());
	}

	@Override
	public void cancelWaitingForBinaryData() {
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mCaptureButton.setOnLongClickListener(l);
		mChooseButton.setOnLongClickListener(l);
		mPlayButton.setOnLongClickListener(l);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mCaptureButton.cancelLongPress();
		mChooseButton.cancelLongPress();
		mPlayButton.cancelLongPress();
	}

}
