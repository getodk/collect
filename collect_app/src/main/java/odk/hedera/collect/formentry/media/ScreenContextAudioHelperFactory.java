package odk.hedera.collect.formentry.media;

import android.content.Context;

import odk.hedera.collect.audio.AudioHelper;
import odk.hedera.collect.utilities.ScreenContext;

public class ScreenContextAudioHelperFactory implements AudioHelperFactory {

    public AudioHelper create(Context context) {
        ScreenContext screenContext = (ScreenContext) context;
        return new AudioHelper(screenContext.getActivity(), screenContext.getViewLifecycle());
    }
}
