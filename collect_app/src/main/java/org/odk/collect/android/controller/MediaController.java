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

import android.media.MediaPlayer;

import org.odk.collect.android.events.MediaEvent;
import org.odk.collect.android.events.RxEventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class MediaController implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    public static final int MEDIA_COMPLETED = 100;
    public static final int MEDIA_ERROR = 101;

    @Inject
    RxEventBus rxEventBus;

    private MediaPlayer player;

    @Inject
    MediaController() {
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        rxEventBus.post(new MediaEvent(MEDIA_COMPLETED));
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        rxEventBus.post(new MediaEvent(MEDIA_ERROR));
        return false;
    }
}
