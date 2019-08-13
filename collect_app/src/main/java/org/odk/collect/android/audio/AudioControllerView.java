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

package org.odk.collect.android.audio;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.odk.collect.android.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class AudioControllerView extends FrameLayout implements SeekBar.OnSeekBarChangeListener {

    @BindView(R.id.currentDuration)
    TextView currentDurationLabel;

    @BindView(R.id.totalDuration)
    TextView totalDurationLabel;

    @BindView(R.id.playBtn)
    @SuppressFBWarnings("UR")
    ImageButton playButton;

    @BindView(R.id.seekBar)
    @SuppressFBWarnings("UR")
    SeekBar seekBar;

    private State state;
    private Integer position;

    private Listener listener;

    public AudioControllerView(Context context) {
        super(context);

        View.inflate(context, R.layout.audio_controller_layout, this);
        ButterKnife.bind(this);
        seekBar.setOnSeekBarChangeListener(this);
        playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
    }

    @OnClick(R.id.fastForwardBtn)
    void fastForwardMedia() {
        int newPosition = position + 5000;
        listener.onPositionChanged(newPosition);
    }

    @OnClick(R.id.fastRewindBtn)
    void rewindMedia() {
        int newPosition = position - 5000;
        listener.onPositionChanged(newPosition);
    }

    @OnClick(R.id.playBtn)
    void playClicked() {
        if (state == State.PLAYING) {
            listener.onPauseClicked();
        } else {
            listener.onPlayClicked();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ((SwipableParent) getContext()).allowSwiping(false);

        if (state == State.PLAYING) {
            pause();
        }
    }

    /**
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ((SwipableParent) getContext()).allowSwiping(true);

        if (state == State.PLAYING) {
            play();
        }
    }

    private void play() {

    }

    private void pause() {

    }

    public void hidePlayer() {
        setVisibility(GONE);
    }

    public void showPlayer() {
        setVisibility(View.VISIBLE);
    }

    /**
     * Converts {@param millis} to mm:ss format
     *
     * @return formatted time as string
     */
    private static String getTime(long seconds) {
        return new DateTime(seconds, DateTimeZone.UTC).toString("mm:ss");
    }

    public void setPlayState(AudioPlayerViewModel.ClipState playState) {
        switch (playState) {
            case NOT_PLAYING:
                playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
                state = State.IDLE;
                break;
            case PLAYING:
                playButton.setImageResource(R.drawable.ic_pause_24dp);
                state = State.PLAYING;
                break;
            case PAUSED:
                playButton.setImageResource(R.drawable.ic_play_arrow_24dp);
                state = State.PAUSED;
                break;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setPosition(Integer position) {
        this.position = position;

        currentDurationLabel.setText(getTime(position));
        seekBar.setProgress(position);
    }

    public void setDuration(Integer duration) {
        totalDurationLabel.setText(getTime(duration));
        seekBar.setMax(duration);
    }

    private enum State {
        PAUSED, PLAYING, IDLE
    }

    public interface SwipableParent {
        void allowSwiping(boolean allowSwiping);
    }

    public interface Listener {

        void onPlayClicked();

        void onPauseClicked();

        void onPositionChanged(Integer newPosition);
    }
}
