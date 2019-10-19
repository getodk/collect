package org.odk.collect.android.audio;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Clip clip = (Clip) o;
        return getClipID().equals(clip.getClipID()) &&
                getURI().equals(clip.getURI());
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(getClipID(), getClipID());
    }

    @Override
    public String toString() {
        return "Clip{" +
                "clipID='" + clipID + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
