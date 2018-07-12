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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CaptureSelfieActivity;
import org.odk.collect.android.activities.CaptureSelfieActivityNewApi;
import org.odk.collect.android.activities.CaptureSelfieVideoActivity;
import org.odk.collect.android.activities.CaptureSelfieVideoActivityNewApi;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.widgets.interfaces.FileWidget;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static android.os.Build.MODEL;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.PermissionUtils.requestCameraPermission;

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

    public static final String NEXUS7 = "Nexus 7";
    private static final String DIRECTORY_PICTURES = "Pictures";

    @NonNull
    private MediaUtil mediaUtil;

    @NonNull
    private FileUtil fileUtil;

    private Button captureButton;
    private Button playButton;
    private Button chooseButton;
    private String binaryName;
    private Uri nexus7Uri;

    private boolean selfie;

    public VideoWidget(Context context, FormEntryPrompt prompt) {
        this(context, prompt, new FileUtil(), new MediaUtil());
    }

    public VideoWidget(Context context, FormEntryPrompt prompt, @NonNull FileUtil fileUtil, @NonNull MediaUtil mediaUtil) {
        super(context, prompt);

        this.fileUtil = fileUtil;
        this.mediaUtil = mediaUtil;

        String appearance = getFormEntryPrompt().getAppearanceHint();
        selfie = appearance != null && (appearance.equalsIgnoreCase("selfie") || appearance.equalsIgnoreCase("new-front"));

        captureButton = getSimpleButton(getContext().getString(R.string.capture_video), R.id.capture_video);
        captureButton.setEnabled(!prompt.isReadOnly());

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_video), R.id.choose_video);
        chooseButton.setEnabled(!prompt.isReadOnly());

        playButton = getSimpleButton(getContext().getString(R.string.play_video), R.id.play_video);

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

        hideButtonsIfNeeded();

        if (selfie) {
            boolean isFrontCameraAvailable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                isFrontCameraAvailable = CaptureSelfieActivityNewApi.isFrontCameraAvailable();
            } else {
                isFrontCameraAvailable = CaptureSelfieActivity.isFrontCameraAvailable();
            }

            if (!isFrontCameraAvailable) {
                captureButton.setEnabled(false);
                ToastUtils.showLongToast(R.string.error_front_camera_unavailable);
            }
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
    public static File getOutputMediaFile(int type) {
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
        MediaManager
                .INSTANCE
                .markOriginalFileOrDelete(getFormEntryPrompt().getIndex().toString(),
                        getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
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

    /**
     * Set this widget with the actual file returned by OnActivityResult.
     * Both of Uri and File are supported.
     * If the file is local, a Uri is enough for the copy task below.
     * If the chose file is from cloud(such as Google Drive),
     * The retrieve and copy task is already executed in the previous step,
     * so a File object would be presented.
     *
     * @param object Uri or File of the chosen file.
     * @see org.odk.collect.android.activities.FormEntryActivity#onActivityResult(int, int, Intent)
     */
    @Override
    public void setBinaryData(Object object) {
        File newVideo = null;
        // get the file path and create a copy in the instance folder
        if (object instanceof Uri) {
            String sourcePath = getSourcePathFromUri((Uri) object);
            String destinationPath = getDestinationPathFromSourcePath(sourcePath);
            File source = fileUtil.getFileAtPath(sourcePath);
            newVideo = fileUtil.getFileAtPath(destinationPath);
            fileUtil.copyFile(source, newVideo);
        } else if (object instanceof File) {
            newVideo = (File) object;
        } else {
            Timber.w("VideoWidget's setBinaryData must receive a File or Uri object.");
            return;
        }


        if (newVideo == null) {
            Timber.e("setBinaryData FAILED");
            return;
        }


        if (newVideo.exists()) {
            // Add the copy to the content provier
            ContentValues values = new ContentValues(6);
            values.put(Video.Media.TITLE, newVideo.getName());
            values.put(Video.Media.DISPLAY_NAME, newVideo.getName());
            values.put(Video.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Video.Media.DATA, newVideo.getAbsolutePath());

            MediaManager
                    .INSTANCE
                    .replaceRecentFileForQuestion(getFormEntryPrompt().getIndex().toString(), newVideo.getAbsolutePath());

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

        // Need to have this ugly code to account for
        // a bug in the Nexus 7 on 4.3 not returning the mediaUri in the data
        // of the intent - uri in this case is a file
        if (NEXUS7.equals(MODEL) && Build.VERSION.SDK_INT == 18) {
            if (object instanceof File) {
                File fileToDelete = (File) object;
                int delCount = fileToDelete.delete() ? 1 : 0;
                Timber.i("Deleting original capture of file: %s count: %d", binaryName, delCount);
            }
        }
    }

    private void hideButtonsIfNeeded() {
        if (getFormEntryPrompt().isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        } else if (selfie || (getFormEntryPrompt().getAppearanceHint() != null
                && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains("new"))) {
            chooseButton.setVisibility(View.GONE);
        }
    }

    private String getSourcePathFromUri(@NonNull Uri uri) {
        return mediaUtil.getPathFromUri(getContext(), uri, Video.Media.DATA);
    }

    private String getDestinationPathFromSourcePath(@NonNull String sourcePath) {
        String extension = sourcePath.substring(sourcePath.lastIndexOf('.'));
        return getInstanceFolder() + File.separator
                + fileUtil.getRandomFilename() + extension;
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

    @Override
    public void onButtonClick(int id) {
        switch (id) {
            case R.id.capture_video:
                requestCameraPermission((FormEntryActivity) getContext(), new PermissionListener() {
                    @Override
                    public void granted() {
                        captureVideo();
                    }

                    @Override
                    public void denied() {
                    }
                });
                break;
            case R.id.choose_video:
                chooseVideo();
                break;
            case R.id.play_video:
                playVideoFile();
                break;
        }
    }

    private void captureVideo() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "captureButton",
                        "click", getFormEntryPrompt().getIndex());
        Intent i;
        if (selfie) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i = new Intent(getContext(), CaptureSelfieVideoActivityNewApi.class);
            } else {
                i = new Intent(getContext(), CaptureSelfieVideoActivity.class);
            }
        } else {
            i = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
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
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.VIDEO_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.capture_video)), Toast.LENGTH_SHORT)
                    .show();
            cancelWaitingForData();
        }
    }

    private void chooseVideo() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "chooseButton",
                        "click", getFormEntryPrompt().getIndex());
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        // Intent i =
        // new Intent(Intent.ACTION_PICK,
        // android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.VIDEO_CHOOSER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.choose_video)), Toast.LENGTH_SHORT)
                    .show();

            cancelWaitingForData();
        }
    }

    private void playVideoFile() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "playButton",
                        "click", getFormEntryPrompt().getIndex());
        Intent i = new Intent("android.intent.action.VIEW");
        File f = new File(getInstanceFolder() + File.separator
                + binaryName);
        i.setDataAndType(Uri.fromFile(f), "video/*");
        try {
            getContext().startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.view_video)), Toast.LENGTH_SHORT).show();
        }
    }
}
