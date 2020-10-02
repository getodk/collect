package org.odk.collect.android.formentry.media;

import android.content.Context;
import android.media.MediaPlayer;

import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.async.Scheduler;

import java.util.function.Supplier;

public class ScreenContextAudioHelperFactory implements AudioHelperFactory {

    private final Scheduler scheduler;
    private final Supplier<MediaPlayer> mediaPlayerFactory;

    public ScreenContextAudioHelperFactory(Scheduler scheduler, Supplier<MediaPlayer> mediaPlayerFactory) {
        this.scheduler = scheduler;
        this.mediaPlayerFactory = mediaPlayerFactory;
    }

    public AudioHelper create(Context context) {
        ScreenContext screenContext = (ScreenContext) context;
        return new AudioHelper(screenContext.getActivity(), screenContext.getViewLifecycle(), scheduler, mediaPlayerFactory);
    }
}
