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

import android.annotation.SuppressLint;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static android.os.Build.MODEL;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
@SuppressLint("ViewConstructor")
public class VideoWidget extends QuestionWidget implements FileWidget {

    public static final boolean DEFAULT_HIGH_RESOLUTION = true;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String NEXUS7 = "Nexus 7";
    private static final String DIRECTORY_PICTURES = "Pictures";

    @Nullable
    private MediaUtil mediaUtil = null;

    @Nullable
    private FileUtil fileUtil = null;

    private Button captureButton;
    private Button playButton;
    private Button chooseButton;
    private String binaryName;
    private String instanceFolder;
    private Uri nexus7Uri;

    public VideoWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        final FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            Timber.e("Started AudioWidget with null FormController.");
            return;
        }

        instanceFolder = formController.getInstancePath().getParent();

        captureButton = getSimpleButton(getContext().getString(R.string.capture_video));
        captureButton.setEnabled(!prompt.isReadOnly());
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(VideoWidget.this, "captureButton",
                                "click", formEntryPrompt.getIndex());
                Intent i = new Intent(
                        android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

                // Need to have this ugly code to account for
                // a bug in the Nexus 7 on 4.3 not returning the mediaUri in the data
                // of the intent - using the MediaStore.EXTRA_OUTPUT to get the data
                // Have it saving to an intermediate location instead of final destination
                // to allow the current location to catch issues with the intermediate file
                Timber.i("The build of this device is %s", MODEL);
                if (NEXUS7.equals(MODEL) && Build.VERSION.SDK_INT == 18) {
                    nexus7Uri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, nexus7Uri);
                } else {
                    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                            Video.Media.EXTERNAL_CONTENT_URI.toString());
                }

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect
                        .getInstance());

                // request high resolution if configured for that...
                boolean highResolution = settings.getBoolean(
                        PreferenceKeys.KEY_HIGH_RESOLUTION,
                        VideoWidget.DEFAULT_HIGH_RESOLUTION);
                if (highResolution) {
                    i.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 1);
                }
                try {
                    formController
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
                    ((Activity) getContext()).startActivityForResult(i,
                            FormEntryActivity.VIDEO_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.activity_not_found,
                                    "capture video"), Toast.LENGTH_SHORT)
                            .show();
                    formController
                            .setIndexWaitingForData(null);
                }

            }
        });

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_video));
        chooseButton.setEnabled(!prompt.isReadOnly());
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(VideoWidget.this, "chooseButton",
                                "click", formEntryPrompt.getIndex());
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("video/*");
                // Intent i =
                // new Intent(Intent.ACTION_PICK,
                // android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                try {
                    formController
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
                    ((Activity) getContext()).startActivityForResult(i,
                            FormEntryActivity.VIDEO_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.activity_not_found,
                                    "choose video "), Toast.LENGTH_SHORT)
                            .show();
                    formController
                            .setIndexWaitingForData(null);
                }

            }
        });

        playButton = getSimpleButton(getContext().getString(R.string.play_video));
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(VideoWidget.this, "playButton",
                                "click", formEntryPrompt.getIndex());
                Intent i = new Intent("android.intent.action.VIEW");
                File f = new File(instanceFolder + File.separator
                        + binaryName);
                i.setDataAndType(Uri.fromFile(f), "video/*");
                try {
                    getContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(
                            getContext(),
                            getContext().getString(R.string.activity_not_found,
                                    "video video"), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();
        if (binaryName != null) {
            playButton.setEnabled(true);
        } else {
            playButton.setEnabled(false);
        }

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(playButton);
        addAnswerView(answerLayout);

        // and hide the capture and choose button if read-only
        if (formEntryPrompt.isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        }

    }

    /*
     * Create a file Uri for saving an image or video
     * For Nexus 7 fix ...
     * See http://developer.android.com/guide/topics/media/camera.html for more info
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     *  Create a File for saving an image or video
     *  For Nexus 7 fix ...
     *  See http://developer.android.com/guide/topics/media/camera.html for more info
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
                DIRECTORY_PICTURES);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSSZ", Locale.US).format(
                new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void deleteFile() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = getMediaUtil().deleteVideoFileFromMediaProvider(
                instanceFolder + File.separator + name);
        Timber.i("Deleted %d rows from media content provider", del);
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteFile();

        // reset buttons
        playButton.setEnabled(false);
    }

    @Override
    public IAnswerData getAnswer() {
        if (binaryName != null) {
            return new StringData(binaryName);
        } else {
            return null;
        }
    }

    @Override
    public void setBinaryData(Object binaryuri) {
        if (binaryuri == null || !(binaryuri instanceof Uri)) {
            Timber.w("AudioWidget's setBinaryData must receive a Uri object.");
            return;
        }

        // get the file path and create a copy in the instance folder
        Uri uri = (Uri) binaryuri;

        String sourcePath = getSourcePathFromUri(uri);
        String destinationPath = getDestinationPathFromSourcePath(sourcePath);

        FileUtil fileUtil = getFileUtil();

        File source = fileUtil.getFileAtPath(sourcePath);
        File newVideo = fileUtil.getFileAtPath(destinationPath);

        getFileUtil().copyFile(source, newVideo);

        if (newVideo.exists()) {
            // Add the copy to the content provier
            ContentValues values = new ContentValues(6);
            values.put(Video.Media.TITLE, newVideo.getName());
            values.put(Video.Media.DISPLAY_NAME, newVideo.getName());
            values.put(Video.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Video.Media.DATA, newVideo.getAbsolutePath());

            Uri videoURI = getContext().getContentResolver().insert(
                    Video.Media.EXTERNAL_CONTENT_URI, values);

            if (videoURI != null) {
                Timber.i("Inserting VIDEO returned uri = %s", videoURI.toString());
            }

        } else {
            Timber.e("Inserting Video file FAILED");
        }
        // you are replacing an answer. remove the media.
        if (binaryName != null && !binaryName.equals(newVideo.getName())) {
            deleteFile();
        }

        binaryName = newVideo.getName();
        cancelWaitingForBinaryData();

        // Need to have this ugly code to account for
        // a bug in the Nexus 7 on 4.3 not returning the mediaUri in the data
        // of the intent - uri in this case is a file
        if (NEXUS7.equals(MODEL) && Build.VERSION.SDK_INT == 18) {
            File fileToDelete = new File(uri.getPath());
            int delCount = fileToDelete.delete() ? 1 : 0;

            Timber.i("Deleting original capture of file: %s count: %d", uri.toString(), delCount);
        }
    }

    private String getSourcePathFromUri(@NonNull Uri uri) {
        return getMediaUtil().getPathFromUri(getContext(), uri, Video.Media.DATA);
    }

    private String getDestinationPathFromSourcePath(@NonNull String sourcePath) {
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        return instanceFolder + File.separator
                + getFileUtil().getRandomFilename() + extension;
    }

    @NonNull
    public MediaUtil getMediaUtil() {
        if (mediaUtil == null) {
            mediaUtil = new MediaUtil();
        }

        return mediaUtil;
    }

    public void setMediaUtil(@Nullable MediaUtil mediaUtil) {
        this.mediaUtil = mediaUtil;
    }

    @NonNull
    public FileUtil getFileUtil() {
        if (fileUtil == null) {
            fileUtil = new FileUtil();
        }

        return fileUtil;
    }

    public void setFileUtil(@Nullable FileUtil fileUtil) {
        this.fileUtil = fileUtil;
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
        FormController formController = Collect.getInstance().getFormController();

        return formController != null
                && formEntryPrompt.getIndex().equals(formController.getIndexWaitingForData());

    }

    @Override
    public void cancelWaitingForBinaryData() {
        FormController formController = Collect.getInstance().getFormController();
        if (formController == null) {
            return;
        }

        formController.setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
        playButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
        playButton.cancelLongPress();
    }
}
