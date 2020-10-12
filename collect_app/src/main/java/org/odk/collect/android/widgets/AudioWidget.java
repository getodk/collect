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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.audio.AudioControllerView;
import org.odk.collect.android.databinding.AudioWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.QuestionMediaManager;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.FileWidget;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.AudioPlayer;
import org.odk.collect.android.widgets.utilities.ExternalAppAudioDataRequester;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.odk.collect.audioclips.Clip;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

@SuppressLint("ViewConstructor")
public class AudioWidget extends QuestionWidget implements FileWidget, WidgetDataReceiver {
    private final AudioPlayer audioPlayer;
    AudioWidgetAnswerBinding binding;

    private AudioControllerView audioController;

    private final WaitingForDataRegistry waitingForDataRegistry;
    private final ActivityAvailability activityAvailability;

    private QuestionMediaManager questionMediaManager;
    private String binaryName;

    public AudioWidget(Context context, QuestionDetails prompt, QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, AudioPlayer audioPlayer) {
        this(context, prompt, questionMediaManager,
                waitingForDataRegistry, new ActivityAvailability(context), audioPlayer);
    }

    AudioWidget(Context context, QuestionDetails questionDetails,
                QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry, ActivityAvailability activityAvailability, AudioPlayer audioPlayer) {
        super(context, questionDetails);
        this.audioPlayer = audioPlayer;

        this.questionMediaManager = questionMediaManager;
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.activityAvailability = activityAvailability;

        hideButtonsIfNeeded();

        binaryName = questionDetails.getPrompt().getAnswerText();
        updatePlayerMedia();
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = AudioWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());

        if (prompt.isReadOnly()) {
            binding.captureButton.setVisibility(View.GONE);
            binding.chooseButton.setVisibility(View.GONE);
        } else {
            binding.captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.chooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

            binding.captureButton.setOnClickListener(v -> new ExternalAppAudioDataRequester((Activity) getContext(), activityAvailability, waitingForDataRegistry, getPermissionUtils()).requestAudioRecording(getFormEntryPrompt()));
            binding.chooseButton.setOnClickListener(v -> chooseSound());
        }

        audioController = binding.audioController;

        return binding.getRoot();
    }

    @Override
    public void deleteFile() {
        audioPlayer.stop();
        questionMediaManager.markOriginalFileOrDelete(getFormEntryPrompt().getIndex().toString(),
                getInstanceFolder() + File.separator + binaryName);
        binaryName = null;
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteFile();

        // hide audio player
        audioController.setVisibility(GONE);

        widgetValueChanged();
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
     * @see org.odk.collect.android.activities.FormEntryActivity
     */
    @Override
    public void setData(Object object) {
        File newAudio;
        // get the file path and create a copy in the instance folder
        if (object instanceof File) {
            // Getting a file indicates we've done the copy in the before step
            newAudio = (File) object;
        } else {
            Timber.w("AudioWidget's setBinaryData must receive a File object.");
            return;
        }

        if (newAudio.exists()) {
            // Add the copy to the content provider
            ContentValues values = new ContentValues(6);
            values.put(Audio.Media.TITLE, newAudio.getName());
            values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
            values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

            questionMediaManager.replaceRecentFileForQuestion(getFormEntryPrompt().getIndex().toString(), newAudio.getAbsolutePath());
            Uri audioURI = getContext().getContentResolver().insert(Audio.Media.EXTERNAL_CONTENT_URI, values);

            if (audioURI != null) {
                Timber.i("Inserting AUDIO returned uri = %s", audioURI.toString());
            }

            // when replacing an answer. remove the current media.
            if (binaryName != null && !binaryName.equals(newAudio.getName())) {
                deleteFile();
            }

            binaryName = newAudio.getName();
            Timber.i("Setting current answer to %s", newAudio.getName());

            widgetValueChanged();
            updatePlayerMedia();
        } else {
            Timber.e("Inserting Audio file FAILED");
        }
    }

    private void hideButtonsIfNeeded() {
        if (getFormEntryPrompt().getAppearanceHint() != null
                && getFormEntryPrompt().getAppearanceHint().toLowerCase(Locale.ENGLISH).contains(WidgetAppearanceUtils.NEW)) {
            binding.chooseButton.setVisibility(View.GONE);
        }
    }

    private void updatePlayerMedia() {
        if (binaryName != null) {
            Clip clip = new Clip("audio:" + getFormEntryPrompt().getIndex().toString(), getAudioFile().getAbsolutePath());

            audioPlayer.onPlayingChanged(clip.getClipID(), audioController::setPlaying);
            audioPlayer.onPositionChanged(clip.getClipID(), audioController::setPosition);
            audioController.setDuration(getDurationOfFile(clip.getURI()));
            audioController.setListener(new AudioControllerView.Listener() {
                @Override
                public void onPlayClicked() {
                    audioPlayer.play(clip);
                }

                @Override
                public void onPauseClicked() {
                    audioPlayer.pause();
                }

                @Override
                public void onPositionChanged(Integer newPosition) {
                    audioPlayer.setPosition(clip.getClipID(), newPosition);
                }
            });

            audioController.setVisibility(View.VISIBLE);
        } else {
            audioController.setVisibility(GONE);
        }
    }

    private Integer getDurationOfFile(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationString != null ? Integer.parseInt(durationString) : 0;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.captureButton.setOnLongClickListener(l);
        binding.chooseButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.captureButton.cancelLongPress();
        binding.chooseButton.cancelLongPress();
    }

    private void chooseSound() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");

        if (activityAvailability.isActivityAvailable(intent)) {
            waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
            ((Activity) getContext()).startActivityForResult(intent, RequestCodes.AUDIO_CHOOSER);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.activity_not_found,
                    getContext().getString(R.string.choose_audio)), Toast.LENGTH_SHORT).show();
            waitingForDataRegistry.cancelWaitingForData();
        }
    }

    /**
     * Returns the audio file added to the widget for the current instance
     */
    private File getAudioFile() {
        return new File(getInstanceFolder() + File.separator + binaryName);
    }
}
