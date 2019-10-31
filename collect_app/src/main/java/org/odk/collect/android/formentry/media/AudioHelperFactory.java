package org.odk.collect.android.formentry.media;

import android.content.Context;

import org.odk.collect.android.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
