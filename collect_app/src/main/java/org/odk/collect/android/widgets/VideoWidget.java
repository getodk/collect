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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Video;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class VideoWidget extends QuestionWidget implements IBinaryWidget {

    private Button captureButton;
    private Button chooseButton;
    private String binaryName;

    private String instanceFolder;

    public static final boolean DEFAULT_HIGH_RESOLUTION = true;

    private static final String NEXUS7 = "Nexus 7";
    private static final String DIRECTORY_PICTURES = "Pictures";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri nexus7Uri;
    private VideoView videoView;
    private FrameLayout videoPlayer;
    private RelativeLayout popupView;
    private Button play;
    private Button open;

    public VideoWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        instanceFolder = Collect.getInstance().getFormController()
                .getInstancePath().getParent();

        initLayout(context);

        // setup capture button
        captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        captureButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
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
                Timber.i("The build of this device is %s", android.os.Build.MODEL);
                if (NEXUS7.equals(android.os.Build.MODEL) && Build.VERSION.SDK_INT == 18) {
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
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
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
        chooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        chooseButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(VideoWidget.this, "chooseButton",
                                "click", formEntryPrompt.getIndex());
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("video/*");
                try {
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
                    ((Activity) getContext()).startActivityForResult(i,
                            FormEntryActivity.VIDEO_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    ToastUtils.showShortToast(
                            getContext().getString(R.string.activity_not_found, "choose video "));
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(null);
                }

            }
        });

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();
        if (binaryName != null) {
            videoPlayer.setVisibility(VISIBLE);
            addMediaToLayout();
        } else {
            videoPlayer.setVisibility(GONE);
        }

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

    private void deleteMedia() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = MediaUtils.deleteVideoFileFromMediaProvider(
                instanceFolder + File.separator + name);
        Timber.i("Deleted %d rows from media content provider", del);
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

        // get the file path and create a copy in the instance folder
        String binaryPath = MediaUtils.getPathFromUri(this.getContext(), (Uri) binaryuri,
                Video.Media.DATA);
        String extension = binaryPath.substring(binaryPath.lastIndexOf("."));
        String destVideoPath = instanceFolder + File.separator
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

            Uri videoURI = getContext().getContentResolver().insert(
                    Video.Media.EXTERNAL_CONTENT_URI, values);
            Timber.i("Inserting VIDEO returned uri = %s", videoURI.toString());
        } else {
            Timber.e("Inserting Video file FAILED");
        }
        // you are replacing an answer. remove the media.
        if (binaryName != null && !binaryName.equals(newVideo.getName())) {
            deleteMedia();
        }

        binaryName = newVideo.getName();
        Collect.getInstance().getFormController().setIndexWaitingForData(null);

        // Need to have this ugly code to account for
        // a bug in the Nexus 7 on 4.3 not returning the mediaUri in the data
        // of the intent - uri in this case is a file
        if (NEXUS7.equals(android.os.Build.MODEL) && Build.VERSION.SDK_INT == 18) {
            Uri mediaUri = (Uri) binaryuri;
            File fileToDelete = new File(mediaUri.getPath());
            int delCount = fileToDelete.delete() ? 1 : 0;
            Timber.i("Deleting original capture of file: %s count: %d", mediaUri.toString(), delCount);
        }

        addMediaToLayout();
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
        return formEntryPrompt.getIndex().equals(
                Collect.getInstance().getFormController()
                        .getIndexWaitingForData());
    }

    @Override
    public void cancelWaitingForBinaryData() {
        Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        captureButton.setOnLongClickListener(l);
        chooseButton.setOnLongClickListener(l);
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteMedia();

        videoPlayer.setVisibility(GONE);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
    }

    private void initLayout(final Context context) {
        View answerLayout = inflate(context, R.layout.video_widget_layout, null);

        captureButton = (Button) answerLayout.findViewById(R.id.recordBtn);
        chooseButton = (Button) answerLayout.findViewById(R.id.chooseBtn);

        videoPlayer = (FrameLayout) answerLayout.findViewById(R.id.videoPlayer);
        videoView = (VideoView) answerLayout.findViewById(R.id.videoView);
        popupView = (RelativeLayout) answerLayout.findViewById(R.id.popupView);

        answerLayout.findViewById(R.id.play).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        answerLayout.findViewById(R.id.open).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchExternalIntent(context);
            }
        });

        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return popupView.getVisibility() == VISIBLE;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(1);
                popupView.setVisibility(VISIBLE);
            }
        });

        // finish complex layout
        addAnswerView(answerLayout);
    }

    private void play() {
        popupView.setVisibility(GONE);
        videoView.start();
    }

    private void launchExternalIntent(Context context) {
        Intent i = new Intent("android.intent.action.VIEW");
        File f = new File(instanceFolder + File.separator
                + binaryName);
        i.setDataAndType(Uri.fromFile(f), "video/*");
        try {
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            ToastUtils.showShortToast(
                    getContext().getString(R.string.activity_not_found, "video video"));
        }
    }

    private void addMediaToLayout() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(instanceFolder + File.separator + binaryName);
            Bitmap frame = retriever.getFrameAtTime();
            int width = frame.getWidth();
            int height = frame.getHeight();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) getContext())
                    .getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displayMetrics);

            int windowWidth = displayMetrics.widthPixels;

            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(windowWidth, windowWidth * height / width);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.topMargin = 20;

            File f = new File(instanceFolder + File.separator + binaryName);
            videoView.setLayoutParams(layoutParams);
            popupView.setLayoutParams(layoutParams);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.fromFile(f));
            videoView.requestFocus();
            videoView.seekTo(1);
        } catch (IllegalArgumentException e) {
            Timber.e(e);
        }
    }
}
