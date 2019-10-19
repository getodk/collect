package org.odk.collect.android.audio;

import androidx.core.util.ObjectsCompat;

public class PlaybackFailedException extends Exception {

    private final String uri;

    public PlaybackFailedException(String uri) {
        this.uri = uri;
    }

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
        PlaybackFailedException that = (PlaybackFailedException) o;
        return ObjectsCompat.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(uri);
    }

    @Override
    public String toString() {
        return "PlaybackFailedException{" +
                "uri='" + uri + '\'' +
                '}';
    }
}
