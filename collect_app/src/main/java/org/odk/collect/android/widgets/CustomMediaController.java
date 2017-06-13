/*
 * Copyright (C) 2017 Shobhit
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.MediaPlayerUtilities;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

class CustomMediaController implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private final Context context;
    private final MediaPlayer mediaPlayer;

    private ImageButton playButton;

    private SeekBar seekBar;

    private TextView totalDurationLabel;
    private TextView currentDurationLabel;

    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    private Handler seekHandler = new Handler();

    /**
     * Background Runnable thread
     */
    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying()) {
                updateTimer();
            }
            seekHandler.postDelayed(this, 100);
        }
    };

    CustomMediaController(Context context, MediaPlayer mediaPlayer) {
        this.context = context;
        this.mediaPlayer = mediaPlayer;

        initMediaPlayer();
    }

    private void updateTimer() {
        String totalDuration = MediaPlayerUtilities.milliSecondsToTimer(mediaPlayer.getDuration());
        String currentDuration = MediaPlayerUtilities.milliSecondsToTimer(mediaPlayer.getCurrentPosition());

        totalDurationLabel.setText(totalDuration);
        currentDurationLabel.setText(currentDuration);

        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
    }

    void initLayout(View view) {
        currentDurationLabel = (TextView) view.findViewById(R.id.currentDuration);
        totalDurationLabel = (TextView) view.findViewById(R.id.totalDuration);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        // setup play button
        playButton = (ImageButton) view.findViewById(R.id.playBtn);
        playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        playButton.setOnClickListener(this);

        // setup fast backward button
        ImageButton fastForwardButton = (ImageButton) view.findViewById(R.id.fastForwardBtn);
        fastForwardButton.setBackgroundResource(R.drawable.ic_fast_forward_black_24dp);
        fastForwardButton.setOnClickListener(this);

        // setup fast rewind button
        ImageButton fastRewindButton = (ImageButton) view.findViewById(R.id.fastRewindBtn);
        fastRewindButton.setBackgroundResource(R.drawable.ic_fast_rewind_black_24dp);
        fastRewindButton.setOnClickListener(this);
    }

    private void initMediaPlayer() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                Timber.i("Media Prepared");
                updateTimer();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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
    private void updateProgressBar() {
        seekHandler.postDelayed(updateTimeTask, 100);
    }

    @Override
    public void onClick(View v) {
        int currentPosition = mediaPlayer.getCurrentPosition();

        switch (v.getId()) {
            case R.id.playBtn:

                if (mediaPlayer.isPlaying()) {
                    pause();
                } else {
                    play();
                }
                break;

            case R.id.fastRewindBtn:

                if (currentPosition - seekBackwardTime >= 0) {
                    mediaPlayer.seekTo(currentPosition - seekBackwardTime);
                } else {
                    mediaPlayer.seekTo(0);
                }
                updateTimer();
                break;

            case R.id.fastForwardBtn:

                if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
                    mediaPlayer.seekTo(currentPosition + seekForwardTime);
                } else {
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                }
                updateTimer();
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mediaPlayer.seekTo(progress);
            updateTimer();
        }
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ((FormEntryActivity) context).allowSwiping(false);

        // remove message Handler from updating progress bar
        seekHandler.removeCallbacks(updateTimeTask);
        pause();
    }

    /**
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ((FormEntryActivity) context).allowSwiping(true);

        seekHandler.removeCallbacks(updateTimeTask);
        mediaPlayer.seekTo(seekBar.getProgress());
        updateProgressBar();
        play();
    }

    private void play() {
        playButton.setBackgroundResource(R.drawable.ic_pause_black_24dp);
        mediaPlayer.start();
        updateProgressBar();
    }

    private void pause() {
        playButton.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
        mediaPlayer.pause();
    }

    public void setMedia(File file) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.fromFile(file));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
