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

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.DateTimeUtils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.view.View.GONE;

public class AudioController implements SeekBar.OnSeekBarChangeListener {

    private static final int SEEK_FORWARD_TIME = 5000; // 5 seconds
    private static final int SEEK_BACKWARD_TIME = 5000; // 5 seconds

    @BindView(R.id.currentDuration)
    TextView currentDurationLabel;
    @BindView(R.id.totalDuration)
    TextView totalDurationLabel;
    @BindView(R.id.playBtn)
    ImageButton playButton;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    private View view;
    private State state;
    private Context context;
    private MediaPlayer mediaPlayer;
    private FormEntryPrompt formEntryPrompt;
    private final Handler seekHandler = new Handler();

    /**
     * Background Runnable thread
     */
    private final Runnable updateTimeTask = new Runnable() {
        public void run() {
            try {
                if (mediaPlayer.isPlaying()) {
                    updateTimer();
                }
                seekHandler.postDelayed(this, 100);
            } catch (IllegalStateException e) {
                seekHandler.removeCallbacks(updateTimeTask);
                Timber.i(e, "Attempting to update timer when player is stopped");
            }
        }
    };

    void init(Context context, MediaPlayer mediaPlayer, FormEntryPrompt formEntryPrompt) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;
        this.formEntryPrompt = formEntryPrompt;

        initMediaPlayer();
        initControlsLayout();
    }

    @OnClick(R.id.fastForwardBtn)
    void fastForwardMedia() {
        seekTo(mediaPlayer.getCurrentPosition() + SEEK_FORWARD_TIME);
    }

    @OnClick(R.id.fastRewindBtn)
    void rewindMedia() {
        seekTo(mediaPlayer.getCurrentPosition() - SEEK_BACKWARD_TIME);
    }

    /**
     * Seeks media to the new position and updates timer
     */
    private void seekTo(int newPosition) {
        mediaPlayer.seekTo(Math.min(Math.max(0, newPosition), mediaPlayer.getDuration()));
        updateTimer();
    }

    @OnClick(R.id.playBtn)
    void playClicked() {
        if (mediaPlayer.isPlaying()) {
            pause();
            state = State.PAUSED;
        } else {
            play();
            state = State.PLAYING;
        }
    }

    private void updateTimer() {
        String totalDuration = DateTimeUtils.millisToTimer(mediaPlayer.getDuration());
        String currentDuration = DateTimeUtils.millisToTimer(mediaPlayer.getCurrentPosition());

        totalDurationLabel.setText(totalDuration);
        currentDurationLabel.setText(currentDuration);

        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
    }

    private void initControlsLayout() {
        view = LayoutInflater.from(context).inflate(R.layout.media_player_layout, null);
        ButterKnife.bind(this, view);
        seekBar.setOnSeekBarChangeListener(this);
    }

    private void initMediaPlayer() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(player -> {
            Timber.i("Media Prepared");
            updateTimer();
        });
        mediaPlayer.setOnCompletionListener(player -> {
            Timber.i("Completed");
            playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        });
    }

    /**
     * Update timer on seekbar
     */
    private void updateProgressBar() {
        seekHandler.postDelayed(updateTimeTask, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            seekTo(progress);
        }
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ((FormEntryActivity) context).allowSwiping(false);

        if (state == State.PLAYING) {
            pause();
        }
    }

    /**
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ((FormEntryActivity) context).allowSwiping(true);

        seekTo(seekBar.getProgress());
        if (state == State.PLAYING) {
            play();
        }
    }

    private void play() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "play", "click",
                        formEntryPrompt.getIndex());

        playButton.setImageResource(R.drawable.ic_pause_black_24dp);
        mediaPlayer.start();
        updateProgressBar();
    }

    private void pause() {
        Collect.getInstance()
                .getActivityLogger()
                .logInstanceAction(this, "pause", "click",
                        formEntryPrompt.getIndex());

        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        mediaPlayer.pause();
        seekHandler.removeCallbacks(updateTimeTask);
    }

    public void setMedia(File file) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.fromFile(file));
            mediaPlayer.prepareAsync();
            state = State.IDLE;
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    void hidePlayer() {
        view.setVisibility(GONE);
    }

    View getPlayerLayout() {
        return view;
    }

    private enum State {
        PAUSED, PLAYING, IDLE
    }
}
