package org.odk.collect.android.audio;

import androidx.annotation.NonNull;

public class Clip {

    @NonNull
    private final String clipID;
    @NonNull
    private final String uri;

    public Clip(@NonNull String clipID, @NonNull String uri) {
        this.clipID = clipID;
        this.uri = uri;
    }

    @NonNull
    public String getClipID() {
        return clipID;
    }

    @NonNull
    public String getURI() {
        return uri;
    }
}
