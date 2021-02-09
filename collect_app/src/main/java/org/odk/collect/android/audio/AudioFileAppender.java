package org.odk.collect.android.audio;

import java.io.File;
import java.io.IOException;

public interface AudioFileAppender {

    void append(File one, File two) throws IOException;
}
