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
import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.odk.collect.android.R;
import org.odk.collect.android.events.MediaEvent;
import org.odk.collect.android.events.RxEventBus;
import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

@PerApplication
public final class MediaController implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    public static final int MEDIA_COMPLETED = 100;
    public static final int MEDIA_ERROR = 101;

    @Inject
    Context context;

    @Inject
    RxEventBus rxEventBus;

    @NonNull
    private MediaPlayer player;

    @Inject
    MediaController() {
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        rxEventBus.post(new MediaEvent(MEDIA_COMPLETED));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        rxEventBus.post(new MediaEvent(MEDIA_ERROR));
        return false;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void playAudio(@NonNull String uri) {
        String audioFilename = "";
        try {
            audioFilename = ReferenceManager.instance().deriveReference(uri).getLocalURI();
        } catch (InvalidReferenceException e) {
            Timber.e(e);
        }

        File audioFile = new File(audioFilename);
        if (audioFile.exists()) {
            try {
                player.reset();
                player.setDataSource(audioFilename);
                player.prepare();
                player.start();
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

    public void stopAudio() {
        player.stop();
    }
}
