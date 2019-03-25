/*
 * Copyright 2019 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.events.MediaEvent;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public final class MediaController implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    public static final int MEDIA_PREPARED = 100;
    public static final int MEDIA_COMPLETED = 101;

    @Inject
    Context context;

    @Inject
    RxEventBus rxEventBus;

    @Nullable
    private MediaPlayer player;
    private String mediaUri;

    @Inject
    MediaController() {
    }

    private MediaPlayer getPlayer() {
        if (player == null) {
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
        }

        return player;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Timber.i("Media prepared");
        rxEventBus.post(new MediaEvent(MEDIA_PREPARED));
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Timber.i("Media completed");
        mediaPlayer.reset();
        rxEventBus.post(new MediaEvent(MEDIA_COMPLETED));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Timber.e("Error occurred : what = %d, extra = %d", what, extra);
        return false;
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void playAudio(@NonNull String uri) {
        mediaUri = uri;

        String audioFilename = "";
        try {
            audioFilename = ReferenceManager.instance().deriveReference(uri).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.e(e);
        }

        File audioFile = new File(audioFilename);
        if (audioFile.exists()) {
            try {
                setMedia(audioFile);
                startAudio();
            } catch (IOException e) {
                String errorMsg = context.getString(R.string.audio_file_invalid, audioFilename);
                Timber.e(errorMsg);
                ToastUtils.showLongToast(errorMsg);
            }
        } else {
            String errorMsg = context.getString(R.string.file_missing, audioFile);
            Timber.e(errorMsg);
            ToastUtils.showLongToast(errorMsg);
        }
    }

    public void startAudio() {
        getPlayer().start();
    }

    public void pauseAudio() {
        getPlayer().pause();
    }

    public void stopAndResetAudio() {
        if (isPlaying()) {
            getPlayer().stop();
            getPlayer().reset();
        }
    }

    public int getDuration() {
        return getPlayer().getDuration();
    }

    public int getCurrentPosition() {
        return getPlayer().getCurrentPosition();
    }

    public void seekTo(int duration) {
        getPlayer().seekTo(duration);
    }

    public void setMedia(File file) throws IOException {
        stopAndResetAudio();
        getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
        getPlayer().setDataSource(context, Uri.fromFile(file));
        getPlayer().prepare();
    }

    public void releaseResources() {
        if (player != null) {
            player.release();
            player = null;
            mediaUri = null;
        }
    }

    public boolean isPlayingMediaUri(String uri) {
        return isPlaying() && uri != null && uri.equals(mediaUri);
    }
}
