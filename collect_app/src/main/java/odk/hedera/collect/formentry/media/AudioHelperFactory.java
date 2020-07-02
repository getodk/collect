package odk.hedera.collect.formentry.media;

import android.content.Context;

import odk.hedera.collect.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
