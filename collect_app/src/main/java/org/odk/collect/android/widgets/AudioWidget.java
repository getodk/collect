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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore.Audio;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.MediaPlayerUtilities;
import org.odk.collect.android.utilities.MediaUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class AudioWidget extends QuestionWidget implements IBinaryWidget {

    Handler seekHandler = new Handler();
    private Button captureButton;
    private ImageButton playButton;
    private Button chooseButton;
    private String binaryName;
    private String instanceFolder;
    private LinearLayout mediaPlayerLayout;
    private SeekBar seekBar;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    private TextView totalDurationLabel;
    private TextView currentDurationLabel;

    /**
     * Background Runnable thread
     */
    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            updateTimer();
            seekBar.setProgress(player.getCurrentPosition());
            seekHandler.postDelayed(this, 100);
        }
    };
    private ImageButton fastRewindButton;
    private ImageButton fastForwardButton;

    public AudioWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        instanceFolder = Collect.getInstance().getFormController()
                .getInstancePath().getParent();

        initLayout(context);
        initMediaPlayer();

        // setup capture button
        captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        captureButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "captureButton", "click",
                                formEntryPrompt.getIndex());
                Intent i = new Intent(
                        android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                i.putExtra(
                        android.provider.MediaStore.EXTRA_OUTPUT,
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                                .toString());
                try {
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
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
        chooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontsize);
        chooseButton.setEnabled(!prompt.isReadOnly());

        // launch capture intent on click
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "chooseButton", "click",
                                formEntryPrompt.getIndex());
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("audio/*");
                try {
                    Collect.getInstance().getFormController()
                            .setIndexWaitingForData(formEntryPrompt.getIndex());
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
        playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "playButton", "click",
                                formEntryPrompt.getIndex());

                if (player.isPlaying()) {
                    pause();
                } else {
                    play();
                }
            }
        });

        // setup fast backward button
        fastRewindButton.setBackgroundResource(R.drawable.ic_fast_rewind_black_24dp);
        fastRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "fastRewindButton", "click",
                                formEntryPrompt.getIndex());

                int currentPosition = player.getCurrentPosition();
                if (currentPosition - seekBackwardTime >= 0) {
                    player.seekTo(currentPosition - seekBackwardTime);
                } else {
                    player.seekTo(0);
                }
            }
        });

        // setup play button
        fastForwardButton.setBackgroundResource(R.drawable.ic_fast_forward_black_24dp);
        fastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collect.getInstance()
                        .getActivityLogger()
                        .logInstanceAction(this, "playButton", "click",
                                formEntryPrompt.getIndex());

                int currentPosition = player.getCurrentPosition();
                if (currentPosition + seekForwardTime <= player.getDuration()) {
                    player.seekTo(currentPosition + seekForwardTime);
                } else {
                    player.seekTo(player.getDuration());
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            /**
             * When user starts moving the progress handler
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // remove message Handler from updating progress bar
                seekHandler.removeCallbacks(updateTimeTask);
            }

            /**
             * When user stops moving the progress handler
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekHandler.removeCallbacks(updateTimeTask);
                player.seekTo(seekBar.getProgress());
                updateProgressBar();
            }
        });

        // retrieve answer from data model and update ui
        binaryName = prompt.getAnswerText();
        if (binaryName != null) {
            mediaPlayerLayout.setVisibility(VISIBLE);
            addMediaToPlayer();
        } else {
            mediaPlayerLayout.setVisibility(GONE);
        }

        // and hide the capture and choose button if read-only
        if (formEntryPrompt.isReadOnly()) {
            captureButton.setVisibility(View.GONE);
            chooseButton.setVisibility(View.GONE);
        }
    }

    private void initLayout(Context context) {
        View answerLayout = inflate(context, R.layout.audio_widget_layout, null);

        mediaPlayerLayout = (LinearLayout) answerLayout.findViewById(R.id.audioPlayer);
        currentDurationLabel = (TextView) answerLayout.findViewById(R.id.currentDuration);
        totalDurationLabel = (TextView) answerLayout.findViewById(R.id.totalDuration);
        seekBar = (SeekBar) answerLayout.findViewById(R.id.seekbar);
        captureButton = (Button) answerLayout.findViewById(R.id.recordBtn);
        chooseButton = (Button) answerLayout.findViewById(R.id.chooseBtn);
        playButton = (ImageButton) answerLayout.findViewById(R.id.playBtn);
        fastForwardButton = (ImageButton) answerLayout.findViewById(R.id.fastForwardBtn);
        fastRewindButton = (ImageButton) answerLayout.findViewById(R.id.fastRewindBtn);

        // finish complex layout
        addAnswerView(answerLayout);
    }

    private void updateTimer() {
        long totalDuration = player.getDuration();
        long currentDuration = player.getCurrentPosition();

        totalDurationLabel.setText(String.valueOf(MediaPlayerUtilities.milliSecondsToTimer(totalDuration)));
        currentDurationLabel.setText(String.valueOf(MediaPlayerUtilities.milliSecondsToTimer(currentDuration)));
    }

    private void play() {
        Timber.i("Playing");
        playButton.setBackgroundResource(R.drawable.ic_pause_black_24dp);

        player.start();
        seekBar.setMax(player.getDuration());
        updateProgressBar();
    }

    private void pause() {
        Timber.i("Paused");
        playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        player.pause();
    }

    private void deleteMedia() {
        // get the file path and delete the file
        String name = binaryName;
        // clean up variables
        binaryName = null;
        // delete from media provider
        int del = MediaUtils.deleteAudioFileFromMediaProvider(
                instanceFolder + File.separator + name);
        Timber.i("Deleted %d rows from media content provider", del);
    }

    @Override
    public void clearAnswer() {
        // remove the file
        deleteMedia();

        // reset buttons
        mediaPlayerLayout.setVisibility(GONE);
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
                Audio.Media.DATA);
        String extension = binaryPath.substring(binaryPath.lastIndexOf("."));
        String destAudioPath = instanceFolder + File.separator
                + System.currentTimeMillis() + extension;

        File source = new File(binaryPath);
        File newAudio = new File(destAudioPath);
        FileUtils.copyFile(source, newAudio);

        if (newAudio.exists()) {
            // Add the copy to the content provider
            ContentValues values = new ContentValues(6);
            values.put(Audio.Media.TITLE, newAudio.getName());
            values.put(Audio.Media.DISPLAY_NAME, newAudio.getName());
            values.put(Audio.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(Audio.Media.DATA, newAudio.getAbsolutePath());

            Uri audioURI = getContext().getContentResolver().insert(
                    Audio.Media.EXTERNAL_CONTENT_URI, values);
            Timber.i("Inserting AUDIO returned uri = %s", audioURI.toString());
            // when replacing an answer. remove the current media.
            if (binaryName != null && !binaryName.equals(newAudio.getName())) {
                deleteMedia();
            }
            binaryName = newAudio.getName();
            addMediaToPlayer();
            Timber.i("Setting current answer to %s", newAudio.getName());
        } else {
            Timber.e("Inserting Audio file FAILED");
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
    public void cancelLongPress() {
        super.cancelLongPress();
        captureButton.cancelLongPress();
        chooseButton.cancelLongPress();
    }

    private void addMediaToPlayer() {
        File f = new File(instanceFolder + File.separator
                + binaryName);
        try {
            player.setDataSource(getContext(), Uri.fromFile(f));
            player.prepareAsync();
            seekBar.setMax(player.getDuration());
            Timber.i("Preparing");
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void initMediaPlayer() {
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                Timber.i("Media Prepared");
                updateTimer();
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                Timber.i("Completed");
                playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            }
        });
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        seekHandler.postDelayed(updateTimeTask, 100);
    }
}
