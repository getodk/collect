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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Video;
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
public class VideoWidget extends QuestionWidget implements IBinaryWidget {
	private final static String t = "MediaWidget";

	private Button mCaptureButton;
	private Button mPlayButton;
	private Button mChooseButton;

	private String mBinaryName;

	private String mInstanceFolder;

	public static final boolean DEFAULT_HIGH_RESOLUTION = true;
	
	private static final String NEXUS7 = "Nexus 7";
	private static final String DIRECTORY_PICTURES = "Pictures";
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private Uri nexus7Uri;

	public VideoWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);

		mInstanceFolder = Collect.getInstance().getFormController()
				.getInstancePath().getParent();

		setOrientation(LinearLayout.VERTICAL);

		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);
		// setup capture button
		mCaptureButton = new Button(getContext());
		mCaptureButton.setId(QuestionWidget.newUniqueId());
		mCaptureButton.setText(getContext().getString(R.string.capture_video));
		mCaptureButton
				.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mCaptureButton.setPadding(20, 20, 20, 20);
		mCaptureButton.setEnabled(!prompt.isReadOnly());
		mCaptureButton.setLayoutParams(params);

		// launch capture intent on click
		mCaptureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect
			                .getInstance());
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(VideoWidget.this, "captureButton",
								"click", mPrompt.getIndex());
				Intent i = new Intent(
						android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
				
				// Need to have this ugly code to account for 
				// a bug in the Nexus 7 on 4.3 not returning the mediaUri in the data
				// of the intent - using the MediaStore.EXTRA_OUTPUT to get the data
				// Have it saving to an intermediate location instead of final destination
				// to allow the current location to catch issues with the intermediate file
				Log.i(t, "The build of this device is " + android.os.Build.MODEL);
				if (NEXUS7.equals(android.os.Build.MODEL) && Build.VERSION.SDK_INT == 18) {
					nexus7Uri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  
					i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, nexus7Uri);
				} else {
					i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
						Video.Media.EXTERNAL_CONTENT_URI.toString());
				}
				
				// request high resolution if configured for that...
				boolean high_resolution = settings.getBoolean(PreferencesActivity.KEY_HIGH_RESOLUTION,
		                VideoWidget.DEFAULT_HIGH_RESOLUTION);
				if(high_resolution) {
					i.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY,1);
				}
				try {
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(mPrompt.getIndex());
					((Activity) getContext()).startActivityForResult(i,
							FormEntryActivity.VIDEO_CAPTURE);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(R.string.activity_not_found,
									"capture video"), Toast.LENGTH_SHORT)
							.show();
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(null);
				}

			}
		});

		// setup capture button
		mChooseButton = new Button(getContext());
		mChooseButton.setId(QuestionWidget.newUniqueId());
		mChooseButton.setText(getContext().getString(R.string.choose_video));
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
						.logInstanceAction(VideoWidget.this, "chooseButton",
								"click", mPrompt.getIndex());
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.setType("video/*");
				// Intent i =
				// new Intent(Intent.ACTION_PICK,
				// android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
				try {
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(mPrompt.getIndex());
					((Activity) getContext()).startActivityForResult(i,
							FormEntryActivity.VIDEO_CHOOSER);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(R.string.activity_not_found,
									"choose video "), Toast.LENGTH_SHORT)
							.show();
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(null);
				}

			}
		});

		// setup play button
		mPlayButton = new Button(getContext());
		mPlayButton.setId(QuestionWidget.newUniqueId());
		mPlayButton.setText(getContext().getString(R.string.play_video));
		mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mPlayButton.setPadding(20, 20, 20, 20);
		mPlayButton.setLayoutParams(params);

		// on play, launch the appropriate viewer
		mPlayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(VideoWidget.this, "playButton",
								"click", mPrompt.getIndex());
				Intent i = new Intent("android.intent.action.VIEW");
				File f = new File(mInstanceFolder + File.separator
						+ mBinaryName);
				i.setDataAndType(Uri.fromFile(f), "video/*");
				try {
					((Activity) getContext()).startActivity(i);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(R.string.activity_not_found,
									"video video"), Toast.LENGTH_SHORT).show();
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
        int del = MediaUtils.deleteVideoFileFromMediaProvider(mInstanceFolder + File.separator + name);
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
		// you are replacing an answer. remove the media.
		if (mBinaryName != null) {
			deleteMedia();
		}

		// get the file path and create a copy in the instance folder
		String binaryPath = MediaUtils.getPathFromUri(this.getContext(), (Uri) binaryuri, Video.Media.DATA);
		String extension = binaryPath.substring(binaryPath.lastIndexOf("."));
		String destVideoPath = mInstanceFolder + File.separator
				+ System.currentTimeMillis() + extension;

		File source = new File(binaryPath);
		File newVideo = new File(destVideoPath);
		FileUtils.copyFile(source, newVideo);

		if (newVideo.exists()) {
			// Add the copy to the content provier
			ContentValues values = new ContentValues(6);
			values.put(Video.Media.TITLE, newVideo.getName());
			values.put(Video.Media.DISPLAY_NAME, newVideo.getName());
			values.put(Video.Media.DATE_ADDED, System.currentTimeMillis());
			values.put(Video.Media.DATA, newVideo.getAbsolutePath());

			Uri VideoURI = getContext().getContentResolver().insert(
					Video.Media.EXTERNAL_CONTENT_URI, values);
			Log.i(t, "Inserting VIDEO returned uri = " + VideoURI.toString());
		} else {
			Log.e(t, "Inserting Video file FAILED");
		}

		mBinaryName = newVideo.getName();
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
		
		// Need to have this ugly code to account for 
		// a bug in the Nexus 7 on 4.3 not returning the mediaUri in the data
		// of the intent - uri in this case is a file 
		if (NEXUS7.equals(android.os.Build.MODEL) && Build.VERSION.SDK_INT == 18) {
			Uri mediaUri = (Uri)binaryuri;
			File fileToDelete = new File(mediaUri.getPath());
			int delCount = fileToDelete.delete() ? 1 : 0;
			Log.i(t, "Deleting original capture of file: " + mediaUri.toString() + " count: " + delCount);
		} 
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
	
	/*
	 * Create a file Uri for saving an image or video 
	 * For Nexus 7 fix ... 
	 * See http://developer.android.com/guide/topics/media/camera.html for more info
	 */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/*
	 *  Create a File for saving an image or video 
	 *  For Nexus 7 fix ... 
	 *  See http://developer.android.com/guide/topics/media/camera.html for more info
	 */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), DIRECTORY_PICTURES);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(t, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSSZ", Locale.US).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

}
