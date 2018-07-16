/*
 * Copyright (C) 2018 Shobhit Agarwal
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
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.utilities.FileUtil;
import org.odk.collect.android.utilities.MediaManager;
import org.odk.collect.android.utilities.MediaUtil;
import org.odk.collect.android.widgets.interfaces.FileWidget;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;
import static org.odk.collect.android.utilities.PermissionUtils.requestRecordAudioPermission;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class AudioWidget extends QuestionWidget implements FileWidget {

    @NonNull
    private FileUtil fileUtil;

    @NonNull
    private MediaUtil mediaUtil;

    private AudioController audioController;
    private Button captureButton;
    private Button chooseButton;

    private String binaryName;

    public AudioWidget(Context context, FormEntryPrompt prompt) {
        this(context, prompt, new FileUtil(), new MediaUtil(), new AudioController());
    }

    AudioWidget(Context context, FormEntryPrompt prompt, @NonNull FileUtil fileUtil, @NonNull MediaUtil mediaUtil, @NonNull AudioController audioController) {
        super(context, prompt);

        this.fileUtil = fileUtil;
        this.mediaUtil = mediaUtil;
        this.audioController = audioController;

        captureButton = getSimpleButton(getContext().getString(R.string.capture_audio), R.id.capture_audio);
        captureButton.setEnabled(!prompt.isReadOnly());

        chooseButton = getSimpleButton(getContext().getString(R.string.choose_sound), R.id.choose_sound);
        chooseButton.setEnabled(!prompt.isReadOnly());

        audioController.init(context, getPlayer(), getFormEntryPrompt());

        // finish complex layout
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(captureButton);
        answerLayout.addView(chooseButton);
        answerLayout.addView(audioController.getPlayerLayout(answerLayout));
        addAnswerView(answerLayout);

        hideButtonsIfNeeded();

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();
        if (binaryName != null) {
            audioController.setMedia(getAudioFile());
        } else {
            audioController.hidePlayer();
        }
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

        // hide audio player
        audioController.hidePlayer();
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
        File newAudio;
        // get the file path and create a copy in the instance folder
        if (object instanceof Uri) {
            String sourcePath = getSourcePathFromUri((Uri) object);
            String destinationPath = getDestinationPathFromSourcePath(sourcePath);
            File source = fileUtil.getFileAtPath(sourcePath);
            newAudio = fileUtil.getFileAtPath(destinationPath);
            fileUtil.copyFile(source, newAudio);
        } else if (object instanceof File) {
            // Getting a file indicates we've done the copy in the before step
            newAudio = (File) object;
        } else {
            Timber.w("AudioWidget's setBinaryData must receive a File or Uri object.");
            return;
        }

        if (newAudio == null) {
            Timber.e("setBinaryData FAILED");
            return;
        }

        if (newAudio.exists()) {
            // Add the copy to the content provier
            ContentValues values = new ContentValues(6);
            values.put(Audio.Media.TITLE, newAudio.getName());
            values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
            values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

            MediaManager
                    .INSTANCE
                    .replaceRecentFileForQuestion(getFormEntryPrompt().getIndex().toString(), newAudio.getAbsolutePath());

            Uri audioURI = getContext().getContentResolver().insert(
                    Audio.Media.EXTERNAL_CONTENT_URI, values);

            if (audioURI != null) {
                Timber.i("Inserting AUDIO returned uri = %s", audioURI.toString());
            }

            // when replacing an answer. remove the current media.
            if (binaryName != null && !binaryName.equals(newAudio.getName())) {
                deleteFile();
            }

            binaryName = newAudio.getName();
            Timber.i("Setting current answer to %s", newAudio.getName());
        } else {
            Timber.e("Inserting Audio file FAILED");
        }
    }

    private void hideButtonsIfNeeded() {
        if (getFormEntryPrompt().isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        } else if (getFormEntryPrompt().getAppearanceHint() != null
                && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains("new")) {
            chooseButton.setVisibility(View.GONE);
        }
    }

    private String getSourcePathFromUri(@NonNull Uri uri) {
        return mediaUtil.getPathFromUri(getContext(), uri, Audio.Media.DATA);
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
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
    }

    @Override
    public void onButtonClick(int buttonId) {
        switch (buttonId) {
            case R.id.capture_audio:
                requestRecordAudioPermission((FormEntryActivity) getContext(), new PermissionListener() {
                    @Override
                    public void granted() {
                        captureAudio();
                    }

                    @Override
                    public void denied() {
                    }
                });
                break;
            case R.id.choose_sound:
                chooseSound();
                break;
        }
    }

    private void captureAudio() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "captureButton", "click",
                        getFormEntryPrompt().getIndex());
        Intent i = new Intent(
                android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        i.putExtra(
                android.provider.MediaStore.EXTRA_OUTPUT,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        .toString());
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.AUDIO_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.capture_audio)), Toast.LENGTH_SHORT)
                    .show();
            cancelWaitingForData();
        }
    }

    private void chooseSound() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "chooseButton", "click",
                        getFormEntryPrompt().getIndex());
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        try {
            waitForData();
            ((Activity) getContext()).startActivityForResult(i,
                    RequestCodes.AUDIO_CHOOSER);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    getContext().getString(R.string.activity_not_found,
                            getContext().getString(R.string.choose_audio)), Toast.LENGTH_SHORT).show();
            cancelWaitingForData();
        }
    }

    /**
     * Returns the audio file added to the widget for the current instance
     */
    private File getAudioFile() {
        return new File(getInstanceFolder() + File.separator + binaryName);
    }
}
